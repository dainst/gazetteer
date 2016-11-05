package org.dainst.gazetteer.converter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.dainst.gazetteer.domain.Location;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.helpers.TempFolderService;
import org.dainst.gazetteer.helpers.ZipArchiveBuilder;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

@Component
public class ShapefileCreator {
	
	private final static Logger logger = LoggerFactory.getLogger("org.dainst.gazetteer.converter.ShapefileCreator");

	@Autowired
	private TempFolderService tempFolderHelper;
	
	private SimpleFeatureType pointType;
	private SimpleFeatureType multiPolygonType;
	
	public File createShapefiles(String filename, Place place) throws Exception {
		
		List<Place> places = new ArrayList<Place>();
		places.add(place);
		
		return createShapefiles(filename, places);
	}
	
	public File createShapefiles(String filename, List<Place> places) throws Exception {
		
		if (pointType == null || multiPolygonType == null) {
			createFeatureTypes();
		}
		
		logger.debug("Create shapefiles for " + places.size() + " places");
		
		File tempFolder = tempFolderHelper.createFolder();		
		File shapeFileFolder = new File(tempFolder.getAbsolutePath() + File.separator + filename);
		shapeFileFolder.mkdir();
		
		createShapefile(places, shapeFileFolder, pointType);
		createShapefile(places, shapeFileFolder, multiPolygonType);
		
		File zipFile = ZipArchiveBuilder.buildZipArchiveFromFolder(shapeFileFolder, tempFolder);
		
		return zipFile;
	}
	
	public void removeShapefileData(File shapefile) throws IOException {
		
		File folder = new File(shapefile.getParent());
		FileUtils.deleteDirectory(folder);
	}
	
	private void createFeatureTypes() throws Exception {
		
		String pointSchema = "the_geom:Point:srid=4326,gaz_id:Integer";
		String multiPolygonSchema = "the_geom:MultiPolygon:srid=4326,gaz_id:Integer";

		try {
			pointType = DataUtilities.createType("point", pointSchema);
			multiPolygonType = DataUtilities.createType("multipolygon", multiPolygonSchema);
		} catch (SchemaException e) {
			throw new Exception("Failed to create feature types for shapefile export", e);
		}
	}
	
	private void createShapefile(List<Place> places, File folder, SimpleFeatureType featureType) throws Exception {
			
		String outputFilePath = folder.getAbsolutePath() + File.separator + featureType.getTypeName() + "s.shp";
		File outputFile = new File(outputFilePath);
		
		MemoryDataStore memoryDataStore = createMemoryDataStore(places, featureType);		
		
		if (memoryDataStore != null) {
			logger.debug("Write shapefile for feature type " + featureType.getTypeName());
			writeShapefile(memoryDataStore, outputFile);
		} else {
			logger.debug("No features of feature type " + featureType.getTypeName());
		}
	}
	
	private MemoryDataStore createMemoryDataStore(List<Place> places, SimpleFeatureType featureType) throws Exception {
		
		MemoryDataStore memoryDataStore = new MemoryDataStore();
		memoryDataStore.createSchema(featureType);
		
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		
		Transaction transaction = Transaction.AUTO_COMMIT;
		
		boolean dataStoreEmpty = true;
		
		for (Place place : places) {
									
			SimpleFeatureBuilder featureBuilder = null;
			
			if (featureType.equals(pointType)) {
				
				if (place.getPrefLocation() == null || (place.getPrefLocation().getCoordinates()) == null || place.getPrefLocation().getCoordinates().length < 2) {
					continue;
				}
				
				featureBuilder = new SimpleFeatureBuilder(pointType);	
				featureBuilder.add(createPointGeometry(place.getPrefLocation(), geometryFactory));
			} else if (featureType.equals(multiPolygonType)) {
								
				if (place.getPrefLocation() == null || (place.getPrefLocation().getShape() == null)) {
					continue;
				}				
				
				featureBuilder = new SimpleFeatureBuilder(multiPolygonType);
				MultiPolygon multiPolygon = createMultiPolygonGeometry(place.getPrefLocation(), geometryFactory);
				if (multiPolygon != null) {
					featureBuilder.add(multiPolygon);
				} else {
					continue;
				}
			}
			
			featureBuilder.add(Integer.parseInt(place.getId()));
			
			SimpleFeature feature = featureBuilder.buildFeature(null);
			memoryDataStore.addFeature(feature);
			dataStoreEmpty = false;
			
			try {
				transaction.commit();
			} catch (Exception e) {
				transaction.rollback();
				transaction.close();
				throw new Exception("Could not write feature for place " + place.getId() + " and featureType " + featureType.getTypeName(), e);
			}
			
			featureBuilder = null;				
		}
		
		transaction.close();
		transaction = null;
		
		if (!dataStoreEmpty) {		
			return memoryDataStore;
		} else {
			return null;
		}
	}
		
