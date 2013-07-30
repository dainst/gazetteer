package org.dainst.gazetteer.harvest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

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

	@Override
	public void harvest(Date date) {
		
		try {

			String userName = "root";
			String password = "";
			String url = "jdbc:mysql://localhost/pleiades";
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			connection = DriverManager.getConnection(url, userName, password);
			logger.info("Database connection established");

			PreparedStatement statement = connection.prepareStatement("SELECT * FROM places");
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
				PlaceName prefName = null;
				
				String title = resultSet.getString("title");
				if (title != null && !title.isEmpty()) {
					prefName = new PlaceName(title);
					prefName.setAncient(true);
					place.setPrefName(prefName);
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
				place.addLink(link);
				
				PreparedStatement namesStmt = connection.prepareStatement("SELECT * FROM names WHERE pid = ?");
				namesStmt.setInt(1, resultSet.getInt("id"));
				ResultSet namesRslt = namesStmt.executeQuery();
				
				while (namesRslt.next()) {
					String nameAttested = namesRslt.getString("nameAttested");
					String nameLanguage = namesRslt.getString("nameLanguage");
					if (nameAttested != null && !nameAttested.isEmpty()) {
						PlaceName name = new PlaceName(nameAttested);
						if (nameLanguage != null && !nameLanguage.isEmpty())
							name.setLanguage(nameLanguage);
						name.setAncient(true);
						if (!name.equals(prefName))
							place.addName(name);
					}
					String nameTransliterated = namesRslt.getString("nameTransliterated");
					if (nameTransliterated != null && !nameTransliterated.isEmpty()) {
						PlaceName name = new PlaceName(nameTransliterated);
						if (nameLanguage != null && !nameLanguage.isEmpty())
							name.setLanguage(nameLanguage);
						name.setAncient(true);
						if (!name.equals(prefName))
							place.addName(name);
					}
				}
				
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
