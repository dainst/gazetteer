package org.dainst.gazetteer.controller;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.dainst.gazetteer.dao.PlaceDao;
import org.dainst.gazetteer.domain.Location;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	private PlaceDao placeDao;

	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! the client locale is "+ locale.toString());

		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);

		String formattedDate = dateFormat.format(date);

		model.addAttribute("serverTime", formattedDate );

		/*Place place = new Place();
		place.addName(new PlaceName("Timbuktu","de"));
		place.addName(new PlaceName("Timbuctoo","en"));
		place.addLocation(new Location(50.0, 50.0));
		place.addLocation(new Location(-50.0, -50.0));
		place.addLocation(new Location(0, 0));
		placeDao.save(place);		
		logger.info("saved timbuktu");*/
		
		Place place2 = new Place();
		place2.addName(new PlaceName("Köln","de"));
		place2.addName(new PlaceName("Cologne","en"));
		place2.addLocation(new Location(50.937527,6.960268));
		placeDao.save(place2);		
		logger.info("saved cologne");
		
		Place place3 = new Place();
		place3.setParent(place2);
		place3.addName(new PlaceName("Arbeitsstelle für digitale Archäologie","de"));
		place3.addName(new PlaceName("Cologne Digital Archaeology Lab","en"));
		place3.addLocation(new Location(50.925100, 6.925767));
		placeDao.save(place3);		
		logger.info("saved codarchlab");

		return "home";
		
	}

}
