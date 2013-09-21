package org.mibcxb.topojson;

import java.util.Arrays;
import java.util.HashMap;

/**
 * 
 * @author chenxb
 * 
 */
public class Topology {
    private String type;
    private Transform transform;
    private HashMap<String, TopoObject> objects;
    private int[][][] arcs;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Transform getTransform() {
        return transform;
    }

    public void setTransform(Transform transform) {
        this.transform = transform;
    }

    public HashMap<String, TopoObject> getObjects() {
        return objects;
    }

    public void setObjects(HashMap<String, TopoObject> objects) {
        this.objects = objects;
    }

    public int[][][] getArcs() {
        return arcs;
    }

    public void setArcs(int[][][] arcs) {
        this.arcs = arcs;
    }

    @Override
    public String toString() {
        return "McTopology [type=" + type + ", transform=" + transform
                + ", objects=" + objects + ", arcs=" + Arrays.toString(arcs)
                + "]";
    }
}
