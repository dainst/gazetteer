package org.dainst.gazetteer.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;

@Entity
public class Place {

	private long id;
	private Set<String> uris;
	private Set<PlaceName> names = new HashSet<PlaceName>();
	private Set<Location> locations = new HashSet<Location>();
	private Place parent;
	private Set<Place> children = new HashSet<Place>();
	private Date lastModified;
	private Date created;
	
	public Place() {
		created = new Date();
	}

	@Id
	@GeneratedValue
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@ElementCollection
	public Set<String> getUris() {
		return uris;
	}

	public void setUris(Set<String> uris) {
		this.uris = uris;
	}

	@OneToMany(mappedBy="place", cascade=CascadeType.ALL)
	public Set<PlaceName> getNames() {
		return names;
	}

	public void setNames(Set<PlaceName> names) {
		this.names = names;
	}
	
	public void addName(PlaceName name) {
		names.add(name);
		name.setPlace(this);
	}

	@OneToMany(mappedBy="place", cascade=CascadeType.ALL)
	public Set<Location> getLocations() {
		return locations;
	}

	public void setLocations(Set<Location> locations) {
		this.locations = locations;
	}
	
	public void addLocation(Location location) {
		locations.add(location);
		location.setPlace(this);
	}

	@ManyToOne
	public Place getParent() {
		return parent;
	}

	public void setParent(Place parent) {
		this.parent = parent;
	}

	@OneToMany(mappedBy="parent")
	public Set<Place> getChildren() {
		return children;
	}

	public void setChildren(Set<Place> children) {
		this.children = children;
	}
	
	public void addChild(Place child) {
		children.add(child);
		child.setParent(this);
	}

	@Version
	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}
	
}
