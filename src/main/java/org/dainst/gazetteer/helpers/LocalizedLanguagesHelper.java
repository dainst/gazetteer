package org.dainst.gazetteer.helpers;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class LocalizedLanguagesHelper {
	
	@Value("${languages}")
	private String[] languages;
	
	@Autowired
	MessageSource messageSource;

	public Map<String,String> getLocalizedLanguages(Locale locale) {
		HashMap<String, String> localizedLanguages = new HashMap<String,String>();
		for (String language : languages) {
			localizedLanguages.put(language, messageSource.getMessage("languages."+language, null, locale));
		}
		return localizedLanguages;
	}

}
