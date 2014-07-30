package org.dainst.gazetteer.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
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
			try {
				localizedLanguages.put(language, messageSource.getMessage("languages."+language, null, locale));
			} catch (NoSuchMessageException e) {
				localizedLanguages.put(language, messageSource.getMessage("languages."+language, null, Locale.GERMAN));
			}
		}
		
		return sortHashMapByValues(localizedLanguages);
	}
	
	private LinkedHashMap<String,String> sortHashMapByValues(HashMap<String,String> passedMap) {
		   List<String> mapKeys = new ArrayList<String>(passedMap.keySet());
		   List<String> mapValues = new ArrayList<String>(passedMap.values());
		   Collections.sort(mapValues);
		   Collections.sort(mapKeys);

		   LinkedHashMap<String,String> sortedMap = new LinkedHashMap<String,String>();

		   Iterator<String> valueIt = mapValues.iterator();
		   while (valueIt.hasNext()) {
		       String val = valueIt.next();
		       Iterator<String> keyIt = mapKeys.iterator();

		       while (keyIt.hasNext()) {
		           String key = keyIt.next();
		           String comp1 = passedMap.get(key);
		           String comp2 = val;

		           if (comp1.equals(comp2)){
		               passedMap.remove(key);
		               mapKeys.remove(key);
		               sortedMap.put(key, val);
		               break;
		           }

		       }

		   }
		   return sortedMap;
		}

}
