'use strict';

/* Filters */

angular.module('gazetteer.filters', []).
  filter('interpolate', ['version', function(version) {
    return function(text) {
      return String(text).replace(/\%VERSION\%/mg, version);
    };
  }]).
  filter('parseUrlFilter', function() {
    var urlPattern = /(http|ftp|https):\/\/[\w-]+(\.[\w-]+)+([\w.,@?^=%&amp;:\/~+#-]*[\w@?^=%&amp;\/~+#-])?/gi;
    return function(text) {        
        if(text != undefined) angular.forEach(text.match(urlPattern), function(url) {
            text = text.replace(url, "<a target=\"_blank\" href="+ url + ">" + url +" <i class=\"icon-external-link\" style=\"font-size:0.7em\"></i></a>");
        });
        return text;        
    };
  }).
  filter('parseLineBreakFilter', function() {
    var lineBreakPattern = /\r?\n|\r/gi;
    return function(text) {
    	if(text != undefined) 
    		return text.replace(lineBreakPattern, "<br>");
    	else
    		return text;
    };
  }).
  filter('toTrusted', ['$sce', function($sce){
      return function(text) {
          return $sce.trustAsHtml(text);
      };
  }]).
  filter('reverse', function() {
	  return function(items) {
		  if (!items || items.length == 0)
			  return items;
		  else
			  return items.slice().reverse();
	  };
  }).
  filter('sortAlphabetically', function() {
	  var compareStrings = function (string1, string2) {
		  string1 = string1.toLowerCase();
		  string1 = string1.replace(/ä/g, "a");
		  string1 = string1.replace(/ö/g, "o");
		  string1 = string1.replace(/ü/g, "u");
		  string1 = string1.replace(/ß/g, "s");

		  string2 = string2.toLowerCase();
		  string2 = string2.replace(/ä/g, "a");
		  string2 = string2.replace(/ö/g, "o");
		  string2 = string2.replace(/ü/g, "u");
		  string2 = string2.replace(/ß/g, "s");

		  return (string1 == string2) ? 0 : (string1 > string2 ) ? 1 : -1;
	  };
	  
	  return function(items) {
		  return items.sort(compareStrings);
	  };
  });