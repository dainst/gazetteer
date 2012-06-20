package org.dainst.gazetteer;

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

		Place place = new Place();
		place.addName(new PlaceName("Timbuktu","de"));
		place.addName(new PlaceName("Timbuctoo","en"));
		place.addLocation(new Location(50.0, 50.0));
		place.addLocation(new Location(-50.0, -50.0));
		place.addLocation(new Location(0, 0));

		placeDao.save(place);
		
		logger.info("saved place");

		return "home";
	}

}
