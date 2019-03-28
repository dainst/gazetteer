package org.dainst.gazetteer.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.dainst.gazetteer.converter.JsonPlaceSerializer;
import org.dainst.gazetteer.converter.ShapefileCreator;
import org.dainst.gazetteer.dao.GroupRoleRepository;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.dao.RecordGroupRepository;
import org.dainst.gazetteer.domain.GroupRole;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.RecordGroup;
import org.dainst.gazetteer.domain.User;
import org.dainst.gazetteer.helpers.PlaceAccessService;
import org.dainst.gazetteer.helpers.ProtectLocationsService;
import org.dainst.gazetteer.search.ElasticSearchClientProvider;
import org.dainst.gazetteer.search.ElasticSearchPlaceQuery;
import org.dainst.gazetteer.search.ElasticSearchSuggestionQuery;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContext;

@Controller
public class SearchController {

	private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

	@Autowired
	private PlaceRepository placeDao;

	@Autowired
	private GroupRoleRepository groupRoleDao;

	@Autowired
	private RecordGroupRepository groupDao;

	@Autowired
	private JsonPlaceSerializer jsonPlaceSerializer;

	@Autowired
	private ShapefileCreator shapefileCreator;

	@Autowired
	private ProtectLocationsService protectLocationsService;

	@Autowired
	private ElasticSearchClientProvider clientProvider;

	@Value("${baseUri}")
	private String baseUri;

	@Value("${languages}")
	private String[] languages;

	@Value("${googleMapsApiKey}")
	private String googleMapsApiKey;

	@Autowired
	MessageSource messageSource;

	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public ModelAndView simpleSearch(@RequestParam(defaultValue = "10") int limit,
			@RequestParam(defaultValue = "0") int offset, @RequestParam(required = false) String q,
			@RequestParam(required = false) String fq, @RequestParam(required = false) String sort,
			@RequestParam(defaultValue = "asc") String order, @RequestParam(required = false) String type,
			@RequestParam(required = false, defaultValue = "map,table") String view,
			@RequestParam(required = false) String callback, @RequestParam(required = false) String showInReview,
			@RequestParam(required = false) double[] bbox,
			@RequestParam(required = false) double[] polygonFilterCoordinates,
			@RequestParam(required = false) boolean showHiddenPlaces, @RequestParam(required = false) String add,
			@RequestParam(required = false) boolean noPolygons, @RequestParam(required = false) String queryId,
			@RequestParam(required = false) boolean pretty, HttpServletRequest request, HttpServletResponse response) {

		logger.info("bbox:" + Arrays.toString(bbox));

		RequestContext requestContext = new RequestContext(request);
		Locale locale = requestContext.getLocale();
		Locale originalLocale = request.getLocale();

		User user = null;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof User)
			user = (User) principal;

		logger.debug("Searching places with query: " + q + ", fq: " + fq + ", limit: " + limit + ", offset: " + offset
				+ ", type: " + type);

		ElasticSearchPlaceQuery query = getQuery(q, fq, type, sort, order, add, bbox, polygonFilterCoordinates, limit,
				offset, showHiddenPlaces, showInReview, user);

		// get ids from elastic search
		String[] result = query.execute();

		logger.debug("Querying index returned: " + result.length + " places");
		logger.debug("Result: {}", Arrays.toString(result));

		// get places for the result ids from db
		List<Place> places = placesForList(result, !noPolygons);
		logger.debug("Places: {}", places);
		Map<String, List<String[]>> facets = processAggregations(query, locale);

		Map<String, List<Place>> parents = new HashMap<String, List<Place>>();
		Map<String, PlaceAccessService.AccessStatus> accessStatusMap = new HashMap<String, PlaceAccessService.AccessStatus>();
		Map<String, PlaceAccessService.AccessStatus> parentAccessStatusMap = new HashMap<String, PlaceAccessService.AccessStatus>();

		PlaceAccessService placeAccessService = new PlaceAccessService(groupDao, groupRoleDao);

