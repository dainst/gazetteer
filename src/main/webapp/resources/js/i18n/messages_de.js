var messages = angular.module('gazetteer.messages', []);

messages.factory('messages', function(){
	return {
		"place.types.continent": "Kontinent",
		"place.types.country": "Stadt",
		"place.types.city": "Stadt",
		"location.confidence.0": "Keine Angabe",
		"location.confidence.1": "Ungenau",
		"location.confidence.2": "Genau",
		"location.confidence.3": "Exakt",
		"languages.deu": "Deutsch",
		"languages.eng": "Englisch",
		"languages.ita": "Italienisch",
		"languages.fra": "Französisch",
		"languages.ell": "Griechisch (Modern)",
		"languages.lat": "Lateinisch",
		"languages.grc": "Altgriechisch"
	};
});