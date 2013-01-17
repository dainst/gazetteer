package org.dainst.gazetteer.controller;

import java.util.List;

import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.dao.ThesaurusRepository;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.Thesaurus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ThesaurusController {
	
	@Autowired
	ThesaurusRepository thesaurusRepository;
	
	@Autowired
	PlaceRepository placeRepository;
	
	@Value("${baseUri}")
	private String baseUri;
	
	@RequestMapping(value="/thesaurus", method=RequestMethod.GET)
	public ModelAndView listThesauri() {
		
		Iterable<Thesaurus> thesauri = thesaurusRepository.findAll();
		
		ModelAndView mav = new ModelAndView("thesaurus/list");
		mav.addObject("thesauri", thesauri);
		mav.addObject("baseUri", baseUri);
		
		return mav;
		
	}
	
	@RequestMapping(value="/thesaurus/{key}", method=RequestMethod.GET)
	public ModelAndView getThesaurus(@PathVariable String key) {
		
		Thesaurus thesaurus = thesaurusRepository.findOne(key);
		List<Place> places = placeRepository
				.findByThesauriAndParentIsNullAndDeletedIsFalse(key, new Sort("prefName"));
		
		ModelAndView mav = new ModelAndView("thesaurus/get");
		mav.addObject("thesaurus", thesaurus);
		mav.addObject("places", places);
		mav.addObject("baseUri", baseUri);
		
		return mav;		
		
	}

}
