package org.dainst.gazetteer.harvest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.Identifier;
import org.dainst.gazetteer.domain.Link;
import org.dainst.gazetteer.domain.Location;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceName;
import org.dainst.gazetteer.helpers.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PleiadesDBHarvester implements Harvester {

	private static Logger logger = LoggerFactory.getLogger(PleiadesDBHarvester.class);

	private ResultSet resultSet;

	private Connection connection;
	
	private IdGenerator idGenerator;
	
	private PlaceRepository placeDao;
	
	public final static Map<String,String> LANGUAGES;
	static {
		Map<String, String> languages = new HashMap<String,String>();
		languages.put("la", "lat");
		languages.put("en", "eng");
		languages.put("it", "ita");
		languages.put("tr", "tur");
		languages.put("ar", "ara");
		languages.put("el", "ell");
		languages.put("fr", "fra");
		languages.put("de", "deu");
		languages.put("es", "spa");
		LANGUAGES = Collections.unmodifiableMap(languages);
	}

	@Override
	public void harvest(Date date) {
		
		try {

			String userName = "root";
			String password = "";
			String url = "jdbc:mysql://localhost/pleiades";
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			connection = DriverManager.getConnection(url, userName, password);
			logger.info("Database connection established");

			PreparedStatement statement = connection.prepareStatement("SELECT * FROM places LEFT JOIN plus ON places.id = plus.pid");
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
				
				Place place = new Place();
				List<PlaceName> names = new ArrayList<PlaceName>();
				
				String title = resultSet.getString("title");
				if (title != null && !title.isEmpty() && !"Untitled".equals(title)
						&& !title.contains("Unnamed")) {
					names.add(new PlaceName(title));
				}
				
				Location location = null;
				double lon = resultSet.getDouble("reprLong");
				double lat = resultSet.getDouble("reprLat");
				if (lat > 0 || lon > 0) {
					location = new Location(lon, lat);
					String precision = resultSet.getString("locationPrecision");
					if ("precise".equals(precision))
						location.setConfidence(3);
					else if ("rough".equals(precision))
						location.setConfidence(2);
					else if ("related".equals(precision))
						location.setConfidence(1);
					place.setPrefLocation(location);
				}
				
				Identifier pleiadesId = new Identifier(resultSet.getString("id"), "pleiades");
				place.addIdentifier(pleiadesId);
				Link link = new Link();
				link.setObject("http://pleiades.stoa.org" + resultSet.getString("path"));
				link.setPredicate("owl:sameAs");
				place.addLink(link);
				String geonamesId = resultSet.getString("geonamesid");
				if (geonamesId != null && !geonamesId.isEmpty() && !"0".equals(geonamesId)) {
					place.addIdentifier(new Identifier(geonamesId, "geonames"));
					Link link2 = new Link();
					link2.setObject("https://sws.geonames.org/" + geonamesId);
					link2.setPredicate("owl:sameAs");
					place.addLink(link2);
				}
				
				PreparedStatement namesStmt = connection.prepareStatement("SELECT * FROM names WHERE pid = ?");
				namesStmt.setInt(1, resultSet.getInt("id"));
				ResultSet namesRslt = namesStmt.executeQuery();
				
				while (namesRslt.next()) {
					String nameAttested = namesRslt.getString("nameAttested");
					String nameLanguage = namesRslt.getString("nameLanguage");
					if (LANGUAGES.containsKey(nameLanguage))
						nameLanguage = LANGUAGES.get(nameLanguage);
					String timePeriods = namesRslt.getString("timePeriods");
					boolean ancient = true;
					if (timePeriods.contains("M"))
						ancient = false;
					if (nameAttested != null && !nameAttested.isEmpty()) {
						PlaceName name = new PlaceName(nameAttested);
						if (nameLanguage != null && !nameLanguage.isEmpty())
							name.setLanguage(nameLanguage);
						name.setAncient(ancient);
						names.add(name);
					}
					String nameTransliterated = namesRslt.getString("nameTransliterated");
					if (nameTransliterated != null && !nameTransliterated.isEmpty()) {
						PlaceName name = new PlaceName(nameTransliterated);
						if (nameLanguage != null && !nameLanguage.isEmpty())
							name.setLanguage(nameLanguage);
						name.setAncient(ancient);
						names.add(name);
					}
				}
				
				if (!names.isEmpty())
					place.setPrefName(names.remove(0));
				while (!names.isEmpty())
					if (!names.get(0).equals(place.getPrefName()))
						place.addName(names.remove(0));
					else
						names.remove(0);
				
				place.setId(idGenerator.generate(place));
				place.setNeedsReview(true);
				placeDao.save(place);
				
				logger.debug("created place: {}", place);
				
			}
			
		} catch(SQLException e) {
			throw new RuntimeException("Error while getting next place from database", e);
		}

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

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