		for (Place place : places) {
			PlaceAccessService.AccessStatus accessStatus = placeAccessService.getAccessStatus(place);
			protectLocationsService.protectLocations(user, place, accessStatus);
			accessStatusMap.put(place.getId(), accessStatus);

			if (add != null && add.contains("parents") && !place.isDeleted()) {
				List<Place> placeParents = new ArrayList<Place>();
				createParentsList(place, placeParents, false);

				for (Place parent : placeParents) {
					parentAccessStatusMap.put(parent.getId(), placeAccessService.getAccessStatus(parent));
					protectLocationsService.protectLocations(user, parent, placeAccessService.getAccessStatus(parent));
				}

				parents.put(place.getId(), placeParents);
			}
		}

		jsonPlaceSerializer.setBaseUri(baseUri);
		jsonPlaceSerializer.setPretty(pretty);
		jsonPlaceSerializer.setIncludeAccessInfo(add != null && add.contains("access"));
		jsonPlaceSerializer.setIncludeChangeHistory(false);
		if (add != null && add.contains("sort")) {
			jsonPlaceSerializer.setLocale(locale);
			jsonPlaceSerializer.setOriginalLocale(originalLocale);
		} else {
			jsonPlaceSerializer.setLocale(null);
			jsonPlaceSerializer.setOriginalLocale(null);
		}

		ModelAndView mav = new ModelAndView("place/list");
		mav.addObject("places", places);
		if (parents.size() > 0)
			mav.addObject("parents", parents);
		mav.addObject("jsonPlaceSerializer", jsonPlaceSerializer);
		mav.addObject("accessStatusMap", accessStatusMap);
		mav.addObject("parentAccessStatusMap", parentAccessStatusMap);
		mav.addObject("groupDao", groupDao);
		mav.addObject("facets", facets);
		mav.addObject("language", locale.getISO3Language());
		mav.addObject("limit", limit);
		mav.addObject("offset", offset);
		mav.addObject("hits", query.getHits());
		mav.addObject("queryId", queryId);
		mav.addObject("placeDao", placeDao);
		mav.addObject("view", view);
		mav.addObject("q", q);
		mav.addObject("googleMapsApiKey", googleMapsApiKey);
		mav.addObject("callback", callback);

