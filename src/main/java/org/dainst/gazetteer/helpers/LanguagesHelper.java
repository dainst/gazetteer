package org.dainst.gazetteer.helpers;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
public class LanguagesHelper {
	
	@Value("${languages}")
	private String[] languages;
	
	@Autowired
	MessageSource messageSource;

	
	private Map<String, Locale> localeMap;
	private Map<Locale, Map<String, String>> localizedLanguagesMap = new HashMap<Locale, Map<String, String>>();
	
	public LanguagesHelper() {
		String[] languages = Locale.getISOLanguages();
		localeMap = new HashMap<String, Locale>(languages.length);
		for (String language : languages) {
		    Locale locale = new Locale(language);
		    localeMap.put(locale.getISO3Language(), locale);
		}
	}
	
	public Locale getLocaleForISO3Language(String iso3) {
		if (localeMap.containsKey(iso3)) {
			return localeMap.get(iso3);
		} else {
			return new Locale(iso3);
		}
	}

	public Map<String, String> getLocalizedLanguages(Locale locale) {
		if (localizedLanguagesMap.containsKey(locale)) {
			return localizedLanguagesMap.get(locale);
		} else {
			HashMap<String, String> map = new HashMap<String,String>();
			for (String language : languages) {
				String displayLanguage = getLocaleForISO3Language(language).getDisplayLanguage(locale);
				if (!displayLanguage.equals(language))
					map.put(language, getLocaleForISO3Language(language).getDisplayLanguage(locale));
				else {
					try {
						map.put(language, messageSource.getMessage("languages." + language, null, locale));
					} catch (NoSuchMessageException e) {
						map.put(language, messageSource.getMessage("languages." + language, null, Locale.GERMAN));
					}
				}
			}
			map = sortHashMapByValues(map, locale);
			localizedLanguagesMap.put(locale, map);
			return map;
		}
	}

	public String[] getLanguages() {
		return languages;
	}

	public void setLanguages(String[] languages) {
		this.languages = languages;
	}
	
	private LinkedHashMap<String,String> sortHashMapByValues(HashMap<String,String> passedMap, final Locale locale) {
		List<String> mapKeys = new ArrayList<String>(passedMap.keySet());
		List<String> mapValues = new ArrayList<String>(passedMap.values());
		Collections.sort(mapValues, new Comparator<String>() {
		    Comparator<Object> collator = Collator.getInstance(locale);
		    public int compare(String str1, String str2) {
		        return collator.compare(str1, str2);
		    }
		});
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
