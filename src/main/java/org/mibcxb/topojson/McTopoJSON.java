package org.mibcxb.topojson;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mibcxb.McException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * 
 * @author chenxb
 * @version 1
 */
public class McTopoJSON {
    private ITopoJSONHandler handler;

    public McTopoJSON() {
        super();
        this.handler = new McGsonHandler();
    }

    public McTopoJSON(ITopoJSONHandler handler) {
        super();
        if (handler == null) {
            throw new IllegalArgumentException("The handler cannot be NULL.");
        }
        this.handler = handler;
    }

    public ITopoJSONHandler getHandler() {
        return handler;
    }

    public void setHandler(ITopoJSONHandler handler) {
        this.handler = handler;
    }

    public List<Geometry> decode(String topojson, GeometryFactory factory)
            throws McException {
        if (handler != null) {
            Topology topology = handler.decode(topojson);
            if (topology != null) {
                return fromTopology(topology, factory);
            }
        }
        return null;
    }

    public List<Geometry> decode(Reader reader, GeometryFactory factory)
            throws McException {
        if (handler != null) {
            Topology topology = handler.decode(reader);
            if (topology != null) {
                return fromTopology(topology, factory);
            }
        }
        return null;
    }

    public String encode(List<Geometry> list) throws McException {
        if (handler != null) {
            Topology topology = toTopology(list);
            if (topology != null) {
                return handler.encode(topology);
            }
        }
        return null;
    }

    public List<Geometry> fromTopology(Topology topology,
            GeometryFactory factory) throws McException {
        if (topology == null) {
            return null;
        }
        if (factory == null) {
            factory = new GeometryFactory();
        }
        String type = topology.getType();
        if (!"Topology".equals(type)) {
            return null;
        }
        Transform transform = topology.getTransform();

        int[][][] arcs = topology.getArcs();
        List<Coordinate[]> coordArcs = decodeArcs(transform, arcs);

        HashMap<String, TopoObject> objects = topology.getObjects();

        List<Geometry> geometries = null;
        try {
            geometries = new ArrayList<Geometry>();
            for (String name : objects.keySet()) {
                TopoObject object = objects.get(name);
                geometries.add(decodeTopoObject(object, transform, coordArcs,
                        factory));
            }
        } catch (Exception e) {
            throw new McException(e);
        }
        return geometries;
    }

