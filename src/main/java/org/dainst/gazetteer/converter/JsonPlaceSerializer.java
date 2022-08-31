package org.dainst.gazetteer.converter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dainst.gazetteer.dao.GroupRoleRepository;
import org.dainst.gazetteer.dao.PlaceChangeRecordRepository;
import org.dainst.gazetteer.dao.RecordGroupRepository;
import org.dainst.gazetteer.dao.UserRepository;
import org.dainst.gazetteer.domain.Comment;
import org.dainst.gazetteer.domain.GroupRole;
import org.dainst.gazetteer.domain.GroupInternalData;
import org.dainst.gazetteer.domain.Identifier;
import org.dainst.gazetteer.domain.Link;
import org.dainst.gazetteer.domain.Location;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceChangeRecord;
import org.dainst.gazetteer.domain.PlaceName;
import org.dainst.gazetteer.domain.RecordGroup;
import org.dainst.gazetteer.domain.User;
import org.dainst.gazetteer.helpers.LanguagesHelper;
import org.dainst.gazetteer.helpers.PlaceAccessService;
import org.dainst.gazetteer.helpers.PlaceNameHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.RequestContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class JsonPlaceSerializer {
	
	private final static Logger logger = LoggerFactory.getLogger("org.dainst.gazetteer.JsonPlaceSerializer");
	
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	private String baseUri;
	
	private ObjectMapper mapper;

	private boolean includeAccessInfo = false;
	private boolean includeChangeHistory = false;
	private boolean pretty = false;
	private boolean useShortLanguageCodes = false;
	private Locale locale = null;
	private Locale originalLocale = null;
	
	
	@Autowired
	private UserRepository userDao;
	
	@Autowired
	private PlaceChangeRecordRepository changeRecordDao;
	
	@Autowired
	private RecordGroupRepository groupDao;
	
	@Autowired
	private GroupRoleRepository groupRoleDao;
	
	@Autowired
	private LanguagesHelper languagesHelper;
	
	public String serializeIndexSource(Place place) {
		return serialize(place, null, null, PlaceAccessService.AccessStatus.EDIT, null, null, true);
	}
	
	public String serialize(Place place, PlaceAccessService.AccessStatus accessStatus) {
		return serialize(place, null, null, accessStatus, null, null, false);
	}
	
	public String serialize(Place place, List<Place> parents, PlaceAccessService.AccessStatus accessStatus,
			Map<String, PlaceAccessService.AccessStatus> parentAccessStatusMap) {
		return serialize(place, null, parents, accessStatus, parentAccessStatusMap, null, false);
	}
	
	public String serialize(Place place, HttpServletRequest request, List<Place> parents, PlaceAccessService.AccessStatus accessStatus, 
			Map<String, PlaceAccessService.AccessStatus> parentAccessStatusMap, String replacing, boolean asIndexSource) {
		
		resetMapper();
		
		ObjectNode placeNode = createJsonNodes(place, request, parents, accessStatus, parentAccessStatusMap, replacing, asIndexSource);
		
		try {
			return mapper.writeValueAsString(placeNode);
		} catch (Exception e) {
			logger.error("Unable to serialize place in JSON.", e);
			return "";
		}
	}
	
	public String serializeGeoJson(Place place, HttpServletRequest request,
			PlaceAccessService.AccessStatus accessStatus) {
		
		resetMapper();
		
		ObjectNode geoJsonPlaceNode = createGeoJsonNodes(place, accessStatus);		
		ObjectNode placeNode = createJsonNodes(place, request, null, accessStatus, null, null, false);
		geoJsonPlaceNode.put("properties", placeNode);
		
		try {
			return mapper.writeValueAsString(geoJsonPlaceNode);
		} catch (Exception e) {
			logger.error("Unable to serialize place in GeoJSON.", e);
			return "";
		}
	}
		
	private ObjectNode createJsonNodes(Place place, HttpServletRequest request, List<Place> parents,
			PlaceAccessService.AccessStatus accessStatus,
			Map<String, PlaceAccessService.AccessStatus> parentAccessStatusMap, String replacing,
			boolean asIndexSource) {
		
		if (place == null) return null;
		
		ObjectNode placeNode = mapper.createObjectNode();
		logger.debug("serializing: {}", place);
		if (!asIndexSource && place.getId() != null) {
			placeNode.put("@id", baseUri + "place/" + place.getId());
			placeNode.put("gazId", place.getId());
		}
		
		if (asIndexSource)
			placeNode.put("needsReview", place.isNeedsReview());
		
		// record group
		if (asIndexSource) {
			if (place.getRecordGroupId() != null && !place.getRecordGroupId().isEmpty())
				placeNode.put("recordGroupId", place.getRecordGroupId());
			else
				placeNode.put("recordGroupId", "none");
		} else if (place.getRecordGroupId() != null && !place.getRecordGroupId().isEmpty() && !place.isDeleted()) {
			RecordGroup group = groupDao.findById(place.getRecordGroupId()).orElse(null);
			if (group != null) {
				ObjectNode groupNode = mapper.createObjectNode();
				groupNode.put("id", group.getId());
				groupNode.put("name", group.getName());
				placeNode.put("recordGroup", groupNode);
			}
		}

		if (place.isDeleted()) {
			placeNode.put("deleted", true);
			if (place.getReplacedBy() != null && !place.getReplacedBy().isEmpty())
				placeNode.put("replacedBy", place.getReplacedBy());
			return placeNode;
		} else if (asIndexSource) {
			placeNode.put("deleted", false);
		}
		
		if (!PlaceAccessService.hasReadAccess(accessStatus)) {
			placeNode.put("accessDenied", true);
			return placeNode;
		}
		
		// replacing
		if (replacing != null && !replacing.isEmpty())
			placeNode.put("replacing", replacing);
				
		// parent
		if (place.getParent() != null && !place.getParent().isEmpty()) {
			if (asIndexSource)
				placeNode.put("parent", place.getParent());
			else
				placeNode.put("parent", baseUri + "place/" + place.getParent());
		}
		
		// related places
		if (!place.getRelatedPlaces().isEmpty()) {
			ArrayNode relatedPlacesNode = mapper.createArrayNode();
			for (String relatedPlaceId : place.getRelatedPlaces()) {
				if (relatedPlaceId != null) {
					if (asIndexSource)
						relatedPlacesNode.add(relatedPlaceId);
					else
						relatedPlacesNode.add(baseUri + "place/" + relatedPlaceId);
				}
			}
			placeNode.put("relatedPlaces", relatedPlacesNode);
		}
		
		// ancestors
		if (place.getAncestors() != null && !place.getAncestors().isEmpty()) {
			ArrayNode ancestorsNode = mapper.createArrayNode();
			for (String ancestorId : place.getAncestors()) {
				if (asIndexSource)
					ancestorsNode.add(ancestorId);
				else
					ancestorsNode.add(baseUri + "place/" + ancestorId);
			}
			placeNode.put("ancestors", ancestorsNode);
		}
		
		if (asIndexSource)
			placeNode.put("children", place.getChildren());
		
		// place types
		if (place.getTypes() != null && !place.getTypes().isEmpty()) {
			ArrayNode typesNode = mapper.createArrayNode();
			for (String type : place.getTypes())
				typesNode.add(type);
			placeNode.put("types", typesNode);
		}
		
		List<String> nameSuggestions = new ArrayList<String>();
		
		// preferred name
		if (place.getPrefName() != null) {
			ObjectNode prefNameNode = mapper.createObjectNode();
			prefNameNode.put("title", place.getPrefName().getTitle());
			if (place.getPrefName().getLanguage() != null)
				prefNameNode.put("language", getLanguageCode(place.getPrefName().getLanguage()));
			if (place.getPrefName().isAncient())
				prefNameNode.put("ancient", true);
			if (place.getPrefName().isTransliterated())
				prefNameNode.put("transliterated", true);
			placeNode.put("prefName", prefNameNode);
			nameSuggestions.add(place.getPrefName().getTitle());
		}

		// get sorted place names if locale is set
		List<PlaceName> sortedPlaceNames = null;
		if (!asIndexSource && locale != null && originalLocale != null) {
			PlaceNameHelper placeNameHelper = new PlaceNameHelper();
			placeNameHelper.setLocale(locale);
			placeNameHelper.setOriginalLocale(originalLocale);
			placeNameHelper.setLanguagesHelper(languagesHelper);
			sortedPlaceNames = placeNameHelper.sortPlaceNames(place.getNames());
		}			
		
		// other names
		if (!place.getNames().isEmpty()) {
			ArrayNode namesNode = mapper.createArrayNode();
			for (PlaceName name : place.getNames()) {
				ObjectNode nameNode = mapper.createObjectNode();
				nameNode.put("title", name.getTitle());
				if (name.getLanguage() != null)
					nameNode.put("language", getLanguageCode(name.getLanguage()));
				if (name.isAncient())
					nameNode.put("ancient", true);
				if (name.isTransliterated())
					nameNode.put("transliterated", true);
				if (sortedPlaceNames != null)
					nameNode.put("sort", sortedPlaceNames.indexOf(name));
				namesNode.add(nameNode);
				if (!nameSuggestions.contains(name.getTitle()))
					nameSuggestions.add(name.getTitle());
			}
			placeNode.put("names", namesNode);
		}
		
		// name suggestions
		if (asIndexSource && !place.isDeleted() && !place.isNeedsReview()) {
			ArrayNode nameSuggestionsNode = mapper.createArrayNode();
			for (String name : nameSuggestions) {
				nameSuggestionsNode.add(name);
			}
			placeNode.put("nameSuggestions", nameSuggestionsNode);
		}
		
		// preferred location
		if (place.getPrefLocation() != null) {
			ObjectNode locationNode = mapper.createObjectNode();
			if (place.getPrefLocation().getCoordinates() != null) {
				ArrayNode coordinatesNode = mapper.createArrayNode();
				coordinatesNode.add(place.getPrefLocation().getLng());
				coordinatesNode.add(place.getPrefLocation().getLat());
				locationNode.put("coordinates", coordinatesNode);
			}
			if (place.getPrefLocation().getShape() != null) {
				ArrayNode shapeCoordinatesNode = createPolygonCoordinatesNode(place.getPrefLocation().getShape().getCoordinates());
				if (asIndexSource) {
					ObjectNode shapeNode = mapper.createObjectNode();
					shapeNode.put("type", "multipolygon");
					shapeNode.put("coordinates", shapeCoordinatesNode);
					locationNode.put("shape", shapeNode);
				} else {
					locationNode.put("shape", shapeCoordinatesNode);
				}
			}
			if (place.getPrefLocation().getAltitude() != null)
				locationNode.put("altitude", place.getPrefLocation().getAltitude());
			locationNode.put("confidence", place.getPrefLocation().getConfidence());
			locationNode.put("publicSite", place.getPrefLocation().isPublicSite());
			placeNode.put("prefLocation", locationNode);
		}
		
		// other locations
		if (!place.getLocations().isEmpty()) {
			ArrayNode locationsNode = mapper.createArrayNode();
			for (Location location : place.getLocations()) {
				ObjectNode locationNode = mapper.createObjectNode();
				if (location.getCoordinates() != null) {
					ArrayNode coordinatesNode = mapper.createArrayNode();
					coordinatesNode.add(location.getLng());
					coordinatesNode.add(location.getLat());
					locationNode.put("coordinates", coordinatesNode);
				}
				if (location.getShape() != null) {
					ArrayNode shapeCoordinatesNode = createPolygonCoordinatesNode(location.getShape().getCoordinates());
					if (asIndexSource) {
						ObjectNode shapeNode = mapper.createObjectNode();
						shapeNode.put("type", "multipolygon");
						shapeNode.put("coordinates", shapeCoordinatesNode);
						locationNode.put("shape", shapeNode);
					} else {
						locationNode.put("shape", shapeCoordinatesNode);
					}
				}
				if (location.getAltitude() != null)
					locationNode.put("altitude", location.getAltitude());
				locationNode.put("confidence", location.getConfidence());
				locationNode.put("publicSite", location.isPublicSite());
				locationsNode.add(locationNode);
			}
			placeNode.put("locations", locationsNode);
		}
		
		if (place.isUnlocatable())
			placeNode.put("unlocatable", true);				
		
		// identifiers
		if (!place.getIdentifiers().isEmpty()) {
			ArrayNode idsNode = mapper.createArrayNode();
			for (Identifier id : place.getIdentifiers()) {
				ObjectNode idNode = mapper.createObjectNode();
				idNode.put("value", id.getValue());
				idNode.put("context", id.getContext());
				idsNode.add(idNode);
			}
			if (asIndexSource)
				placeNode.put("ids", idsNode);
			else
				placeNode.put("identifiers", idsNode);
		}
		
		// links
		if (!place.getLinks().isEmpty()) {
			ArrayNode linksNode = mapper.createArrayNode();
			for (Link link : place.getLinks()) {
				ObjectNode linkNode = mapper.createObjectNode();
				linkNode.put("object", link.getObject());
				linkNode.put("predicate", link.getPredicate());
				if (link.getDescription() != null)
					linkNode.put("description", link.getDescription());
				linksNode.add(linkNode);
			}
			placeNode.put("links", linksNode);
		}
		
		// comments
		if (!place.getComments().isEmpty()) {
			ArrayNode commentsNode = mapper.createArrayNode();
			for (Comment comment : place.getComments()) {
				ObjectNode commentNode = mapper.createObjectNode();
				commentNode.put("text", comment.getText());
				if (comment.getLanguage() != null && !comment.getLanguage().isEmpty())
					commentNode.put("language", getLanguageCode(comment.getLanguage()));
				commentsNode.add(commentNode);
			}
			placeNode.put("comments", commentsNode);
		}
		
		// tags
		if (!place.getTags().isEmpty()) {
			ArrayNode tagsNode = mapper.createArrayNode();
			for (String tag : place.getTags()) {
				tagsNode.add(tag);
			}
			placeNode.put("tags", tagsNode);
		}
		
		// provenance
		if (!place.getProvenance().isEmpty()) {
			ArrayNode provenanceNode = mapper.createArrayNode();
			for (String provenanceEntry : place.getProvenance()) {
				provenanceNode.add(provenanceEntry);
			}
			placeNode.put("provenance", provenanceNode);
		}
		
		// reisestipendium content		
		logger.debug("serializing reisestipendium content?");
		User user = getUser();
		if (asIndexSource || (user != null && user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_REISESTIPENDIUM")))) {
			
			logger.debug("serializing reisestipendium note");
			if (place.getNoteReisestipendium() != null && !place.getNoteReisestipendium().isEmpty()) {
				placeNode.put("noteReisestipendium", place.getNoteReisestipendium());
				logger.debug("serialized reisestipendium note");
			}
				if (!place.getCommentsReisestipendium().isEmpty()) {
				ArrayNode commentsNode = mapper.createArrayNode();
				for (Comment comment : place.getCommentsReisestipendium()) {
					ObjectNode commentNode = mapper.createObjectNode();
					commentNode.put("text", comment.getText());
					commentNode.put("language", getLanguageCode(comment.getLanguage()));
					commentNode.put("user", comment.getUser());
					commentsNode.add(commentNode);
				}
				placeNode.put("commentsReisestipendium", commentsNode);
			}
		}
		
		// record group internal data
		if (asIndexSource || (user != null && !place.getGroupInternalData().isEmpty())) {
			logger.debug("serializing record group internal data");
			ArrayNode groupInternalDataNode = mapper.createArrayNode();
			for (GroupInternalData data : place.getGroupInternalData()) {
				if (asIndexSource) {
					ObjectNode dataNode = mapper.createObjectNode();
					dataNode.put("text", data.getText());
					dataNode.put("groupId", data.getGroupId());
					groupInternalDataNode.add(dataNode);
				} else {
					GroupRole role = groupRoleDao.findByGroupIdAndUserId(data.getGroupId(), user.getId());
					if (role != null) {
						ObjectNode dataNode = mapper.createObjectNode();
						dataNode.put("text", data.getText());
						ObjectNode groupNode = mapper.createObjectNode();
						groupNode.put("id", data.getGroupId());
						groupNode.put("name", groupDao.findById(data.getGroupId()).orElse(null).getName());
						dataNode.put("recordGroup", groupNode);
						groupInternalDataNode.add(dataNode);
					}
				}
			}
			if (groupInternalDataNode.size() > 0)
				placeNode.put("groupInternalData", groupInternalDataNode);
		}
		
		// last change date
		if (place.getLastChangeDate() != null && asIndexSource) {
			placeNode.put("lastChangeDate", dateFormat.format(place.getLastChangeDate()));
		}
		
		// read access
		if (includeAccessInfo && accessStatus.equals(PlaceAccessService.AccessStatus.LIMITED_READ))
			placeNode.put("limitedReadAccess", true);
		
		// edit access
		if (includeAccessInfo && !PlaceAccessService.hasEditAccess(accessStatus))
			placeNode.put("editAccessDenied", true);
		
		// change history
		if (includeChangeHistory && user != null  && user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_EDITOR")) && userDao != null
				&& changeRecordDao != null && request != null) {
			logger.debug("serializing change history");
			
			List<PlaceChangeRecord> changeHistory = changeRecordDao.findByPlaceId(place.getId());
			
			if (!changeHistory.isEmpty()) {
				
				Collections.sort(changeHistory, new PlaceChangeRecord.ChangeDateComparator());
				ArrayNode changeHistoryNode = mapper.createArrayNode();
								
				for (PlaceChangeRecord changeRecord : changeHistory) {
					ObjectNode changeRecordNode = mapper.createObjectNode();
				
					User changeRecordUser = userDao.findById(changeRecord.getUserId()).orElse(null);
					if (changeRecordUser != null) {
						changeRecordNode.put("username", changeRecordUser.getUsername());
						changeRecordNode.put("userId", changeRecord.getUserId());
					} else {
						RequestContext context = new RequestContext(request);
						changeRecordNode.put("username", context.getMessage("ui.changeHistory.deletedUser"));
						changeRecordNode.put("userId", "");
					}
					
					if (changeRecord.getChangeType() != null)
						changeRecordNode.put("changeType", changeRecord.getChangeType());
					else
						changeRecordNode.put("changeType", "unknown");
					
					if (changeRecord.getAdditionalData() != null)
						changeRecordNode.put("additionalData", changeRecord.getAdditionalData());
				
					DateFormat format = new SimpleDateFormat("dd.MM.yyyy (HH:mm:ss z)");
					changeRecordNode.put("changeDate", format.format(changeRecord.getChangeDate()));
					changeHistoryNode.add(changeRecordNode);
				}
				
				placeNode.put("changeHistory", changeHistoryNode);
			}
		}
		
		// parents list
		if (parents != null && parentAccessStatusMap != null) {
			ArrayNode parentsNode = mapper.createArrayNode();
			
			for (Place parent : parents) {
				ObjectNode parentNode = createJsonNodes(parent, request, null, parentAccessStatusMap.get(parent.getId()), null, replacing, asIndexSource);
				parentsNode.add(parentNode);
			}
			
			placeNode.put("parents", parentsNode);
		}
				
		return placeNode;
	}
	
	private ObjectNode createGeoJsonNodes(Place place, PlaceAccessService.AccessStatus accessStatus) {
	
		if (place == null) return null;		
		
		ObjectNode placeNode = mapper.createObjectNode();
		placeNode.put("type", "Feature");
		placeNode.put("id", baseUri + "place/" + place.getId());
		
		ObjectNode geometryCollectionNode = mapper.createObjectNode();
		geometryCollectionNode.put("type", "GeometryCollection");
		
		ArrayNode geometriesNode = mapper.createArrayNode();
		
		if (!PlaceAccessService.hasReadAccess(accessStatus)) {
			geometryCollectionNode.put("geometries", geometriesNode);
			placeNode.put("geometry", geometryCollectionNode);
			return placeNode;
		}
		
		// preferred location
		if (place.getPrefLocation() != null) {
			if (place.getPrefLocation().getCoordinates() != null) {
				ObjectNode pointNode = mapper.createObjectNode();
				pointNode.put("type", "Point");
				
				ArrayNode coordinatesNode = mapper.createArrayNode();
				coordinatesNode.add(place.getPrefLocation().getLng());
				coordinatesNode.add(place.getPrefLocation().getLat());
				pointNode.put("coordinates", coordinatesNode);
				
				geometriesNode.add(pointNode);
			}
			if (place.getPrefLocation().getShape() != null) {
				ObjectNode polygonNode = mapper.createObjectNode();
				polygonNode.put("type", "MultiPolygon");
				
				ArrayNode coordinatesNode = createPolygonCoordinatesNode(place.getPrefLocation().getShape().getCoordinates());
				polygonNode.put("coordinates", coordinatesNode);
				
				geometriesNode.add(polygonNode);
			}
		}

		// other locations
		if (!place.getLocations().isEmpty()) {
			for (Location location : place.getLocations()) {
				if (location.getCoordinates() != null) {
					ObjectNode pointNode = mapper.createObjectNode();
					pointNode.put("type", "Point");
					
					ArrayNode coordinatesNode = mapper.createArrayNode();
					coordinatesNode.add(location.getLng());
					coordinatesNode.add(location.getLat());					
					pointNode.put("coordinates", coordinatesNode);
					
					geometriesNode.add(pointNode);
				}
				if (location.getShape() != null) {
					ObjectNode polygonNode = mapper.createObjectNode();
					polygonNode.put("type", "MultiPolygon");
										
					ArrayNode coordinatesNode = createPolygonCoordinatesNode(location.getShape().getCoordinates());
					polygonNode.put("coordinates", coordinatesNode);
					
					geometriesNode.add(polygonNode);
				}
			}
		}
		
		geometryCollectionNode.put("geometries", geometriesNode);
		placeNode.put("geometry", geometryCollectionNode);
		
		return placeNode;
	}
	
	private User getUser() {
		User user = null;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof User)
			user = (User) principal;
		
		return user;
	}

	private ArrayNode createPolygonCoordinatesNode(double[][][][] polygonCoordinates) {
		ArrayNode coordinatesNode = mapper.createArrayNode();
		
		for (int i = 0; i < polygonCoordinates.length; i++) {
			ArrayNode shapeCoordinatesNode1 = mapper.createArrayNode();
			
			for (int j = 0; j < polygonCoordinates[i].length; j++) {
				ArrayNode shapeCoordinatesNode2 = mapper.createArrayNode();
				
				for (int k = 0; k < polygonCoordinates[i][j].length; k++) {
					ArrayNode shapeCoordinatesNode3 = mapper.createArrayNode();
					
					for (int l = 0; l < polygonCoordinates[i][j][k].length; l++) {
						shapeCoordinatesNode3.add(polygonCoordinates[i][j][k][l]);
					}
					shapeCoordinatesNode2.add(shapeCoordinatesNode3);
				}
				shapeCoordinatesNode1.add(shapeCoordinatesNode2);
			}
			coordinatesNode.add(shapeCoordinatesNode1);
		}
		
		return coordinatesNode;
	}
	
	private String getLanguageCode(String iso3LanguageCode) {
		
		return useShortLanguageCodes
			? languagesHelper.getLocaleForISO3Language(iso3LanguageCode).getLanguage()
			: iso3LanguageCode; 
	}
	
	private void resetMapper() {
		
		mapper = new ObjectMapper();
		
		if (pretty)
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
	}
	
	public String getBaseUri() {
		return baseUri;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public boolean getIncludeAccessInfo() {
		return includeAccessInfo;
	}

	public void setIncludeAccessInfo(boolean includeAccessInfo) {
		this.includeAccessInfo = includeAccessInfo;
	}

	public boolean getIncludeChangeHistory() {
		return includeChangeHistory;
	}

	public void setIncludeChangeHistory(boolean includeChangeHistory) {
		this.includeChangeHistory = includeChangeHistory;
	}
	
	public boolean isPretty() {
		return pretty;
	}

	public void setPretty(boolean pretty) {
		this.pretty = pretty;
	}
	
	public boolean getUseShortLanguageCodes() {
		return useShortLanguageCodes;
	}
	
	public void setUseShortLanguageCodes(boolean useShortLanguageCodes) {
		this.useShortLanguageCodes = useShortLanguageCodes;
	}
	
	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public Locale getOriginalLocale() {
		return originalLocale;
	}

	public void setOriginalLocale(Locale originalLocale) {
		this.originalLocale = originalLocale;
	}
}
