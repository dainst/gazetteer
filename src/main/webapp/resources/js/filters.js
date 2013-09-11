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
  });
