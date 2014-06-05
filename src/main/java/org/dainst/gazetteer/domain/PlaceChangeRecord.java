package org.dainst.gazetteer.domain;

import java.util.Comparator;
import java.util.Date;

public class PlaceChangeRecord {

	private String userId;
	private Date changeDate;
	
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public Date getChangeDate() {
		return changeDate;
	}
	
	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}
	
	@Override
	public String toString() {
		return "PlaceChangeRecord [userId=" + userId + ", changeDate=" + changeDate + "]";
	}
	
	public static class ChangeDateComparator implements Comparator<PlaceChangeRecord>{
		public int compare(PlaceChangeRecord changeRecord1, PlaceChangeRecord changeRecord2) {
			return (int) (changeRecord2.getChangeDate().getTime() - changeRecord1.getChangeDate().getTime());
		}
	}
}
