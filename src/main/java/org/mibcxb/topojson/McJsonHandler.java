package org.mibcxb.topojson;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mibcxb.McException;

/**
 * 
 * @author chenxb
 * @version 1
 * 
 */
public class McJsonHandler implements ITopoJSONHandler {

    public String name() {
        return McJsonHandler.class.getName();
    }

    public Topology decode(String topojson) throws McException {
        if (topojson != null) {
            try {
                JSONObject jsonObj = new JSONObject(topojson);

                Topology topology = new Topology();
                Iterator<?> it = jsonObj.keys();
                while (it.hasNext()) {
                    Object key = it.next();
                    if (key instanceof String) {
                        String name = (String) key;
                        if ("type".equals(name)) {
                            String type = jsonObj.getString(name);
                            topology.setType(type);
                        } else if ("transform".equals(name)) {
                            JSONObject transformObj = jsonObj
                                    .getJSONObject(name);
                            JSONArray scaleArray = transformObj
                                    .getJSONArray("scale");
                            double[] scale = new double[2];
                            for (int i = 0; i < scale.length; i++) {
                                scale[i] = scaleArray.getDouble(i);
                            }
                            JSONArray translateArray = transformObj
                                    .getJSONArray("translate");
                            double[] translate = new double[2];
                            for (int i = 0; i < translate.length; i++) {
                                translate[i] = translateArray.getDouble(i);
                            }

                            Transform transform = new Transform();
                            transform.setScale(scale);
                            transform.setTranslate(translate);
                            topology.setTransform(transform);
                        } else if ("objects".equals(name)) {
                            JSONObject mapObj = jsonObj.getJSONObject(name);
                            HashMap<String, TopoObject> objects = decodeObjects(mapObj);
                            topology.setObjects(objects);
                        } else if ("arcs".equals(name)) {
                            JSONArray arcsArray = jsonObj.getJSONArray(name);
                            int[][][] arcs = new int[arcsArray.length()][][];
                            for (int i = 0; i < arcsArray.length(); i++) {
                                JSONArray arcArray = arcsArray.getJSONArray(i);
                                int[][] arc = new int[arcArray.length()][];
                                for (int j = 0; j < arcArray.length(); j++) {
                                    JSONArray posArray = arcArray
                                            .getJSONArray(j);
                                    int[] position = new int[posArray.length()];
                                    for (int k = 0; k < posArray.length(); k++) {
                                        position[k] = posArray.getInt(k);
                                    }
                                    arc[j] = position;
                                }
                                arcs[i] = arc;
                            }
                            topology.setArcs(arcs);
                        }
                    }
                }
                return topology;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private HashMap<String, TopoObject> decodeObjects(JSONObject mapObj) {
        if (mapObj == null) {
            return null;
        }
        HashMap<String, TopoObject> map = new HashMap<String, TopoObject>();
        Iterator<?> it = mapObj.keys();
        while (it.hasNext()) {
            Object key = it.next();
            if (key instanceof String) {
                String name = (String) key;
                TopoObject topo = null;
                try {
                    JSONObject topoObj = mapObj.getJSONObject(name);
                    topo = decodeTopoObject(topoObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (topo != null) {
                    map.put(name, topo);
                }
            }
        }
        return map;
    }

    private TopoObject decodeTopoObject(JSONObject topoObj) {
        if (topoObj == null) {
            return null;
        }
        try {
            TopoObject topo = null;
            String type = topoObj.getString("type");
            if (GeomType.Point.name.equals(type)) {
                JSONArray coordArray = topoObj.optJSONArray("coordinates");
                if (coordArray != null && coordArray.length() > 0) {
                    Object[] coords = new Object[coordArray.length()];
                    for (int i = 0; i < coords.length; i++) {
                        coords[i] = coordArray.getInt(i);
                    }
                    topo = new TopoObject();
                    topo.setType(type);
                    topo.setCoordinates(coords);
                }
            } else if (GeomType.LineString.name.equals(type)) {
                JSONArray arcsArray = topoObj.optJSONArray("arcs");
                if (arcsArray != null && arcsArray.length() > 0) {
                    Object[] arcs = new Object[arcsArray.length()];
                    for (int i = 0; i < arcs.length; i++) {
                        arcs[i] = arcsArray.getInt(i);
                    }
                    topo = new TopoObject();
                    topo.setType(type);
                    topo.setArcs(arcs);
                }
            } else if (GeomType.Polygon.name.equals(type)) {
                JSONArray arcsArray = topoObj.optJSONArray("arcs");
                if (arcsArray != null && arcsArray.length() > 0) {
                    Object[] arcs = new Object[arcsArray.length()];
                    for (int i = 0; i < arcs.length; i++) {
                        JSONArray arcArray = arcsArray.getJSONArray(i);
                        if (arcArray.length() > 0) {
                            List<Integer> arc = new ArrayList<Integer>();
                            for (int j = 0; j < arcArray.length(); j++) {
                                arc.add(arcArray.getInt(j));
                            }
                            arcs[i] = arc;
                        }
                    }
                    topo = new TopoObject();
                    topo.setType(type);
                    topo.setArcs(arcs);
                }
            } else if (GeomType.MultiPoint.name.equals(type)) {
                // TODO
            } else if (GeomType.MultiLineString.name.equals(type)) {
                // TODO
            } else if (GeomType.MultiPolygon.name.equals(type)) {
                // TODO
            } else if (GeomType.GeometryCollection.name.equals(type)) {
                JSONArray children = topoObj.optJSONArray("geometries");
                if (children != null && children.length() > 0) {
                    TopoObject[] geometries = new TopoObject[children.length()];
                    for (int i = 0; i < geometries.length; i++) {
                        JSONObject childObj = children.optJSONObject(i);
                        TopoObject child = decodeTopoObject(childObj);
                        if (child != null) {
                            geometries[i] = child;
                        }
                    }
                    topo = new TopoObject();
                    topo.setType(type);
                    topo.setGeometries(geometries);
                }
            }

            if (topo != null) {
                int id = topoObj.optInt("id");
                topo.setId(id);

                JSONObject propObj = topoObj.optJSONObject("properties");
                HashMap<String, Object> properties = decodeProperties(propObj);
                topo.setProperties(properties);
            }
            return topo;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private HashMap<String, Object> decodeProperties(JSONObject propObj) {
        if (propObj == null) {
            return null;
        }
        HashMap<String, Object> map = new HashMap<String, Object>();
        Iterator<?> it = propObj.keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            JSONObject subPropObj = propObj.optJSONObject(key);
            if (subPropObj == null) {
                Object value = propObj.opt(key);
                if (value != null) {
                    map.put(key, value);
                }
            } else {
                Object subProp = decodeProperties(subPropObj);
                if (subProp != null) {
                    map.put(key, subProp);
                }
            }
        }
        return map;
    }

    public Topology decode(Reader reader) throws McException {
        if (reader == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        try {
            char[] buf = new char[2048];
            int len = 0;
            while (-1 != (len = reader.read(buf))) {
                sb.append(buf, 0, len);
            }
        } catch (IOException e) {
            throw new McException(e);
        }
        return decode(sb.toString());
    }

    public String encode(Topology topology) throws McException {
        // TODO Auto-generated method stub
        return null;
    }

}
