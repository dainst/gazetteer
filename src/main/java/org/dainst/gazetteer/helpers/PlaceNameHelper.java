package org.dainst.gazetteer.helpers;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

import org.dainst.gazetteer.domain.PlaceName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlaceNameHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(PlaceNameHelper.class);
		
	private Locale locale;
	
	private Locale originalLocale;
	
	private LanguagesHelper languagesHelper;
	
	private Map<String, String> localizedLanguages;

	public List<PlaceName> sortPlaceNames(Set<PlaceName> placeNames) {
			
		localizedLanguages = languagesHelper.getLocalizedLanguages(locale);
		
		List<PlaceName> result = new ArrayList<PlaceName>(placeNames);
		Collections.sort(result, new PlaceNameComparator());
		
		return result;
	}
	
	
	public Locale getLocale() {
		return locale;
	}


	public void setLocale(Locale locale) {
		this.locale = locale;
	}


	public Locale getOriginalLocale() {
		return originalLocale;
	}


	public void setOriginalLocale(Locale originalLocale) {
		this.originalLocale = originalLocale;
	}


	public LanguagesHelper getLanguagesHelper() {
		return languagesHelper;
	}


	public void setLanguagesHelper(LanguagesHelper languagesHelper) {
		this.languagesHelper = languagesHelper;
	}


	private class PlaceNameComparator implements Comparator<PlaceName> {
		public int compare(PlaceName placeName1, PlaceName placeName2) {

			int langComp;
			
			if ((placeName1.getLanguage() == null || placeName1.getLanguage().isEmpty())
					&& (placeName2.getLanguage() != null && !placeName2.getLanguage().isEmpty()))
				langComp = 1;
			else if ((placeName2.getLanguage() == null || placeName2.getLanguage().isEmpty())
					&& (placeName1.getLanguage() != null && !placeName1.getLanguage().isEmpty()))
				langComp = -1;
			else if ((placeName1.getLanguage() == null || placeName1.getLanguage().isEmpty())
					&& (placeName2.getLanguage() == null || placeName2.getLanguage().isEmpty()))
				langComp = 0;
			else {
				String originalLocaleIso3Language = null;
				String placeName1Iso3Language = null;
				String placeName2Iso3Language = null;
				
				try {
					originalLocaleIso3Language = originalLocale.getISO3Language();
					placeName1Iso3Language = new Locale(placeName1.getLanguage()).getISO3Language();
					placeName2Iso3Language = new Locale(placeName2.getLanguage()).getISO3Language();
				} catch (MissingResourceException e) {
					logger.warn("Failed to get ISO 3 language code", e);
				}
				
				if (originalLocaleIso3Language.equals(placeName1Iso3Language)
						&& !originalLocaleIso3Language.equals(placeName2Iso3Language))
					langComp = -1;
				else if (originalLocaleIso3Language.equals(placeName2Iso3Language)
						&& !originalLocaleIso3Language.equals(placeName1Iso3Language))
					langComp = 1;
				else {
					String localizedLanguage1 = localizedLanguages.get(placeName1.getLanguage());
					String localizedLanguage2 = localizedLanguages.get(placeName2.getLanguage());
					
					if (localizedLanguage1 != null && localizedLanguage2 != null)
						langComp = Collator.getInstance(locale).compare(localizedLanguage1, localizedLanguage2);
					else
						langComp = 0;
				}
			}
			
			if (langComp != 0)
	            return langComp;
			else
				return Collator.getInstance(locale).compare(placeName1.getTitle(), placeName2.getTitle());
		}
	}
	
}
