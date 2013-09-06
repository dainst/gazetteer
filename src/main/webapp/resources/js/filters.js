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
    return function(text, otherProp) {        
        angular.forEach(text.match(urlPattern), function(url) {
            text = text.replace(url, "<a target=\"_blank\" href="+ url + ">" + url +" <i class=\"icon-external-link\" style=\"font-size:0.7em\"></i></a>");
        });
        return text;        
    };
  });
