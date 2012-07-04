package org.dainst.gazetteer.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;

@Entity
public class Place {

	@Id
	@GeneratedValue
	private long id;

	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private Set<Description> descriptions;

	@ElementCollection(fetch=FetchType.EAGER)
	private Set<String> uris;

	@OneToMany(mappedBy="place", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private Set<PlaceName> names = new HashSet<PlaceName>();

	@OneToMany(mappedBy="place", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private Set<Location> locations = new HashSet<Location>();

	@ManyToOne
	private Place parent;

	@OneToMany(mappedBy="parent", fetch=FetchType.EAGER)
	private Set<Place> children = new HashSet<Place>();

	@Version
	private Date lastModified;
	
	private Date created;
	
	private boolean deleted = false;
	
	public Place() {
		created = new Date();
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Set<Description> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(Set<Description> descriptions) {
		this.descriptions = descriptions;
	}
	
	public void addDescription(Description description) {
		descriptions.add(description);
	}

	public Set<String> getUris() {
		return uris;
	}

	public void setUris(Set<String> uris) {
		this.uris = uris;
	}

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

	public Place getParent() {
		return parent;
	}

	public void setParent(Place parent) {
		this.parent = parent;
		parent.addChild(this);
	}
	
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

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
}
