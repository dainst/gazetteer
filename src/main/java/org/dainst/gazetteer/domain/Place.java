package org.dainst.gazetteer.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@CompoundIndexes({
	@CompoundIndex(name="linksObject_linksPredicate", def="{'links.object': 1, 'links.predicate': 1}"),
	@CompoundIndex(name="prefNameTitle_types", def="{'prefName.title': 1, 'types': 1}"),
	@CompoundIndex(name="parent_type", def="{'parent': 1, 'types': 1}"),
	@CompoundIndex(name="types_deleted", def="{'types': 1, 'deleted': 1}"),
	@CompoundIndex(name="prefNameTitle_types_needsReview_id", def="{'prefName.title': 1, 'types': 1, 'needsReview': 1, '_id': 1}"),
	@CompoundIndex(name="prefNameTitle_needsReview_id", def="{'prefName.title': 1, 'needsReview': 1, '_id': 1}"),
	@CompoundIndex(name="namesTitle_needsReview_id", def="{'names.title': 1, 'needsReview': 1, '_id': 1}")
})
public class Place {

	@Id
	private String id;

	private Set<Link> links = new HashSet<Link>();
	
	private PlaceName prefName;

	private Set<PlaceName> names = new HashSet<PlaceName>();
	
	private Set<String> types = new HashSet<String>();
	
	private Location prefLocation;

	private Set<Location> locations = new HashSet<Location>();

	@Indexed
	private String parent;
	
	@Indexed
	private List<String> grandparents = new ArrayList<String>();

	@Indexed
	private Set<String> relatedPlaces = new HashSet<String>();
	
	private List<Comment> comments = new ArrayList<Comment>();
	
	private Set<String> tags = new HashSet<String>();
	
	private Set<String> provenance = new HashSet<String>();
	
	@Indexed
	private Set<Identifier> ids = new HashSet<Identifier>();
	
	@Indexed
	private boolean needsReview = false;
	
	private boolean deleted = false;
	
	private String replacedBy;
	
	private int children = 0;
	
	private String noteReisestipendium;
	
	private List<Comment> commentsReisestipendium = new ArrayList<Comment>();
	
	@Indexed
	private String recordGroupId;
	
	private Date lastChangeDate;
	
	
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

	public List<String> getGrandparents() {
		return grandparents;
	}

	public void setGrandparents(List<String> grandparents) {
		this.grandparents = grandparents;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Set<String> getTypes() {
		return types;
	}

	public void setTypes(Set<String> types) {
		this.types = types;
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

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	
	public void addComment(Comment comment) {
		this.comments.add(comment);
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}
	
	public void addTag(String tag) {
		this.tags.add(tag);
	}
	
	public Set<String> getProvenance() {
		return provenance;
	}

	public void setProvenance(Set<String> provenance) {
		this.provenance = provenance;
	}
	
	public void addProvenance(String provenance) {
		this.provenance.add(provenance);
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
	
	public String getIdentifier(String context) {
		for (Identifier id : ids)
			if (context.equals(id.getContext()))
				return id.getValue();
		return null;
	}
	
	public String getArachneId() {
		return getIdentifier("arachne-place");
	}
	
	public String getZenonId() {
		return getIdentifier("zenon-thesaurus");
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
				+ ", types=" + types + ", links=" + links
				+ ", locations=" + locations + ", parent=" + parent
				+ ", comments=" + comments + ", tags=" + tags + ", provenance=" + provenance + ", ids=" + ids
				+ ", deleted=" + deleted + ", replacedBy=" + replacedBy + "]";
	}

	public Location getPrefLocation() {
		return prefLocation;
	}

	public void setPrefLocation(Location prefLocation) {
		this.prefLocation = prefLocation;
	}

	public int getChildren() {
		return children;
	}

	public void setChildren(int children) {
		this.children = children;
	}

	public String getNoteReisestipendium() {
		return noteReisestipendium;
	}

	public void setNoteReisestipendium(String noteReisestipendium) {
		this.noteReisestipendium = noteReisestipendium;
	}

	public List<Comment> getCommentsReisestipendium() {
		return commentsReisestipendium;
	}

	public void setCommentsReisestipendium(List<Comment> commentsReisestipendium) {
		this.commentsReisestipendium = commentsReisestipendium;
	}
	
	public String getRecordGroupId() {
		return recordGroupId;
	}

	public void setRecordGroupId(String recordGroupId) {
		this.recordGroupId = recordGroupId;
	}

	public Date getLastChangeDate() {
		return lastChangeDate;
	}

	public void setLastChangeDate(Date lastChangeDate) {
		this.lastChangeDate = lastChangeDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + children;
		result = prime * result
				+ ((comments == null) ? 0 : comments.hashCode());
		result = prime
				* result
				+ ((commentsReisestipendium == null) ? 0
						: commentsReisestipendium.hashCode());
		result = prime * result + (deleted ? 1231 : 1237);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((ids == null) ? 0 : ids.hashCode());
		result = prime * result + ((links == null) ? 0 : links.hashCode());
		result = prime * result
				+ ((locations == null) ? 0 : locations.hashCode());
		result = prime * result + ((names == null) ? 0 : names.hashCode());
		result = prime * result + (needsReview ? 1231 : 1237);
		result = prime
				* result
				+ ((noteReisestipendium == null) ? 0 : noteReisestipendium
						.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result
				+ ((prefLocation == null) ? 0 : prefLocation.hashCode());
		result = prime * result
				+ ((prefName == null) ? 0 : prefName.hashCode());
		result = prime * result
				+ ((relatedPlaces == null) ? 0 : relatedPlaces.hashCode());
		result = prime * result
				+ ((replacedBy == null) ? 0 : replacedBy.hashCode());
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
		result = prime * result + ((provenance == null) ? 0 : provenance.hashCode());
		result = prime * result + ((types == null) ? 0 : types.hashCode());
		result = prime * result + ((recordGroupId == null) ? 0 : recordGroupId.hashCode());
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
		if (children != other.children)
			return false;
		if (comments == null) {
			if (other.comments != null)
				return false;
		} else if (!comments.equals(other.comments))
			return false;
		if (commentsReisestipendium == null) {
			if (other.commentsReisestipendium != null)
				return false;
		} else if (!commentsReisestipendium
				.equals(other.commentsReisestipendium))
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
		if (noteReisestipendium == null) {
			if (other.noteReisestipendium != null)
				return false;
		} else if (!noteReisestipendium.equals(other.noteReisestipendium))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (prefLocation == null) {
			if (other.prefLocation != null)
				return false;
		} else if (!prefLocation.equals(other.prefLocation))
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
		if (provenance == null) {
			if (other.provenance != null)
				return false;
		} else if (!provenance.equals(other.provenance))
			return false;
		if (types == null) {
			if (other.types != null)
				return false;
		} else if (!types.equals(other.types))
			return false;
		if (recordGroupId == null) {
			if (other.recordGroupId != null)
				return false;
		} else if (!recordGroupId.equals(other.recordGroupId))
			return false;
		return true;
	}
	
}