    private Geometry decodeTopoObject(TopoObject object, Transform transform,
            List<Coordinate[]> coordArcs, GeometryFactory factory) {
        if (object == null || transform == null) {
            return null;
        }

        String type = object.getType();
        Geometry geometry = null;
        if (GeomType.Point.name.equals(type)) {
            int[] position = object.getCoordinates();
            Coordinate coordinate = decodePosition(transform, position);
            if (coordinate != null) {
                geometry = factory.createPoint(coordinate);
            }
        } else if (GeomType.LineString.name.equals(type)) {
            Object[] arcs = object.getArcs();
            if (arcs != null) {
                for (int i = 0; i < arcs.length; i++) {
                    int index = (int) Math.round((Double) arcs[i]);
                    if (index < 0) {
                        index += coordArcs.size();
                    }
                    Coordinate[] arc = coordArcs.get(index);
                    geometry = factory.createLineString(arc);
                }
            }
        } else if (GeomType.Polygon.name.equals(type)) {
            Object[] arcs = object.getArcs();
            if (arcs != null && arcs.length >= 1) {
                LinearRing shell = null;
                if (arcs[0] instanceof List<?>) {
                    List<?> shellIndex = (List<?>) arcs[0];
                    for (int i = 0; i < shellIndex.size(); i++) {
                        if (shellIndex.get(i) instanceof Double) {
                            Integer index = object2Integer(shellIndex.get(i));
                            if (index != null) {
                                if (index < 0) {
                                    index += coordArcs.size();
                                }
                                Coordinate[] arc = coordArcs.get(index);
                                shell = factory.createLinearRing(arc);
                            }
                        }
                    }
                }

                LinearRing[] holes = null;
                int holeCount = arcs.length - 1;
                if (holeCount > 0) {
                    holes = new LinearRing[holeCount];
                    for (int i = 1; i < arcs.length; i++) {
                        LinearRing hole = null;
                        if (arcs[i] instanceof List<?>) {
                            List<?> holeIndex = (List<?>) arcs[i];
                            for (int j = 0; j < holeIndex.size(); j++) {
                                if (holeIndex.get(j) instanceof Double) {
                                    Integer index = object2Integer(holeIndex
                                            .get(j));
                                    if (index != null) {
                                        if (index < 0) {
                                            index += coordArcs.size();
                                        }
                                        Coordinate[] arc = coordArcs.get(index);
                                        hole = factory.createLinearRing(arc);
                                        holes[i - 1] = hole;
                                    }
                                }
                            }
                        }
                    }
                }

                if (shell != null) {
                    if (holes != null) {
                        geometry = factory.createPolygon(shell, holes);
                    } else {
                        geometry = factory.createPolygon(shell);
                    }
                }
            }
        } else if (GeomType.MultiPoint.name.equals(type)) {
            // TODO
        } else if (GeomType.MultiLineString.name.equals(type)) {
            // TODO
        } else if (GeomType.MultiPolygon.name.equals(type)) {
            // TODO
        } else if (GeomType.GeometryCollection.name.equals(type)) {
            TopoObject[] subs = object.getGeometries();
            Geometry[] geometries = null;
            if (subs != null) {
                geometries = new Geometry[subs.length];
                for (int i = 0; i < geometries.length; i++) {
                    geometries[i] = decodeTopoObject(subs[i], transform,
                            coordArcs, factory);
                }
            }
            if (geometries != null) {
                geometry = factory.createGeometryCollection(geometries);
            }
        }
        Object properties = object.getProperties();
        if (geometry != null && properties != null) {
            geometry.setUserData(properties);
        }
        return geometry;
    }

    private Coordinate decodePosition(Transform transform, int[] posision) {
        if (transform == null || posision == null) {
            return null;
        }
        if (posision.length > 2) {
            return null;
        }
        final double[] scale = transform.getScale();
        final double[] translate = transform.getTranslate();

        double x = 0, y = 0;
        x = posision[0] * scale[0] + translate[0];
        y = posision[1] * scale[1] + translate[1];
        return new Coordinate(x, y);
    }

    private List<Coordinate[]> decodeArcs(Transform transform, int[][][] arcs) {
        if (transform == null || arcs == null) {
            return null;
        }
        final double[] scale = transform.getScale();
        final double[] translate = transform.getTranslate();

        List<Coordinate[]> coordList = new ArrayList<Coordinate[]>();
        for (int i = 0; i < arcs.length; i++) {
            int[][] arc = arcs[i];
            Coordinate[] coordArc = new Coordinate[arc.length];
            double x = 0, y = 0;
            double dx = 0, dy = 0;
            for (int j = 0; j < arc.length; j++) {
                x = (dx += arc[j][0]) * scale[0] + translate[0];
                y = (dy += arc[j][1]) * scale[1] + translate[1];
                coordArc[j] = new Coordinate(x, y);
            }
            coordList.add(coordArc);
        }
        return coordList;
    }

    public Topology toTopology(List<Geometry> list) {
        return null;
    }

    public static Integer object2Integer(Object obj) {
        Integer value = null;
        if (obj instanceof Integer) {
            value = (Integer) obj;
        } else if (obj instanceof Long) {
            value = ((Long) obj).intValue();
        } else if (obj instanceof Float) {
            Float f = (Float) obj;
            value = Math.round(f);
        } else if (obj instanceof Double) {
            Double d = (Double) obj;
            value = (int) Math.round(d);
        } else if (obj instanceof String) {
            String s = (String) obj;
            try {
                value = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                value = null;
            }
        }
        return value;
    }
}