		return mav;

	}

	@RequestMapping(value = "/search", method = RequestMethod.POST)
	public ModelAndView extendedSearch(@RequestParam(defaultValue = "10") int limit,
			@RequestParam(defaultValue = "0") int offset, @RequestParam(required = false) String showInReview,
			@RequestBody String jsonQuery, HttpServletRequest request) {

		RequestContext requestContext = new RequestContext(request);
		Locale locale = requestContext.getLocale();

		User user = null;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof User)
			user = (User) principal;

		ElasticSearchPlaceQuery query = new ElasticSearchPlaceQuery(clientProvider.getClient());
		query.extendedSearch(jsonQuery);
		query.limit(limit);
		query.offset(offset);
		query.addBoostForChildren();
		query.addFilter("deleted:false");
		query.addFilter(buildRecordGroupFilter(user));

		if (!"true".equals(showInReview))
			query.addFilter("needsReview:false");

		query.addTermsAggregation("parent");
		query.addTermsAggregation("types");
		query.addTermsAggregation("tags");

		logger.debug("executing extended search with query: {}", jsonQuery);

		// get ids from elastic search
		String[] result = query.execute();

		List<Place> places = placesForList(result, true);
		Map<String, List<String[]>> facets = processAggregations(query, locale);

		Map<String, PlaceAccessService.AccessStatus> accessStatusMap = checkAccess(places);

		ModelAndView mav = new ModelAndView("place/list");
		mav.addObject("places", places);
		mav.addObject("accessStatusMap", accessStatusMap);
		mav.addObject("groupDao", groupDao);
		mav.addObject("facets", facets);
		mav.addObject("baseUri", baseUri);
		mav.addObject("language", locale.getISO3Language());
		mav.addObject("limit", limit);
		mav.addObject("offset", offset);
		mav.addObject("hits", query.getHits());
		mav.addObject("googleMapsApiKey", googleMapsApiKey);
		return mav;

	}

	@RequestMapping(value = "/geoSearch", method = RequestMethod.GET)
	public ModelAndView geoList(@RequestParam(defaultValue = "10") int limit,
			@RequestParam(defaultValue = "0") int offset, @RequestParam double lat, @RequestParam double lon,
			@RequestParam(defaultValue = "50") int distance, @RequestParam(required = false) String filter,
			@RequestParam(required = false) String showInReview, HttpServletRequest request,
			HttpServletResponse response) {

		RequestContext requestContext = new RequestContext(request);
		Locale locale = requestContext.getLocale();
		Locale originalLocale = request.getLocale();

		User user = null;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof User)
			user = (User) principal;

		ElasticSearchPlaceQuery query = new ElasticSearchPlaceQuery(clientProvider.getClient());
		query.geoDistanceSearch(lon, lat, distance);
		query.addGeoDistanceSort(lon, lat);
		query.limit(limit);
		query.offset(offset);
		query.addFilter("deleted:false");
		query.addFilter(buildRecordGroupFilter(user));

		if (!"true".equals(showInReview))
			query.addFilter("needsReview:false");
		query.addTermsAggregation("parent");
		query.addTermsAggregation("types");
		query.addTermsAggregation("tags");

		if (filter != null) {
			query.addFilter(filter);
		}

		// get ids from elastic search
		String[] result = query.execute();
		Map<String, List<String[]>> facets = processAggregations(query, locale);

		logger.debug("Querying index returned: " + result.length + " places");

		List<Place> places = placesForList(result, true);

		Map<String, PlaceAccessService.AccessStatus> accessStatusMap = checkAccess(places);

		jsonPlaceSerializer.setBaseUri(baseUri);
		jsonPlaceSerializer.setPretty(false);
		jsonPlaceSerializer.setIncludeAccessInfo(true);
		jsonPlaceSerializer.setIncludeChangeHistory(false);
		jsonPlaceSerializer.setLocale(locale);
		jsonPlaceSerializer.setOriginalLocale(originalLocale);

		ModelAndView mav = new ModelAndView("place/list");
		mav.addObject("places", places);
		mav.addObject("jsonPlaceSerializer", jsonPlaceSerializer);
		mav.addObject("accessStatusMap", accessStatusMap);
		mav.addObject("groupDao", groupDao);
		mav.addObject("facets", facets);
		mav.addObject("language", locale.getISO3Language());
		mav.addObject("limit", limit);
		mav.addObject("offset", offset);
		mav.addObject("hits", query.getHits());
		mav.addObject("googleMapsApiKey", googleMapsApiKey);

		return mav;

	}

	@RequestMapping(value = "/search/shapefile", method = RequestMethod.GET)
	public void getShapefile(@RequestParam(defaultValue = "1000000") int limit,
			@RequestParam(defaultValue = "0") int offset, @RequestParam(required = false) String q,
			@RequestParam(required = false) String fq, @RequestParam(required = false) String sort,
			@RequestParam(defaultValue = "asc") String order, @RequestParam(required = false) String type,
			@RequestParam(required = false) double[] bbox,
			@RequestParam(required = false) double[] polygonFilterCoordinates, HttpServletRequest request,
			HttpServletResponse response) {

		String pointsFq = fq;
		String multipolygonsFq = fq;

		if (fq == null || fq.length() == 0) {
			pointsFq = "_exists_:prefLocation.coordinates";
			multipolygonsFq = "_exists_:prefLocation.shape";
		} else {
			if (fq.indexOf("_exists_:prefLocation.coordinates") == -1) {
				pointsFq += " AND _exists_:prefLocation.coordinates";
			}
			if (fq.indexOf("_exists_:prefLocation.shape") == -1) {
				multipolygonsFq += " AND _exists_:prefLocation.shape";
			}
		}

		User user = null;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof User)
			user = (User) principal;

		logger.debug("Creating shapefile for query: " + q + ", fq: " + fq + ", limit: " + limit + ", offset: " + offset
				+ ", type: " + type);

		String[] pointsResult = getQuery(q, pointsFq, type, sort, order, null, bbox, polygonFilterCoordinates, limit,
				offset, false, null, user).execute();
		String[] multipolygonsResult = getQuery(q, multipolygonsFq, type, sort, order, null, bbox,
				polygonFilterCoordinates, limit, offset, false, null, user).execute();

		logger.debug("Querying index returned: " + pointsResult.length + " places with point coordinates");
		logger.debug(
				"Querying index returned: " + multipolygonsResult.length + " places with multipolygon coordinates");

		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm");

		File file = null;
		try {
			file = shapefileCreator.createShapefile("iDAIgazetteer_" + dateFormat.format(new Date()),
					new ArrayList<String>(Arrays.asList(pointsResult)),
					new ArrayList<String>(Arrays.asList(multipolygonsResult)), q);
		} catch (Exception e) {
			throw new RuntimeException("Shapefile creation failed", e);
		}

		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Shapefile could not be found", e);
		}

		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());

		try {
			IOUtils.copy(inputStream, response.getOutputStream());
			response.flushBuffer();
		} catch (IOException e) {
			throw new RuntimeException("Failed to copy zipped shapefile to output stream", e);
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				throw new RuntimeException("Failed to close input stream", e);
			}
		}

		try {
			shapefileCreator.removeShapefileData(file);
		} catch (IOException e) {
			throw new RuntimeException("Failed to remove shapefile data", e);
		}
	}

	@RequestMapping(value = "/suggestions", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, List<String>> getSuggestions(@RequestParam String field, @RequestParam String text,
			@RequestParam String queryId) {

		ElasticSearchSuggestionQuery query = new ElasticSearchSuggestionQuery(clientProvider.getClient(), groupDao,
				groupRoleDao);
		List<String> suggestions = query.getSuggestions(field, text, field.equals("suggestionNames.suggest"));

		List<String> queryIdList = new ArrayList<String>();
		queryIdList.add(queryId);

		Map<String, List<String>> resultMap = new HashMap<String, List<String>>();
		resultMap.put("suggestions", suggestions);
		resultMap.put("queryId", queryIdList);

		return resultMap;
	}

	@RequestMapping(value = "/heatmapCoordinates", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, List<String>> getHeatmapCoordinates() {

		List<Place> places = placeDao.findHeatmapPlaces(0,
				new PageRequest(0, 5000, new Sort(Direction.DESC, "children")));

		List<String> heatmapCoordinates = new ArrayList<String>();

		for (Place place : places) {
			if (place.getPrefLocation() != null && place.getPrefLocation().getCoordinates() != null
					&& place.getPrefLocation().getCoordinates().length > 0) {
				heatmapCoordinates.add(String.valueOf(place.getPrefLocation().getLat()));
				heatmapCoordinates.add(String.valueOf(place.getPrefLocation().getLng()));
			}
		}

		Map<String, List<String>> resultMap = new HashMap<String, List<String>>();
		resultMap.put("coordinates", heatmapCoordinates);

		return resultMap;
	}

	private ElasticSearchPlaceQuery getQuery(String q, String fq, String type, String sort, String order, String add,
			double[] bbox, double[] polygonFilterCoordinates, int limit, int offset, boolean showHiddenPlaces,
			String showInReview, User user) {

		ElasticSearchPlaceQuery query = new ElasticSearchPlaceQuery(clientProvider.getClient());

		if (q != null) {
			if (checkQueryAuthorization(q, user)) {
				if ("fuzzy".equals(type))
					query.fuzzySearch(q);
				else if ("queryString".equals(type))
					query.queryStringSearch(q);
				else if ("extended".equals(type))
					query.extendedSearch(q);
				else if ("prefix".equals(type))
					query.prefixSearch(q);
				else
					query.metaSearch(q);
			} else
				query.listAll();
		} else {
			query.listAll();
		}

		if (fq != null && !fq.isEmpty())
			query.addFilter(fq);

		if (!showHiddenPlaces)
			query.addFilter(buildRecordGroupFilter(user));

		query.addBoostForChildren();
		query.limit(limit);
		query.offset(offset);
		if (sort != null && !sort.isEmpty()) {
			query.addSort(sort, order);
		}
		if (add == null || !add.contains("deleted"))
			query.addFilter("deleted:false");
		if (!"true".equals(showInReview))
			query.addFilter("needsReview:false");
		query.addTermsAggregation("parent");
		query.addTermsAggregation("types");
		query.addTermsAggregation("tags");

		if (bbox != null && bbox.length > 0) {
			query.addBBoxFilter(bbox[0], bbox[1], bbox[2], bbox[3]);
		}

		if (polygonFilterCoordinates != null && polygonFilterCoordinates.length > 0) {
			double[][] polygon = new double[polygonFilterCoordinates.length / 2][];

			for (int i = 0; i < polygonFilterCoordinates.length / 2; i++) {
				polygon[i] = new double[2];
				polygon[i][0] = polygonFilterCoordinates[i * 2];
				polygon[i][1] = polygonFilterCoordinates[i * 2 + 1];
			}

			query.addPolygonFilter(polygon);
		}

		return query;
	}

	// get places for the result ids from db
	private List<Place> placesForList(String[] result, boolean includePolygons) {
		List<Place> places = new ArrayList<Place>();
		for (int i = 0; i < result.length; i++) {
			if (includePolygons)
				places.add(placeDao.findOne(result[i]));
			else
				places.add(placeDao.findWithoutPolygon(result[i]));
		}

		return places;
	}

	private Map<String, List<String[]>> processAggregations(ElasticSearchPlaceQuery query, Locale locale) {

		Map<String, List<String[]>> result = new HashMap<String, List<String[]>>();
		List<Aggregation> aggregations = query.getTermsAggregations().asList();

		for (Aggregation aggregation : aggregations) {
			List<String[]> terms = new ArrayList<String[]>();

			for (Bucket bucket : ((Terms) aggregation).getBuckets()) {

				if (aggregation.getName().equals("parent")) {
					Place place = placeDao.findOne(bucket.getKeyAsString());
					if (place == null)
						continue;
					String[] term = new String[3];
					try {
						term[0] = place.getPrefName().getTitle();
					} catch (NullPointerException e) {
						logger.warn("Could not resolve parent name for aggregation. Place: " + place, e);
						continue;
					}
					term[1] = bucket.getKeyAsString();
					term[2] = String.valueOf(bucket.getDocCount());
					terms.add(term);
				} else if (aggregation.getName().equals("types")) {
					String message;
					try {
						message = messageSource.getMessage("place.types." + bucket.getKeyAsString(), null, locale);
					} catch (NoSuchMessageException e) {
						logger.warn("No message for type '" + bucket.getKeyAsString() + "'.", e);
						message = bucket.getKeyAsString();
					}
					String[] term = new String[3];
					term[0] = message;
					term[1] = bucket.getKeyAsString();
					term[2] = String.valueOf(bucket.getDocCount());
					terms.add(term);
				} else {
					String[] term = new String[3];
					term[0] = bucket.getKeyAsString();
					term[1] = bucket.getKeyAsString();
					term[2] = String.valueOf(bucket.getDocCount());
					terms.add(term);
				}
			}

			result.put(aggregation.getName(), terms);
		}

		return result;
	}

	@RequestMapping(value = "/children/{id}", method = RequestMethod.GET)
	public ModelAndView childrenSearch(@PathVariable String id,
			@RequestParam(required = false, defaultValue = "map,table") String view, HttpServletRequest request,
			HttpServletResponse response) {
		ElasticSearchPlaceQuery query = new ElasticSearchPlaceQuery(clientProvider.getClient());
		query.metaSearch("parent: " + id);
		query.addSort("prefName.title.sort", "asc");
		query.addFilter("deleted:false");
		query.limit(1000);
		String[] result = query.execute();

		List<Place> places = placesForList(result, true);
		logger.debug("Places: {}", places);

		Map<String, PlaceAccessService.AccessStatus> accessStatusMap = checkAccess(places);

		RequestContext requestContext = new RequestContext(request);
		Locale locale = requestContext.getLocale();
		Locale originalLocale = request.getLocale();

		jsonPlaceSerializer.setBaseUri(baseUri);
		jsonPlaceSerializer.setPretty(false);
		jsonPlaceSerializer.setIncludeAccessInfo(false);
		jsonPlaceSerializer.setIncludeChangeHistory(false);
		jsonPlaceSerializer.setLocale(locale);
		jsonPlaceSerializer.setOriginalLocale(originalLocale);

		ModelAndView mav = new ModelAndView("place/list");
		mav.addObject("places", places);
		mav.addObject("jsonPlaceSerializer", jsonPlaceSerializer);
		mav.addObject("accessStatusMap", accessStatusMap);
		mav.addObject("groupDao", groupDao);
		mav.addObject("placeDao", placeDao);
		mav.addObject("view", view);
		mav.addObject("language", locale.getISO3Language());
		mav.addObject("googleMapsApiKey", googleMapsApiKey);

		return mav;
	}

	private void createParentsList(Place place, List<Place> parents, boolean includePolygons) {
		logger.warn("create parent list for place " + place.getId());
		if (place.getParent() != null && !place.getParent().isEmpty()) {
			Place parent = null;
			if (includePolygons)
				parent = placeDao.findOne(place.getParent());
			else
				parent = placeDao.findWithoutPolygon(place.getParent());
			if (parent != null) {
				parents.add(parent);
				createParentsList(parent, parents, includePolygons);
			}
		}
	}

	private boolean checkQueryAuthorization(String q, User user) {

		if (q.contains("groupInternalData")) {
			if (q.contains("groupInternalData.groupId")) {
				int start = q.indexOf("groupInternalData.groupId\":\"") + 28;
				int end = q.indexOf("\"}", start);
				String groupId = q.substring(start, end);
				if (user != null && groupRoleDao.findByGroupIdAndUserId(groupId, user.getId()) != null)
					return true;
				else
					return false;
			} else
				return false;
		} else
			return true;
	}

	private String buildRecordGroupFilter(User user) {

		String recordGroupFilter = "_missing_:recordGroupId";

		Set<String> groupIds = new HashSet<String>();
		List<RecordGroup> showPlacesGroups = groupDao.findByShowPlaces(true);
		for (RecordGroup group : showPlacesGroups) {
			groupIds.add(group.getId());
		}

		if (user != null) {
			List<GroupRole> groupRoles = groupRoleDao.findByUserId(user.getId());
			for (GroupRole role : groupRoles) {
				groupIds.add(role.getGroupId());
			}
		}

		if (groupIds.size() > 0) {
			boolean first = true;
			recordGroupFilter += " OR recordGroupId:(";
			for (String groupId : groupIds) {
				if (first)
					first = false;
				else
					recordGroupFilter += " OR ";
				recordGroupFilter += groupId;
			}
			recordGroupFilter += ")";
		}

		return recordGroupFilter;
	}

	private Map<String, PlaceAccessService.AccessStatus> checkAccess(List<Place> places) {

		User user = null;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof User)
			user = (User) principal;

		PlaceAccessService placeAccessService = new PlaceAccessService(groupDao, groupRoleDao);

		Map<String, PlaceAccessService.AccessStatus> accessStatusMap = new HashMap<String, PlaceAccessService.AccessStatus>();

		for (Place place : places) {
			PlaceAccessService.AccessStatus accessStatus = placeAccessService.getAccessStatus(place);
			protectLocationsService.protectLocations(user, place, accessStatus);
			accessStatusMap.put(place.getId(), accessStatus);
		}

		return accessStatusMap;
	}
}
