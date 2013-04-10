$(document).ready(function(){
	
	$(".simpleSearchForm").submit(function(e) {
		e.preventDefault();
		var query = $(e.delegateTarget).find("input[name=q]").val();
		window.location = $(e.delegateTarget).attr("action") + "?q=" + query;
	});
	
});
$(document).scroll(function(){

	if($('#map-well')) {
		if ($(window).scrollTop() > 90) {
			$('#map-well').css({'position': 'fixed', 'top': '50px', 'width': $('#map-well').width()});
		} else {
			$('#map-well').css({'position': 'relative', 'top': 'auto'});
		}
	}
	
});
