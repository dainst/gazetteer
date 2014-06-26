(function($){

    //Attach this new method to jQuery
    $.fn.extend({ 
        
        //This is where you write your plugin's name
        locationPicker: function() {
            
            var options = {
                height: "400px",
                padding: 10,
                defaultLat: 51.500152,
                defaultLng: -0.126236            
            };
            
            function RoundDecimal(num, decimals){
                var mag = Math.pow(10, decimals);
                return Math.round(num * mag)/mag;
            };
            
            var geocoder = new google.maps.Geocoder();

            //Iterate over the current set of matched elements
            return this.each(function() {
                
                var that = this;
                
                var setPosition = function(latLng, viewport){
                    var lat = RoundDecimal(latLng.lat(), 6);
                    var lng = RoundDecimal(latLng.lng(), 6);
                    marker.setPosition(latLng);
                    if(viewport){
                        map.fitBounds(viewport);
                        map.setZoom(map.getZoom() + 2);
                    }else{
                        map.panTo(latLng);
                    }
                    $(that).val(lat + "," + lng);
                    var scope = angular.element(that).scope();
                    scope.$apply("coordinates = [" + lat + "," + lng + "]");
                };
                
                var id = $(this).attr('id');
                
                var searchButton = $(this).siblings("button");
                $(this).keydown(function(e) {
                	if(e.keyCode == 13) {
                		searchButton.click();
                		e.preventDefault();
                		return false;
                	}
                });
                
                var picker = $("<div id='" + id + "-picker' class='modal hide'><div id='modal-header'><button type='button' class='close' data-dismiss='modal'>×</button><h3>Pick a location</h3></div>").css({
                    padding: options.padding
                });
                $("body").append(picker);
                var mapDiv = $("<div class='picker-map gmap modal-body'>Loading</div>").css({
                    height: options.height
                });
                picker.append(mapDiv);
                picker.append("<div class='modal-footer'><a href='#' class='btn btn-primary' data-dismiss='modal'>OK</a></div>");
                
                var myLatlng = new google.maps.LatLng(options.defaultLat, options.defaultLng);
                var myOptions = {
                    zoom: 15,
                    center: myLatlng,
                    mapTypeId: google.maps.MapTypeId.ROADMAP,
                    disableDoubleClickZoom: true,
                    streetViewControl: false
                }
                var map = new google.maps.Map(mapDiv.get(0), myOptions);
                
                var marker = new google.maps.Marker({
                    position: myLatlng, 
                    map: map, 
                    title: "Drag Me",
                    draggable: true
                });
                
                google.maps.event.addListener(map, 'dblclick', function(event) {
                    setPosition(event.latLng);
                });
                
                google.maps.event.addListener(marker, 'dragend', function(event) {
                    setPosition(marker.position);
                });
                
                function getCurrentPosition(){
                    var posStr = $(that).val();
                    if(posStr != ""){
                        var posArr = posStr.split(",");
                        if(posArr.length == 2){
                            var lat = $.trim(posArr[0]);
                            var lng = $.trim(posArr[1]);
                            var latlng = new google.maps.LatLng(lat, lng);
                            setPosition(latlng);
                            return;
                        }
                        $(that).val("Invalid Position");
                    }
                    
                }
                
                function showPicker(){
                    picker.modal();
                    google.maps.event.trigger(map, 'resize');
                    getCurrentPosition();
                    map.setCenter(marker.position);
                }
                
                $(":input").focus(function(){
                    if($(this).attr('id') != $(that).attr('id')){
                        if($(picker).children(this).length == 0){
                            picker.fadeOut('fast');
                        }
                    }
                });
                
                function isLngLat(val){
                    var lngLatArr = val.split(",");
                    if(lngLatArr.length == 2){
                        if(isNaN(lngLatArr[0]) || isNaN(lngLatArr[1])){
                            return false;
                        }else{
                            return true;
                        }
                    }
                    return false;
                }
                
                function findAddress(){
                    var address = $(that).val().split(",").reverse().join(",");
                    if(address == ""){
                        alert("Please enter an address or Lng/Lat position.");
                    }else{
                        if(isLngLat(address)){
                            showPicker();
                        }else{
                            geocoder.geocode( {'address': address, 'region': 'uk'}, function(results, status) {
                                if (status == google.maps.GeocoderStatus.OK) {
                                    setPosition(
                                        results[0].geometry.location,
                                        results[0].geometry.viewport
                                    );
                                    showPicker();
                                } else {
                                    alert("Geocode was not successful for the following reason: " + status);
                                }
                            });
                        }
                        $(that).focus();
                    }
                }
                
                
                
                $(searchButton).click(function(event){
                    findAddress();
                    event.stopPropagation();
                });
                
                $(that).keydown(function(event) {
                    if (event.keyCode == '13') { // enter
                        findAddress();
                    }
                });
                
                $('html').click(function() {
                    picker.fadeOut('fast');
                });
                
                $(picker).click(function(event){
                    event.stopPropagation();
                    //$(that).focus();
                });
                
                $(this).click(function(event){
                    event.stopPropagation();
                });
            
            });
            
            
        }
        
    });
       
})(jQuery);