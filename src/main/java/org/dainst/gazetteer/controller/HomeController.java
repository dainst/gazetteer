package org.dainst.gazetteer.controller;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

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

	@RequestMapping(value="/")
	public String home() {
		return "forward:/place";
	}
	
	@RequestMapping(value="/generate", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {

		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);

		String formattedDate = dateFormat.format(date);

		model.addAttribute("serverTime", formattedDate );
		
		/*Place place2 = new Place();
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
		logger.info("saved codarchlab");*/
		
		Random random = new Random();
		for (int i = 0; i < 1000; i++) {
			double d1 = random.nextDouble();
			double d2 = random.nextDouble();
			Place place = new Place();
			place.addName(new PlaceName("Ort " + i,"de"));
			place.addName(new PlaceName("Place " + i,"en"));			
			place.addLocation(new Location(d1*160-80, d2*360-180));
			placeDao.save(place);		
		}

		return "generate";
		
	}

}
