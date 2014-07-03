package org.dainst.gazetteer.helpers;

import java.math.BigDecimal;

import org.dainst.gazetteer.domain.Location;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class RoundCoordinatesService {

	private static int decimalPlaces = 1;
	
	public static void roundCoordinates(User user, Place place) {
		
		if (place.getType() != null && place.getType().equals("archaeological-site") &&
				(user == null || !user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")))) {			
			
			if (!place.getPrefLocation().isPublicSite()) {
				double lng = BigDecimal.valueOf(place.getPrefLocation().getLng()).setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP).doubleValue();
				double lat = BigDecimal.valueOf(place.getPrefLocation().getLat()).setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP).doubleValue();				
				place.getPrefLocation().setCoordinates(new double[] {lng, lat});			
			}
			
			for (Location location : place.getLocations()) {
				if (!location.isPublicSite()) {
					double lng = BigDecimal.valueOf(location.getLng()).setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP).doubleValue();
					double lat = BigDecimal.valueOf(location.getLat()).setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP).doubleValue();					
					location.setCoordinates(new double[] {lng, lat});
				}
			}
		}
	}
}