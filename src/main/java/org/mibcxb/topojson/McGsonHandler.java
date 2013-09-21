package org.mibcxb.topojson;

import java.io.Reader;

import org.mibcxb.McException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class McGsonHandler implements ITopoJSONHandler {
    private static final Gson GSON = new Gson();

    public String name() {
        return McGsonHandler.class.getName();
    }

    public Topology decode(String topojson) throws McException {
        Topology topology = null;
        try {
            topology = GSON.fromJson(topojson, Topology.class);
        } catch (JsonSyntaxException e) {
            throw new McException(e);
        }
        return topology;
    }

    public Topology decode(Reader reader) throws McException {
        Topology topology = null;
        try {
            topology = GSON.fromJson(reader, Topology.class);
        } catch (JsonSyntaxException e) {
            throw new McException(e);
        }
        return topology;
    }

    public String encode(Topology topology) throws McException {
        // TODO Auto-generated method stub
        return null;
    }
}
