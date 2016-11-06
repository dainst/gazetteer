package org.dainst.gazetteer.converter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.dainst.gazetteer.dao.RecordGroupRepository;
import org.dainst.gazetteer.domain.Identifier;
import org.dainst.gazetteer.domain.Link;
import org.dainst.gazetteer.domain.Location;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceName;
import org.dainst.gazetteer.domain.RecordGroup;
import org.dainst.gazetteer.helpers.PlaceAccessService;
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
	
	@Autowired
	private RecordGroupRepository recordGroupRepository;
	
	private SimpleFeatureType pointType;
	private SimpleFeatureType multiPolygonType;
	
	private static final String dataSchema =
				"gazId:String,"
			+   "pNameTitl:String,"
			+   "pNameLang:String,"
			+   "pNameAnc:Boolean,"
			+   "pNameTrns:Boolean,"
			+   "name1Titl:String,"
			+   "name1Lang:String,"
			+   "name1Anc:Boolean,"
			+   "name1Trns:Boolean,"
			+   "name2Titl:String,"
			+   "name2Lang:String,"
			+   "name2Anc:Boolean,"
			+   "name2Trns:Boolean,"
			+   "name3Titl:String,"
			+   "name3Lang:String,"
			+   "name3Anc:Boolean,"
			+   "name3Trns:Boolean,"
			+   "name4Titl:String,"
			+   "name4Lang:String,"
			+   "name4Anc:Boolean,"
			+   "name4Trns:Boolean,"
			+   "name5Titl:String,"
			+   "name5Lang:String,"
			+   "name5Anc:Boolean,"
			+   "name5Trns:Boolean,"
			+	"type1:String,"
			+	"type2:String,"
			+	"type3:String,"
			+	"type4:String,"
			+	"type5:String,"
			+	"altitude:Double,"
			+	"locConfid:Integer,"
			+	"publicSite:Boolean,"
			+	"parent:Integer,"
			+	"related1:String,"
			+	"related2:String,"
			+	"related3:String,"
			+	"related4:String,"
			+	"related5:String,"
			+	"related6:String,"
			+	"related7:String,"
			+	"related8:String,"
			+	"related9:String,"
			+	"related10:String,"
			+	"tag1:String,"
			+	"tag2:String,"
			+	"tag3:String,"
			+	"tag4:String,"
			+	"tag5:String,"
			+	"tag6:String,"
			+	"tag7:String,"
			+	"tag8:String,"
			+	"tag9:String,"
			+	"tag10:String,"
			+	"prov1:String,"
			+	"prov2:String,"
			+	"prov3:String,"
			+	"prov4:String,"
			+	"prov5:String,"
			+	"prov6:String,"
			+	"prov7:String,"
			+	"prov8:String,"
			+	"prov9:String,"
			+	"prov10:String,"
			+	"id1Contxt:String,"
			+	"id1Value:String,"
			+	"id2Contxt:String,"
			+	"id2Value:String,"
			+	"id3Contxt:String,"
			+	"id3Value:String,"
			+	"id4Contxt:String,"
			+	"id4Value:String,"
			+	"id5Contxt:String,"
			+	"id5Value:String,"
			+	"id6Contxt:String,"
			+	"id6Value:String,"
			+	"id7Contxt:String,"
			+	"id7Value:String,"
			+	"id8Contxt:String,"
			+	"id8Value:String,"
			+	"id9Contxt:String,"
			+	"id9Value:String,"
			+	"id10Contxt:String,"
			+	"id10Value:String,"
			+	"link1Pred:String,"
			+	"link1Obj:String,"
			+	"link2Pred:String,"
			+	"link2Obj:String,"
			+	"link3Pred:String,"
			+	"link3Obj:String,"
			+	"link4Pred:String,"
			+	"link4Obj:String,"
			+	"link5Pred:String,"
			+	"link5Obj:String,"
			+	"link6Pred:String,"
			+	"link6Obj:String,"
			+	"link7Pred:String,"
			+	"link7Obj:String,"
			+	"link8Pred:String,"
			+	"link8Obj:String,"
			+	"link9Pred:String,"
			+	"link9Obj:String,"
			+	"link10Pred:String,"
			+	"link10Obj:String,"
			+	"comment1:String,"
			+	"comment2:String,"
			+	"comment3:String,"
			+	"comment4:String,"
			+	"comment5:String,"
			+	"recGroup:String,"
			+	"changeDate:Date";
	
	public File createShapefiles(String filename, Place place) throws Exception {
		
		List<Place> places = new ArrayList<Place>();
		places.add(place);
		
		return createShapefiles(filename, places, null);
	}
	
	public File createShapefiles(String filename, List<Place> places, Map<String, PlaceAccessService.AccessStatus> placeAccessMap) throws Exception {
		
		if (pointType == null || multiPolygonType == null) {
			createFeatureTypes();
		}
		
		logger.debug("Create shapefiles for " + places.size() + " places");
		
		File tempFolder = tempFolderHelper.createFolder();		
		File shapeFileFolder = new File(tempFolder.getAbsolutePath() + File.separator + filename);
		shapeFileFolder.mkdir();
		
		createShapefile(places, placeAccessMap, shapeFileFolder, pointType);
		createShapefile(places, placeAccessMap, shapeFileFolder, multiPolygonType);
		
		File zipFile = ZipArchiveBuilder.buildZipArchiveFromFolder(shapeFileFolder, tempFolder);
		
		return zipFile;
	}
	
	public void removeShapefileData(File shapefile) throws IOException {
		
		File folder = new File(shapefile.getParent());
		FileUtils.deleteDirectory(folder);
	}
	
	private void createFeatureTypes() throws Exception {
		
		String pointSchema = "the_geom:Point:srid=4326," + dataSchema;
		String multiPolygonSchema = "the_geom:MultiPolygon:srid=4326," + dataSchema;

		try {
			pointType = DataUtilities.createType("point", pointSchema);
			multiPolygonType = DataUtilities.createType("multipolygon", multiPolygonSchema);
		} catch (SchemaException e) {
			throw new Exception("Failed to create feature types for shapefile export", e);
		}
	}
	
	private void createShapefile(List<Place> places, Map<String, PlaceAccessService.AccessStatus> placeAccessMap, File folder,
			SimpleFeatureType featureType) throws Exception {
			
		String outputFilePath = folder.getAbsolutePath() + File.separator + featureType.getTypeName() + "s.shp";
		File outputFile = new File(outputFilePath);
		
		MemoryDataStore memoryDataStore = createMemoryDataStore(places, placeAccessMap, featureType);
		
		if (memoryDataStore != null) {
			logger.debug("Write shapefile for feature type " + featureType.getTypeName());
			writeShapefile(memoryDataStore, outputFile);
		} else {
			logger.debug("No features of feature type " + featureType.getTypeName());
		}
	}
	
	private MemoryDataStore createMemoryDataStore(List<Place> places, Map<String, PlaceAccessService.AccessStatus> placeAccessMap,
			SimpleFeatureType featureType) throws Exception {
		
		MemoryDataStore memoryDataStore = new MemoryDataStore();
		memoryDataStore.createSchema(featureType);
		
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		
		Transaction transaction = Transaction.AUTO_COMMIT;
		
		boolean dataStoreEmpty = true;
		
		for (Place place : places) {
			
			if (placeAccessMap != null && !PlaceAccessService.hasReadAccess(placeAccessMap.get(place.getId()))) {
				continue;
			}
									
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
			
			fillFeatureFields(place, place.getPrefLocation(), featureBuilder);
			
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
	
	private void fillFeatureFields(Place place, Location location, SimpleFeatureBuilder featureBuilder) {
		
		// Gazetteer ID
		featureBuilder.add(Integer.parseInt(place.getId()));
		
		// Prefered name
		if (place.getPrefName() != null) {
			featureBuilder.add(place.getPrefName().getTitle());
			featureBuilder.add(place.getPrefName().getLanguage());
			featureBuilder.add(place.getPrefName().isAncient());
			featureBuilder.add(place.getPrefName().isTransliterated());
		} else {
			for (int i = 0; i < 2; i++) featureBuilder.add("");
			for (int i = 0; i < 2; i++) featureBuilder.add(false);
		}
		
		// Alternative names
		if (place.getNames() != null) {
			List<PlaceName> alternativeNames = new ArrayList<PlaceName>(place.getNames());
			for (int i = 0; i < 5; i++) {
				if (i >= alternativeNames.size()) {
					for (int j = 0; j < 2; j++) featureBuilder.add("");
					for (int j = 0; j < 2; j++) featureBuilder.add(false);
				} else {
					featureBuilder.add(alternativeNames.get(i).getTitle());
					featureBuilder.add(alternativeNames.get(i).getLanguage());
					featureBuilder.add(alternativeNames.get(i).isAncient());
					featureBuilder.add(alternativeNames.get(i).isTransliterated());
				}
			}
		} else {
			for (int i = 0; i < 5; i++) {
				for (int j = 0; j < 2; j++) featureBuilder.add("");
				for (int j = 0; j < 2; j++) featureBuilder.add(false);
			}
		}
		
		// Types
		addFeatureEntries(featureBuilder, place.getTypes(), 5);
		
		// Location
		if (location.getAltitude() != null) {
			featureBuilder.add(location.getAltitude());
		} else {
			featureBuilder.add(Double.NaN);
		}
		featureBuilder.add(location.getConfidence());
		featureBuilder.add(location.isPublicSite());
		
		// Parent
		if (place.getParent() != null) {
			featureBuilder.add(place.getParent());
		} else {
			featureBuilder.add("");
		}
		
		// Related places
		addFeatureEntries(featureBuilder, place.getRelatedPlaces(), 10);
		
		// Tags
		addFeatureEntries(featureBuilder, place.getTags(), 10);
		
		// Provenance tags
		addFeatureEntries(featureBuilder, place.getProvenance(), 10);
		
		// Identifiers
		if (place.getIdentifiers() != null) {
			List<Identifier> identifiers = new ArrayList<Identifier>(place.getIdentifiers());
			for (int i = 0; i < 10; i++) {
				if (i >= identifiers.size()) {
					for (int j = 0; j < 2; j++) featureBuilder.add("");
				} else {
					featureBuilder.add(identifiers.get(i).getContext());
					featureBuilder.add(identifiers.get(i).getValue());
				}
			}
		} else {
			for (int i = 0; i < 20; i++) featureBuilder.add("");
		}
		
		// Links
		if (place.getLinks() != null) {
			List<Link> links = new ArrayList<Link>(place.getLinks());
			for (int i = 0; i < 10; i++) {
				if (i >= links.size()) {
					for (int j = 0; j < 2; j++) featureBuilder.add("");
				} else {
					featureBuilder.add(links.get(i).getPredicate());
					featureBuilder.add(links.get(i).getObject());
				}
			}
		} else {
			for (int i = 0; i < 20; i++) featureBuilder.add("");
		}
		
		// Comments
		if (place.getComments() != null) {
			for (int i = 0; i < 5; i++) {
				if (i >= place.getComments().size()) {
					featureBuilder.add("");
				} else {
					featureBuilder.add(place.getComments().get(i).getText());
				}
			}
		} else {
			for (int i = 0; i < 5; i++) featureBuilder.add("");
		}
		
		// Record group
		if (place.getRecordGroupId() != null) {
			RecordGroup recordGroup = recordGroupRepository.findOne(place.getRecordGroupId());
			if (recordGroup != null) {
				featureBuilder.add(recordGroup.getName());
			} else {
				featureBuilder.add("");
			}
		} else {
			featureBuilder.add("");
		}
		
		// Last change date
		if (place.getLastChangeDate() != null) {
			featureBuilder.add(place.getLastChangeDate());
		} else {
			Calendar calendar = new GregorianCalendar();
			calendar.setTimeInMillis(0);
			featureBuilder.add(calendar.getTime());
		}
		
	}
	
	private void addFeatureEntries(SimpleFeatureBuilder featureBuilder, Set<String> set, int size) {
		
		if (set != null) {
			List<String> entries = new ArrayList<String>(set);
			for (int i = 0; i < size; i++) {
				if (i >= entries.size()) {
					featureBuilder.add("");
				} else {
					featureBuilder.add(entries.get(i));
				}
			}
		} else {
			for (int i = 0; i < size; i++) featureBuilder.add("");
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
