package org.dainst.gazetteer.search;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.elasticsearch.geometry.LinearRing;
import org.elasticsearch.geometry.Polygon;
import org.junit.jupiter.api.Test;

public class QueriesTest {

    @Test
    public void polygonFromCoordinates_shouldConstructPolygon() {
        double[][] coordinates = {
            { -104.603875, 39.670111 },
            { -104.603875, 40.000000 },
            { -105.000000, 40.000000 },
            { -105.000000, 39.670111 },
            { -104.603875, 39.670111 },
        };

        var result = Queries.polygonFromCoordinates(coordinates);

        var expected = new Polygon(
            new LinearRing(
                new double[] {
                    -104.603875,
                    -104.603875,
                    -105.000000,
                    -105.000000,
                    -104.603875,
                },
                new double[] {
                    39.670111,
                    40.000000,
                    40.000000,
                    39.670111,
                    39.670111,
                }
            )
        );
        assertEquals(expected, result);
    }
}
