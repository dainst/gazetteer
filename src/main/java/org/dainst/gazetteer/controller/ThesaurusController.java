package org.dainst.gazetteer.controller;

import java.util.List;

import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.Place;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ThesaurusController {
	
	@Autowired
	PlaceRepository placeRepository;
	
	@Value("${baseUri}")
	private String baseUri;
	
	@RequestMapping(value="/thesaurus", method=RequestMethod.GET)
	public ModelAndView getThesaurus() {
		
		List<Place> places = placeRepository
				.findByParentIsNullAndDeletedIsFalse(new Sort("prefName"));
		
		ModelAndView mav = new ModelAndView("thesaurus/get");
		mav.addObject("places", places);
		mav.addObject("baseUri", baseUri);
		
		return mav;		
		
	}

}
