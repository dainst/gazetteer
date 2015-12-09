package org.dainst.gazetteer.domain;

import java.util.Comparator;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class PlaceChangeRecord {

	@Id
	private String id;
	
	private String userId;
	private String placeId;
	private String changeType;
	private Date changeDate;
	private String additionalData;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getPlaceId() {
		return placeId;
	}

	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}

	public String getChangeType() {
		return changeType;
	}

	public void setChangeType(String changeType) {
		this.changeType = changeType;
	}

	public Date getChangeDate() {
		return changeDate;
	}
	
	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}
	
	public String getAdditionalData() {
		return additionalData;
	}

	public void setAdditionalData(String additionalData) {
		this.additionalData = additionalData;
	}

	@Override
	public String toString() {
		return "PlaceChangeRecord [userId=" + userId + ", placeId=" + placeId + ", changeDate="
				+ changeDate + ", additionalData=" + additionalData + "]";
	}
	
	public static class ChangeDateComparator implements Comparator<PlaceChangeRecord> {
		public int compare(PlaceChangeRecord changeRecord1, PlaceChangeRecord changeRecord2) {
			
			long time1 = changeRecord1.getChangeDate().getTime();
			long time2 = changeRecord2.getChangeDate().getTime();
			
			if (time2 > time1)
	            return 1;
			else if (time1 > time2)
	            return -1;
			else
	            return 0;
		}
	}
}
