$(document).ready(function(){

	// Toggle extended search
	$("#extendedSearchBtn").click(function() {
		$("#extendedSearchDiv").slideToggle();
		$("#extendedSearchBtn i").toggleClass("icon-circle-arrow-down icon-circle-arrow-up");
		$("#extendedSearchBtn").toggleClass("active");
	});
	
	$('input.lnglat').locationPicker();
	
});

// Fix navbar to top when scrolling
$(document).scroll(function(){
	
	if($('.subnav')) {
		
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