package org.dainst.gazetteer.domain;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class HarvesterDefinition {
	
	@Id
	private String name;

	private String targetThesaurus;
	
	private Date lastHarvestedDate;
	
	private String cronExpression;
	
	private boolean enabled = true;
	
	private String harvesterType;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTargetThesaurus() {
		return targetThesaurus;
	}

	public void setTargetThesaurus(String targetThesaurus) {
		this.targetThesaurus = targetThesaurus;
	}

	public Date getLastHarvestedDate() {
		return lastHarvestedDate;
	}

	public void setLastHarvestedDate(Date lastHarvestedDate) {
		this.lastHarvestedDate = lastHarvestedDate;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public String getHarvesterType() {
		return harvesterType;
	}

	public void setHarvesterType(String harvesterType) {
		this.harvesterType = harvesterType;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
