package org.dainst.gazetteer.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Place {

	@Id
	private String id;

	private Set<Link> links = new HashSet<Link>();
	
	private PlaceName prefName;

	private Set<PlaceName> names = new HashSet<PlaceName>();
	
	private String type;

	private Set<Location> locations = new HashSet<Location>();

	private String parent;

	private Set<String> children = new HashSet<String>();

	private Set<String> relatedPlaces = new HashSet<String>();
	
	private Set<Comment> comments = new HashSet<Comment>();
	
	private Set<Tag> tags = new HashSet<Tag>();
	
	private Set<Identifier> ids = new HashSet<Identifier>();
	
	private String thesaurus;
	
	private boolean needsReview = false;
	
	private boolean deleted = false;
	
	private String replacedBy;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Set<Link> getLinks() {
		return links;
	}

	public void setLinks(Set<Link> links) {
		this.links = links;
	}
	
	public void addLink(Link link) {
		this.links.add(link);
	}

	public Set<PlaceName> getNames() {
		return names;
	}

	public Map<String, PlaceName> getNameMap() {
		HashMap<String, PlaceName> result = new HashMap<String, PlaceName>();
		for (PlaceName name : names) {
			result.put(name.getLanguage(), name);
		}
		return result;
	}

	public void setNames(Set<PlaceName> names) {
		this.names = names;
	}
	
	public void addName(PlaceName name) {
		names.add(name);
	}

	public Set<Location> getLocations() {
		return locations;
	}

	public void setLocations(Set<Location> locations) {
		this.locations = locations;
	}
	
	public void addLocation(Location location) {
		locations.add(location);
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}
	
	public Set<String> getChildren() {
		return children;
	}

	public void setChildren(Set<String> children) {
		this.children = children;
	}
	
	public void addChild(String child) {
		children.add(child);
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
		}
	}

	public void removeLocation(Location location) {
		if (locations.contains(location)) {
			locations.remove(location);
		}
	}

	public Set<String> getRelatedPlaces() {
		return relatedPlaces;
	}

	public void setRelatedPlaces(Set<String> relatedPlaces) {
		this.relatedPlaces = relatedPlaces;
	}

	public void addRelatedPlace(String relatedPlace) {
		relatedPlaces.add(relatedPlace);
	}

	public Set<Comment> getComments() {
		return comments;
	}

	public void setComments(Set<Comment> comments) {
		this.comments = comments;
	}
	
	public void addComment(Comment comment) {
		this.comments.add(comment);
	}

	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}
	
	public void addTag(Tag tag) {
		this.tags.add(tag);
	}

	public Set<Identifier> getIdentifiers() {
		return ids;
	}

	public void setIdentifiers(Set<Identifier> ids) {
		this.ids = ids;
	}
	
	public void addIdentifier(Identifier id) {
		this.ids.add(id);
	}

	public String getThesaurus() {
		return thesaurus;
	}

	public void setThesaurus(String thesaurus) {
		this.thesaurus = thesaurus;
	}

	public boolean isNeedsReview() {
		return needsReview;
	}

	public void setNeedsReview(boolean needsReview) {
		this.needsReview = needsReview;
	}

	public PlaceName getPrefName() {
		return prefName;
	}

	public void setPrefName(PlaceName prefName) {
		this.prefName = prefName;
	}

	public String getReplacedBy() {
		return replacedBy;
	}

	public void setReplacedBy(String replacedBy) {
		this.replacedBy = replacedBy;
	}

	@Override
	public String toString() {
		return "Place [id=" + id + ", prefName=" + prefName + ", names=" + names
				+ ", type=" + type + ", links=" + links
				+ ", locations=" + locations + ", parent=" + parent
				+ ", children=" + children + ", relatedPlaces=" + relatedPlaces
				+ ", comments=" + comments + ", tags=" + tags + ", ids=" + ids
				+ ", thesaurus=" + thesaurus + ", needsReview=" + needsReview
				+ ", deleted=" + deleted + ", replacedBy=" + replacedBy + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((children == null) ? 0 : children.hashCode());
		result = prime * result
				+ ((comments == null) ? 0 : comments.hashCode());
		result = prime * result + (deleted ? 1231 : 1237);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((ids == null) ? 0 : ids.hashCode());
		result = prime * result + ((links == null) ? 0 : links.hashCode());
		result = prime * result
				+ ((locations == null) ? 0 : locations.hashCode());
		result = prime * result + ((names == null) ? 0 : names.hashCode());
		result = prime * result + (needsReview ? 1231 : 1237);
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result
				+ ((prefName == null) ? 0 : prefName.hashCode());
		result = prime * result
				+ ((relatedPlaces == null) ? 0 : relatedPlaces.hashCode());
		result = prime * result
				+ ((replacedBy == null) ? 0 : replacedBy.hashCode());
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
		result = prime * result
				+ ((thesaurus == null) ? 0 : thesaurus.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Place other = (Place) obj;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (comments == null) {
			if (other.comments != null)
				return false;
		} else if (!comments.equals(other.comments))
			return false;
		if (deleted != other.deleted)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (ids == null) {
			if (other.ids != null)
				return false;
		} else if (!ids.equals(other.ids))
			return false;
		if (links == null) {
			if (other.links != null)
				return false;
		} else if (!links.equals(other.links))
			return false;
		if (locations == null) {
			if (other.locations != null)
				return false;
		} else if (!locations.equals(other.locations))
			return false;
		if (names == null) {
			if (other.names != null)
				return false;
		} else if (!names.equals(other.names))
			return false;
		if (needsReview != other.needsReview)
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (prefName == null) {
			if (other.prefName != null)
				return false;
		} else if (!prefName.equals(other.prefName))
			return false;
		if (relatedPlaces == null) {
			if (other.relatedPlaces != null)
				return false;
		} else if (!relatedPlaces.equals(other.relatedPlaces))
			return false;
		if (replacedBy == null) {
			if (other.replacedBy != null)
				return false;
		} else if (!replacedBy.equals(other.replacedBy))
			return false;
		if (tags == null) {
			if (other.tags != null)
				return false;
		} else if (!tags.equals(other.tags))
			return false;
		if (thesaurus == null) {
			if (other.thesaurus != null)
				return false;
		} else if (!thesaurus.equals(other.thesaurus))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	
	
}
