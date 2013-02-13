$(document).ready(function(){

	// Toggle extended search
	$("#extendedSearchBtn").click(function(e) {
		$("#extendedSearchDiv").slideToggle();
		$("#extendedSearchBtn i").toggleClass("icon-circle-arrow-down icon-circle-arrow-up");
		$("#extendedSearchBtn").toggleClass("active");
		e.preventDefault();
	});
	
	
	$(".simpleSearchForm").submit(function(e) {
		e.preventDefault();
		var query = $(e.delegateTarget).find("input[name=q]").val();
		window.location = $(e.delegateTarget).attr("action") + "?q=" + query;
	});
	
	$("#extendedSearchForm").submit(function(e) {
		e.preventDefault();
		var query = $("#extendedSearchForm").find("input[name=q]").val();
		var type = "";
		if ($("#extendedSearchForm").find("input[name=fuzzy]").val()) {
			type = "fuzzy"
		}
		window.location = $("#simpleSearchForm").attr("action") + "?q=" + query + "&type=" + type;
	});
	
});
$(document).scroll(function(){

	// Fix navbar to top when scrolling
	if($('.subnav').length > 0) {
		
	    // If has not activated (has no attribute "data-top"
	    if (!$('.subnav').attr('data-top')) {
	        // If already fixed, then do nothing
	        if ($('.subnav').hasClass('subnav-fixed')) return;
	        // Remember top position
	        var offset = $('.subnav').offset();
	        $('.subnav').attr('data-top', offset.top);
	    }

	    if ($('.subnav').attr('data-top') - $('.subnav').outerHeight() <= $(this).scrollTop())
	        $('.subnav').addClass('subnav-fixed');
	    else
	        $('.subnav').removeClass('subnav-fixed');
	    
	}
	
});
