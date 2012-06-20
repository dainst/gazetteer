package org.dainst.gazetteer.domain;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.annotations.Type;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;

@Entity
public class Location {
	
	private long id;
	private String description;
	private Point point;
	private Polygon polygon;
	private Place place;
	private Date lastModified;
	private Date created;
	
	public Location() {
		created = new Date();
	}
	
	public Location(double lat, double lng) {
		PackedCoordinateSequenceFactory factory = new PackedCoordinateSequenceFactory(PackedCoordinateSequenceFactory.DOUBLE);
		CoordinateSequence coordinates = factory.create(new double[]{lat, lng}, 2);
		point = new Point(coordinates, new GeometryFactory(factory));
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

	@Type(type="org.hibernatespatial.GeometryUserType")
	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}

	@Type(type="org.hibernatespatial.GeometryUserType")
	public Polygon getPolygon() {
		return polygon;
	}

	public void setPolygon(Polygon polygon) {
		this.polygon = polygon;
	}

	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE})
	public Place getPlace() {
		return place;
	}

	public void setPlace(Place place) {
		this.place = place;
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
	
	@Transient
	public double getLat() {
		return point.getCoordinateSequence().getX(0);
	}
	
	@Transient
	public double getLng() {
		return point.getCoordinateSequence().getY(0);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
