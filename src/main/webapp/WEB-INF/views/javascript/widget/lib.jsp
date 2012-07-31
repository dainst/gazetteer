<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="application/javascript; charset=utf-8" session="false"%>

(function() {

// Localize jQuery variable
var jQuery;

/******** Load jQuery if not present *********/
if (window.jQuery === undefined || window.jQuery.fn.jquery !== '1.7.2') {
    var script_tag = document.createElement('script');
    script_tag.setAttribute("type","text/javascript");
    script_tag.setAttribute("src",
        "http://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js");
    if (script_tag.readyState) {
      script_tag.onreadystatechange = function () { // For old versions of IE
          if (this.readyState == 'complete' || this.readyState == 'loaded') {
              scriptLoadHandler();
          }
      };
    } else {
      script_tag.onload = scriptLoadHandler;
    }
    // Try to find the head, otherwise default to the documentElement
    (document.getElementsByTagName("head")[0] || document.documentElement).appendChild(script_tag);
} else {
    // The jQuery version on the window is the one we want to use
    jQuery = window.jQuery;
    main();
}

/******** Called once jQuery has loaded ******/
function scriptLoadHandler() {
    // Restore $ and window.jQuery to their previous values and store the
    // new jQuery in our local jQuery variable
    jQuery = window.jQuery.noConflict(true);
    // Call our main function
    main(); 
}

/******** Our main function ********/
function main() { 
    jQuery(document).ready(function($) { 
        /******* Load CSS *******/
        var css_link = $("<link>", { 
            rel: "stylesheet", 
            type: "text/css", 
            href: "${baseUri}resources/css/widget.css" 
        });
        css_link.appendTo('head');

        /******* Load HTML for show elements *******/
        if ($('gaz\\:show').length) {
        	$('gaz\\:show').each(function(index, showElem) {
        		var jsonp_url = "${baseUri}widget/show.js?callback=?";
        		var id = $(this).attr("gaz-id");
        		if (id != undefined) jsonp_url += "&id=" + id;
        		var ids = $(this).attr("gaz-ids");
        		if (ids != undefined) {
        			var ids_split = ids.split(",");
        			$.each(ids_split, function(i, val) {
        				jsonp_url += "&id=" + val;
        			});
        		} 
        		var height = $(this).attr("map-height");
        		if (height != undefined) jsonp_url += "&mapHeight=" + height;
        		var show_info = $(this).attr("show-info");
        		if (show_info != undefined) jsonp_url += "&showInfo=true";
		        $.getJSON(jsonp_url, function(data) {
		        	var decodedHtml = $("<div/>").html(data.html).text();
		        	$(showElem).replaceWith(decodedHtml);
		        });
        	});
        }        
        
        
    });
}

})(); // We call our anonymous function immediately