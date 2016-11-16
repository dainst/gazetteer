package org.dainst.gazetteer.controller;

import org.dainst.gazetteer.domain.Shape;
import org.dainst.gazetteer.domain.ValidationResult;
import org.dainst.gazetteer.helpers.PolygonValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ValidationController {
	
	private static final Logger logger = LoggerFactory.getLogger(ValidationController.class);

	@RequestMapping(value="/validation/multipolygon", method=RequestMethod.POST)
	@ResponseBody
	public ValidationResult validateMultipolygon(@RequestBody double[][][][] coordinates) {
		
		Shape shape = new Shape();
		shape.setCoordinates(coordinates);
		
		logger.debug("COORDINATES LENGTH: " + coordinates.length);
		
		PolygonValidator validator = new PolygonValidator();
		ValidationResult result = validator.validate(shape);		
		
		return result;
	}
}
