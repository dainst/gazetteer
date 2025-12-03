package org.dainst.gazetteer.search;

import java.util.Arrays;
import org.elasticsearch.geometry.Geometry;
import org.elasticsearch.geometry.LinearRing;
import org.elasticsearch.geometry.Polygon;

public class Queries {

    private Queries() {}

    public static Geometry polygonFromCoordinates(double[][] lngLatPairs) {
        final double[] x = Arrays.stream(lngLatPairs)
            .mapToDouble(lngLat -> lngLat[0])
            .toArray();
        final double[] y = Arrays.stream(lngLatPairs)
            .mapToDouble(lngLat -> lngLat[1])
            .toArray();
        return new Polygon(new LinearRing(x, y));
    }
}