	private void writeShapefile(MemoryDataStore memoryDataStore, File outputFile) throws Exception {
		
		SimpleFeatureSource featureSource = memoryDataStore.getFeatureSource(memoryDataStore.getTypeNames()[0]);
		SimpleFeatureType featureType = featureSource.getSchema();

		Map<String, java.io.Serializable> creationParams = new HashMap<String, java.io.Serializable>();
		creationParams.put("url", DataUtilities.fileToURL(outputFile));

		FileDataStoreFactorySpi factory = FileDataStoreFinder.getDataStoreFactory("shp");
		DataStore dataStore = factory.createNewDataStore(creationParams);

		dataStore.createSchema(featureType);

		SimpleFeatureStore featureStore = (SimpleFeatureStore) dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
		
		Transaction transaction = new DefaultTransaction();
		try {
			SimpleFeatureCollection collection = featureSource.getFeatures();
			featureStore.addFeatures(collection);
			transaction.commit();
		} catch (IOException e) {
			try {
				transaction.rollback();
				throw new Exception("Failed to commit data to feature store", e);
			} catch (IOException e2 ) {
				logger.error("Failed to commit data to feature store", e);
				throw new Exception("Transaction rollback failed", e2);
			}			
		} finally {
			transaction.close();
		}
	}
	
	private Point createPointGeometry(Location location, GeometryFactory geometryFactory) {
		
		return geometryFactory.createPoint(new Coordinate(location.getCoordinates()[0], location.getCoordinates()[1]));
	}
	
	private MultiPolygon createMultiPolygonGeometry(Location location, GeometryFactory geometryFactory) {
		
		double[][][][] coordinates = location.getShape().getCoordinates();
		
		Polygon[] polygons = new Polygon[coordinates.length];
		
		for (int i = 0; i < coordinates.length; i++) {			
			double[][][] polygonCoordinates = coordinates[i];			
			LinearRing[] linearRings = new LinearRing[polygonCoordinates.length];
			
			for (int j = 0; j < polygonCoordinates.length; j++) {				
				double[][] linearRingCoordinates = polygonCoordinates[j];				
				Coordinate[] points = new Coordinate[linearRingCoordinates.length];
				
				for (int k = 0; k < linearRingCoordinates.length; k++) {					
					double[] pointCoordinates = linearRingCoordinates[k];					
					points[k] = new Coordinate(pointCoordinates[0], pointCoordinates[1]);
				}
				
				linearRings[j] = geometryFactory.createLinearRing(points);
			}
						
			if (linearRings.length == 0) return null;
			
			LinearRing shell = linearRings[0];
			
			LinearRing[] holes = null;			
			if (linearRings.length > 1) {
				holes = Arrays.copyOfRange(linearRings, 1, linearRings.length);
			}
			
			polygons[i] = geometryFactory.createPolygon(shell, holes);
		}
		
		return geometryFactory.createMultiPolygon(polygons);
	}

}
