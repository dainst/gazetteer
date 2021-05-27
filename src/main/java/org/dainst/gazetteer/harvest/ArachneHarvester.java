package org.dainst.gazetteer.harvest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.Identifier;
import org.dainst.gazetteer.domain.Link;
import org.dainst.gazetteer.domain.Location;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceName;
import org.dainst.gazetteer.helpers.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArachneHarvester implements Harvester {

	private static Logger logger = LoggerFactory.getLogger(ArachneHarvester.class);
	
	public final static List<String> SKIP_TITLES = Arrays.asList(
		"unbekannt",
		"Privatbesitz",
		"Kunsthandel",
		"verschollen",
		"unbekannt/verschollen"
	);

	private ResultSet resultSet;

	private Connection connection;

	private PreparedStatement statement;
	
	private IdGenerator idGenerator;
	
	private PlaceRepository placeDao;

	@Override
	public void harvest(Date date) {

		try {

			String userName = "";
			String password = "";
			String url = "jdbc:mysql://arachne.uni-koeln.de/arachne";
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			connection = DriverManager.getConnection(url, userName, password);
			logger.info("Database connection established");

			statement = connection.prepareStatement("SELECT * FROM ort "
					+ "LEFT JOIN arachneentityidentification ON PS_OrtID = ForeignKey "
					+ "WHERE TableName LIKE 'ort' AND ort.lastModified > ? "
					+ "AND (Gazetteerid IS NULL OR Gazetteerid = 0)");
			if (date != null)
				statement.setTimestamp(1, new Timestamp(date.getTime()));
			else
				statement.setTimestamp(1, new Timestamp(0));
			logger.debug(statement.toString());	        
			resultSet = statement.executeQuery();
			if (resultSet != null) logger.info("Retrieved results.");
			else logger.info("Nothing to harvest");

		} catch (Exception e) {
			throw new RuntimeException("Error while harvesting.", e);
		}

		try {

			// try until at least one place can be constructed from row
			// (some rows will construct no place at all, e.g. "unbekannt" ...)
			while (resultSet.next()) {

				logger.debug("got next row: {}", resultSet.getString("PS_OrtID"));

				Place repository = null, city = null, country = null, continent = null;
				List<Place> result = new ArrayList<Place>();

				String repositoryTitle = normalize(resultSet.getString("Aufbewahrungsort"));
				logger.debug("got repository title: {}", repositoryTitle);
				String cityTitle = normalize(resultSet.getString("Stadt"));
				logger.debug("got city title: {}", cityTitle);
				String ancientTitle = normalize(resultSet.getString("Ort_antik"));
				logger.debug("got ancient title: {}", ancientTitle);
				String countryTitle = normalize(resultSet.getString("Land"));
				logger.debug("got country title: {}", countryTitle);
				String continentTitle = normalize(resultSet.getString("continent"));
				logger.debug("got continent title: {}", continentTitle);

				String confidence = normalize(resultSet.getString("Genauigkeit"));

				// create location object that will be assigned to the
				// most granular place generated from this row
				Location location = null;
				double lon = resultSet.getDouble("Longitude");
				double lat = resultSet.getDouble("Latitude");
				if (lat > 0 || lon > 0) {
					location = new Location(lon, lat);
					location.setConfidence(3);
				}

				logger.debug("created location");

				// create ids for most granular place
				Identifier arachnePlaceId = new Identifier(resultSet.getString("PS_OrtID"), "arachne-place");
				Identifier arachneEntityId = new Identifier(resultSet.getString("ArachneEntityID"), "arachne-entity");
				String arachneUri = "http://arachne.uni-koeln.de/entity/" + arachneEntityId.getValue();
				Identifier geonamesId = null;
				String geonamesIdValue = normalize(resultSet.getString("geonamesid"));
				if (!geonamesIdValue.isEmpty() && !"0".equals(geonamesIdValue)) {
					geonamesId = new Identifier(geonamesIdValue, "geonames");
				}

				logger.debug("created ids");

				// create place for Aufbewahrungsort
				if (!repositoryTitle.isEmpty()) {

					Identifier repoGeonamesId = null;
					Location repoLocation = null;
					if ("exakt".equals(confidence) || "genau".equals(confidence)) {
						repoGeonamesId = geonamesId;
						geonamesId = null;
						// location is the exact location of repository
						repoLocation = location;
						location = null;
					} else if (location != null) {
						// location is the location of the city (lower confidence)
						repoLocation = new Location();
						repoLocation.setCoordinates(location.getCoordinates());
						repoLocation.setConfidence(1);
					}

					repository = createPlace(
							repositoryTitle, repoLocation, arachnePlaceId,
							arachneEntityId, arachneUri, repoGeonamesId
							);
					
					if (repository != null) {
						
						String alternativeTitle = normalize(resultSet.getString("Aufbewahrungsort_synonym"));
						if (!alternativeTitle.isEmpty() && !alternativeTitle.equals(repositoryTitle)) {
							PlaceName name = new PlaceName(alternativeTitle);
							repository.addName(name);
						}
						
						arachnePlaceId = null;
						arachneEntityId = null;
						arachneUri = null;
						
					}

				}

				logger.debug("created Aufbewahrungsort: {}", repository);

				// create place for Stadt
				if (!cityTitle.isEmpty() || !ancientTitle.isEmpty()) {
					
					city = createPlace(
							cityTitle, location, arachnePlaceId,
							arachneEntityId, arachneUri, geonamesId
							);
					
					if (city != null) {
						
						if (!ancientTitle.isEmpty()) {
							PlaceName name = new PlaceName(ancientTitle);
							name.setAncient(true);
							if (city.getPrefName().getTitle() == null
									|| city.getPrefName().getTitle().isEmpty()) {
								city.setPrefName(name);
								city.getTypes().clear();
							} else {
								city.addName(name);
							}
						}
						String alternativeTitle = normalize(resultSet.getString("Stadt_synonym"));
						if (!alternativeTitle.isEmpty()) {
							PlaceName name = new PlaceName(alternativeTitle);
							city.addName(name);
						}
						city.getTypes().add("populated-place");
						city.setChildren(1);
						
						location = null;
						arachnePlaceId = null;
						arachneEntityId = null;
						arachneUri = null;
						geonamesId = null;
						
					}

				}

				logger.debug("created Stadt: {}", city);

				// create place for Land
				if (!countryTitle.isEmpty()) {

					country = createPlace(
							countryTitle, location, arachnePlaceId,
							arachneEntityId, arachneUri, geonamesId
							);
					
					if (country != null) {
						
						String isoCodeValue = normalize(resultSet.getString("Countrycode"));
						if (!isoCodeValue.isEmpty()) {
							Identifier isoCode = new Identifier(isoCodeValue, "ISO 3166-1 alpha-2");
							country.addIdentifier(isoCode);
						}
						country.getTypes().add("administrative-unit");
						country.setChildren(1);

						location = null;
						arachnePlaceId = null;
						arachneEntityId = null;
						arachneUri = null;
						geonamesId = null;
						
					}

				}

				logger.debug("created Land: {}", country);

				// create place for continent
				if (!continentTitle.isEmpty()) {

					continent = createPlace(
							continentTitle, location, arachnePlaceId,
							arachneEntityId, arachneUri, geonamesId
							);
					
					if (continent != null) {
						continent.getTypes().add("continent");
						continent.setChildren(1);
					}

				}

				logger.debug("created continent: {}", continent);

				// create relationships and fill result
				if (repository != null) {
					if (city != null) {
						repository.setParent(city.getId());
					} else if (country != null) {
						repository.setParent(country.getId());
					} else if (continent != null) {
						repository.setParent(continent.getId());
					}
					result.add(0, repository);
				}
				if (city != null) {
					if (country != null) {
						city.setParent(country.getId());
					} else if (continent != null) {
						city.setParent(continent.getId());
					}
					result.add(0, city);
				}
				if (country != null) {
					if (continent != null) {
						country.setParent(continent.getId());
					}
					result.add(0, country);
				}
				if (continent != null) {
					result.add(0, continent);
				}

				logger.debug("created relationships");
				
				// create places in database
				if(!result.isEmpty()) {
					
					for (Place place : result) {
						place.setNeedsReview(true);
						placeDao.save(place);
					}
					
				}

			}

		} catch(SQLException e) {
			throw new RuntimeException("Error while getting next place from database", e);
		}

	}

	private Place createPlace(String title, Location location,
			Identifier arachnePlaceId, Identifier arachneEntityId,
			String arachneUri, Identifier geonamesId) {

		if (SKIP_TITLES.contains(title)) return null;
		
		Place place = new Place();

		place.setPrefName(new PlaceName(title));

		if (location != null) { 
			place.setPrefLocation(location);
			location = null;
		}

		if (arachnePlaceId != null) {
			place.addIdentifier(arachnePlaceId);
			arachnePlaceId = null;
		}

		if (arachneEntityId != null) {
			place.addIdentifier(arachneEntityId);
			arachneEntityId = null;
		}

		if (arachneUri != null) {
			Link link = new Link();
			link.setObject(arachneUri);
			link.setPredicate("owl:sameAs");
			place.addLink(link);
			arachneUri = null;
		}
		
		if (geonamesId != null) {
			Link link = new Link();
			link.setObject("https://sws.geonames.org/" + geonamesId.getValue());
			link.setPredicate("owl:sameAs");
			place.addLink(link);
		}

		if (geonamesId != null) {
			place.addIdentifier(geonamesId);
			geonamesId = null;
		}
		
		place.setId(idGenerator.generate(place));

		return place;

	}

	@Override
	public void close() {
		try {
			if(resultSet != null) resultSet.close();
			if(statement != null) statement.close();
			if(connection != null) connection.close();
		} catch (SQLException e) {
			throw new RuntimeException("Error while closing connection", e);
		}
	}

	private String normalize(String value) {
		if (value == null) return "";
		return value.trim().replaceAll("\\p{Cntrl}", "");
	}

	@Override
	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	@Override
	public void setPlaceRepository(PlaceRepository placeRepository) {
		placeDao = placeRepository;
	}

}
