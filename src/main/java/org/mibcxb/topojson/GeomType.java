package org.mibcxb.topojson;

/**
 * 
 * @author chenxb
 * @version 1
 */
public enum GeomType {
    Point("Point"), LineString("LineString"), Polygon("Polygon"), MultiPoint(
            "MultiPoint"), MultiLineString("MultiLineString"), MultiPolygon(
            "MultiPolygon"), GeometryCollection("GeometryCollection");

    public final String name;

    private GeomType(String name) {
        this.name = name;
    }
}
