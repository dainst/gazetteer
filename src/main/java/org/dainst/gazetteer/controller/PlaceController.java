package org.dainst.gazetteer.controller;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class PlaceController {
	
	@Resource(name="mediaTypes")
	Map<String,String> mediaTypes;

	@RequestMapping(value="/place/{id}", method=RequestMethod.GET)
	public View getPlace(@PathVariable String id, HttpServletRequest request) {
		
		String acceptHeader = request.getHeader("Accept");
		
		String suffix = "html";
		if (acceptHeader != null) for (Entry<String, String> entry : mediaTypes.entrySet()) {
			if (entry.getValue().equals(acceptHeader))
				suffix = entry.getKey();
		}
		
		return new RedirectView("/doc/" + id + "." + suffix, true, false);
		
	}
	
}
