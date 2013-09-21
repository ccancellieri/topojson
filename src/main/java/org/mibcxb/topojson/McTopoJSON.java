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
                return parseFromTopology(topology, factory);
            }
        }
        return null;
    }

    public List<Geometry> decode(Reader reader, GeometryFactory factory)
            throws McException {
        if (handler != null) {
            Topology topology = handler.decode(reader);
            if (topology != null) {
                return parseFromTopology(topology, factory);
            }
        }
        return null;
    }

    public String encode(List<Geometry> list) throws McException {
        if (handler != null) {
            // TODO
        }
        return null;
    }

    public static List<Geometry> parseFromTopology(Topology topology,
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
        List<Coordinate[]> coordArcs = transformArcs(transform, arcs);

        HashMap<String, TopoObject> objects = topology.getObjects();

        List<Geometry> geometries = null;
        try {
            geometries = new ArrayList<Geometry>();
            for (String name : objects.keySet()) {
                TopoObject object = objects.get(name);
                geometries.add(parseFromObject(object, transform, coordArcs,
                        factory));
            }
        } catch (Exception e) {
            throw new McException(e);
        }
        return geometries;
    }

    private static Geometry parseFromObject(TopoObject object,
            Transform transform, List<Coordinate[]> coordArcs,
            GeometryFactory factory) {
        if (object == null || transform == null) {
            return null;
        }

        String type = object.getType();
        Geometry geometry = null;
        if ("Point".equals(type)) {
            int[] position = object.getCoordinates();
            Coordinate coordinate = transformCoordinate(transform, position);
            if (coordinate != null) {
                geometry = factory.createPoint(coordinate);
            }
        } else if ("MultiPoint".equals(type)) {
            // TODO
        } else if ("LineString".equals(type)) {
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
        } else if ("MultiLineString".equals(type)) {
            // TODO
        } else if ("Polygon".equals(type)) {
            Object[] arcs = object.getArcs();
            if (arcs != null) {
                if (arcs.length == 1) {
                    List<Double> indexes = (ArrayList<Double>) arcs[0];
                    for (int i = 0; i < indexes.size(); i++) {
                        int index = (int) Math.round((Double) indexes.get(i));
                        if (index < 0) {
                            index += coordArcs.size();
                        }
                        Coordinate[] arc = coordArcs.get(index);
                        LinearRing shell = factory.createLinearRing(arc);
                        if (shell != null && shell.isValid()) {
                            geometry = factory.createPolygon(shell);
                        }
                    }
                } else {
                    LinearRing shell = null;
                    List<Double> shellIndex = (ArrayList<Double>) arcs[0];
                    for (int i = 0; i < shellIndex.size(); i++) {
                        int index = (int) Math
                                .round((Double) shellIndex.get(i));
                        if (index < 0) {
                            index += coordArcs.size();
                        }
                        Coordinate[] arc = coordArcs.get(index);
                        shell = factory.createLinearRing(arc);
                    }

                    LinearRing[] holes = new LinearRing[arcs.length - 1];
                    for (int i = 1; i < arcs.length; i++) {
                        LinearRing hole = null;
                        List<Double> holeIndex = (ArrayList<Double>) arcs[i];
                        for (int j = 0; j < holeIndex.size(); j++) {
                            int index = (int) Math.round((Double) holeIndex
                                    .get(j));
                            if (index < 0) {
                                index += coordArcs.size();
                            }
                            Coordinate[] arc = coordArcs.get(index);
                            hole = factory.createLinearRing(arc);
                            holes[i - 1] = hole;
                        }
                    }

                    if (shell != null && shell.isValid() && holes != null) {
                        geometry = factory.createPolygon(shell, holes);
                    }
                }
            }
        } else if ("MultiPolygon".equals(type)) {
            // TODO
        } else if ("GeometryCollection".equals(type)) {
            TopoObject[] subs = object.getGeometries();
            Geometry[] geometries = null;
            if (subs != null) {
                geometries = new Geometry[subs.length];
                for (int i = 0; i < geometries.length; i++) {
                    geometries[i] = parseFromObject(subs[i], transform,
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

    private static Coordinate transformCoordinate(Transform transform,
            int[] posision) {
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

    private static List<Coordinate[]> transformArcs(Transform transform,
            int[][][] arcs) {
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

}
