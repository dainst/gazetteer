package org.dainst.gazetteer.match;

import org.dainst.gazetteer.domain.Place;

public class Candidate {
	
	private Place place;
	private Place candidate;
	private float score;
	
	public Candidate(Place place, Place candidate, float score) {
		this.place = place;
		this.candidate = candidate;
		this.score = score;
	}

	public Place getPlace() {
		return place;
	}
	
	public void setPlace(Place place) {
		this.place = place;
	}
	
	public float getScore() {
		return score;
	}
	
	public void setScore(float score) {
		this.score = score;
	}

	public Place getCandidate() {
		return candidate;
	}

	public void setCandidate(Place candidate) {
		this.candidate = candidate;
	}

}
