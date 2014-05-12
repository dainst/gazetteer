package org.dainst.gazetteer.helpers;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.i18n.AbstractLocaleResolver;

public class ConfigurableAcceptHeaderLocaleResolver extends
		AbstractLocaleResolver {
	
	private Set<String> availableLanguages;

	@Override
	public Locale resolveLocale(HttpServletRequest request) {
		@SuppressWarnings("unchecked")
		Enumeration<Locale> acceptLocales = request.getLocales();
		for (Locale locale = acceptLocales.nextElement(); 
				acceptLocales.hasMoreElements(); locale = acceptLocales.nextElement()) {
			if (availableLanguages.contains(locale.getLanguage()))
				return locale;
		}
		return getDefaultLocale();
	}

	@Override
	public void setLocale(HttpServletRequest request,
			HttpServletResponse response, Locale locale) {
		// cannot be implemented since accept header is not writable
	}

	public void setAvailableLanguages(Set<String> availableLanguages) {
		this.availableLanguages = availableLanguages;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		setDefaultLocale(new Locale(defaultLanguage));
	}

}
