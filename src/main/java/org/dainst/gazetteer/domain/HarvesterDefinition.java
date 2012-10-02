package org.dainst.gazetteer.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.dainst.gazetteer.harvest.Harvester;

@Entity
public class HarvesterDefinition {
	
	@Id
	private String name;

	private String targetThesaurus;
	
	private Date lastHarvestedDate;
	
	private String cronExpression;
	
	private Class<? extends Harvester> harvesterType;
	
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

	public Class<? extends Harvester> getHarvesterType() {
		return harvesterType;
	}

	public void setHarvesterType(Class<? extends Harvester> harvesterType) {
		this.harvesterType = harvesterType;
	}

}
