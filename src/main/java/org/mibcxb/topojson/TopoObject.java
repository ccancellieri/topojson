package org.mibcxb.topojson;

import java.util.Arrays;
import java.util.HashMap;

public class TopoObject {
    private String type;
    private Object[] coordinates;
    private Object[] arcs;
    private int id;
    private TopoObject[] geometries;
    private HashMap<String, Object> properties;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Object[] coordinates) {
        this.coordinates = coordinates;
    }

    public Object[] getArcs() {
        return arcs;
    }

    public void setArcs(Object[] arcs) {
        this.arcs = arcs;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TopoObject[] getGeometries() {
        return geometries;
    }

    public void setGeometries(TopoObject[] geometries) {
        this.geometries = geometries;
    }

    public HashMap<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "McGeometry [type=" + type + ", coordinates="
                + Arrays.toString(coordinates) + ", arcs=" + arcs + ", id="
                + id + ", geometries=" + Arrays.toString(geometries)
                + ", properties=" + properties + "]";
    }
}
