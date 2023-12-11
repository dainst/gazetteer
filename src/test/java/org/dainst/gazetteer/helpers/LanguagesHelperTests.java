package org.dainst.gazetteer.helpers;

import static org.junit.Assert.assertEquals;

import java.util.Locale;
import java.util.Map;

import org.junit.Test;

public class LanguagesHelperTests {

	@Test
	public void testLocaleForISO3Language() {
		
		LanguagesHelper helper = new LanguagesHelper();
		
		assertEquals("en", helper.getLocaleForISO3Language("eng").getLanguage());
		assertEquals("de", helper.getLocaleForISO3Language("deu").getLanguage());
		assertEquals("it", helper.getLocaleForISO3Language("ita").getLanguage());
		assertEquals("fr", helper.getLocaleForISO3Language("fra").getLanguage());
		assertEquals("el", helper.getLocaleForISO3Language("ell").getLanguage());
		assertEquals("la", helper.getLocaleForISO3Language("lat").getLanguage());
		assertEquals("grc", helper.getLocaleForISO3Language("grc").getLanguage());
		assertEquals("sq", helper.getLocaleForISO3Language("sqi").getLanguage());
		assertEquals("pl", helper.getLocaleForISO3Language("pol").getLanguage());
		assertEquals("tr", helper.getLocaleForISO3Language("tur").getLanguage());
		assertEquals("ar", helper.getLocaleForISO3Language("ara").getLanguage());
		assertEquals("es", helper.getLocaleForISO3Language("spa").getLanguage());
		assertEquals("pt", helper.getLocaleForISO3Language("por").getLanguage());
		assertEquals("zh", helper.getLocaleForISO3Language("zho").getLanguage());
		assertEquals("ru", helper.getLocaleForISO3Language("rus").getLanguage());
		assertEquals("vi", helper.getLocaleForISO3Language("vie").getLanguage());
		
	}
	
	@Test
	public void testDisplayNamesGerman() {
		
		LanguagesHelper helper = new LanguagesHelper();
		helper.setLanguages(new String[]{"deu","eng","ita","fra","ell","lat","grc","sqi","pol","tur","ara","spa","por","zho","rus","vie"});
		
		Map<String,String> localizedLanguages = helper.getLocalizedLanguages(Locale.GERMAN);
		
		assertEquals("Deutsch", localizedLanguages.get("deu"));
		assertEquals("Englisch", localizedLanguages.get("eng"));
		assertEquals("Italienisch", localizedLanguages.get("ita"));
		assertEquals("Französisch", localizedLanguages.get("fra"));
		assertEquals("Griechisch", localizedLanguages.get("ell"));
		assertEquals("Lateinisch", localizedLanguages.get("lat"));
		assertEquals("Altgriechisch", localizedLanguages.get("grc"));
		assertEquals("Albanisch", localizedLanguages.get("sqi"));
		assertEquals("Polnisch", localizedLanguages.get("pol"));
		assertEquals("Türkisch", localizedLanguages.get("tur"));
		assertEquals("Arabisch", localizedLanguages.get("ara"));
		assertEquals("Spanisch", localizedLanguages.get("spa"));
		assertEquals("Portugiesisch", localizedLanguages.get("por"));
		assertEquals("Chinesisch", localizedLanguages.get("zho"));
		assertEquals("Russisch", localizedLanguages.get("rus"));
		assertEquals("Vietnamesisch", localizedLanguages.get("vie"));
		
	}
	
	@Test
	public void testDisplayNamesEnglish() {
		
		LanguagesHelper helper = new LanguagesHelper();
		helper.setLanguages(new String[]{"deu","eng","ita","fra","ell","lat","grc","sqi","pol","tur","ara","spa","por","zho","rus","vie"});
		
		Map<String,String> localizedLanguages = helper.getLocalizedLanguages(Locale.ENGLISH);
		
		assertEquals("German", localizedLanguages.get("deu"));
		assertEquals("English", localizedLanguages.get("eng"));
		assertEquals("Italian", localizedLanguages.get("ita"));
		assertEquals("French", localizedLanguages.get("fra"));
		assertEquals("Greek", localizedLanguages.get("ell"));
		assertEquals("Latin", localizedLanguages.get("lat"));
		assertEquals("Ancient Greek", localizedLanguages.get("grc"));
		assertEquals("Albanian", localizedLanguages.get("sqi"));
		assertEquals("Polish", localizedLanguages.get("pol"));
		assertEquals("Turkish", localizedLanguages.get("tur"));
		assertEquals("Arabic", localizedLanguages.get("ara"));
		assertEquals("Spanish", localizedLanguages.get("spa"));
		assertEquals("Portuguese", localizedLanguages.get("por"));
		assertEquals("Chinese", localizedLanguages.get("zho"));
		assertEquals("Russian", localizedLanguages.get("rus"));
		assertEquals("Vietnamese", localizedLanguages.get("vie"));
		
	}

}
