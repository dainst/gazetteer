$(document).ready(function(){
	
	$(".simpleSearchForm").submit(function(e) {
		e.preventDefault();
		var query = $(e.delegateTarget).find("input[name=q]").val();
		window.location = $(e.delegateTarget).attr("action") + "?q=" + query;
	});
	
	$('#affix-menu').affix({ offset: {top: 176} });
	
});
$(document).scroll(function(){

	if($('#map-well')) {
		if ($(window).scrollTop() > 276 && $(window).width() > 767) {
			$('#map-well').css({'position': 'fixed', 'top': '60px', 'width': $('#map-well').width()});
			$(window).resize(function() {
				$('#map-well').css({'width': $('#map-well-wrapper').width() - 40});
			});
		} else {
			$('#map-well').css({'position': 'relative', 'top': 'auto'});
		}
		
	}
	
});

function getIdFromUri(uri) {
	return uri.substr(uri.lastIndexOf("/")+1);
}
