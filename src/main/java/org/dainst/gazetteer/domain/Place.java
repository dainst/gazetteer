package org.dainst.gazetteer.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.persistence.Version;

@Entity
public class Place {

	@Id
	@GeneratedValue
	private long id;

	@ElementCollection(fetch=FetchType.EAGER)
	private Set<String> uris;

	@OneToMany(mappedBy="place", cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
	@OrderBy("ordering")
	private List<PlaceName> names = new ArrayList<PlaceName>();
	
	private String type;

	@OneToMany(mappedBy="place", cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
	private Set<Location> locations = new HashSet<Location>();

	@ManyToOne
	private Place parent;

	@OneToMany(mappedBy="parent", cascade={CascadeType.PERSIST, CascadeType.MERGE}, fetch=FetchType.EAGER)
	private Set<Place> children = new HashSet<Place>();
	
	@ManyToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE}, fetch=FetchType.EAGER)
	private Set<Place> relatedPlaces = new HashSet<Place>();
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
	private Set<Comment> comments = new HashSet<Comment>();
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
	private Set<Tag> tags = new HashSet<Tag>();
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
	private Set<Identifier> ids = new HashSet<Identifier>();
	
	@ManyToOne
	private Thesaurus thesaurus;

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

	public Set<String> getUris() {
		return uris;
	}

	public void setUris(Set<String> uris) {
		this.uris = uris;
	}

	public List<PlaceName> getNames() {
		return names;
	}
	
	public String[] getNamesAsArray() {
		String[] result = new String[names.size()];
		for (int i = 0; i < names.size(); i++) {
			result[i] = names.get(i).getTitle();
		}
		return result;
	}
	
	@Transient
	public Map<String, PlaceName> getNameMap() {
		HashMap<String, PlaceName> result = new HashMap<String, PlaceName>();
		for (PlaceName name : names) {
			result.put(name.getLanguage(), name);
		}
		return result;
	}

	public void setNames(List<PlaceName> names) {
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void removeName(PlaceName name) {
		if (names.contains(name)) {
			names.remove(name);
			name.setPlace(null);
		}
	}

	public void removeLocation(Location location) {
		if (locations.contains(location)) {
			locations.remove(location);
			location.setPlace(null);
		}
	}

	public Set<Place> getRelatedPlaces() {
		return relatedPlaces;
	}

	public void setRelatedPlaces(Set<Place> relatedPlaces) {
		this.relatedPlaces = relatedPlaces;
	}

	public void addRelatedPlace(Place relatedPlace) {
		relatedPlaces.add(relatedPlace);
		relatedPlace.getRelatedPlaces().add(relatedPlace);
	}

	public Set<Comment> getComments() {
		return comments;
	}

	public void setComments(Set<Comment> comments) {
		this.comments = comments;
	}

	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	public Set<Identifier> getIdentifiers() {
		return ids;
	}

	public void setIdentifiers(Set<Identifier> ids) {
		this.ids = ids;
	}

	public Thesaurus getThesaurus() {
		return thesaurus;
	}

	public void setThesaurus(Thesaurus thesaurus) {
		this.thesaurus = thesaurus;
	}
	
}
