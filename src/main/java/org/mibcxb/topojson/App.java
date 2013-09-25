package org.mibcxb.topojson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.mibcxb.McException;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Hello world!
 * 
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Start Test!");

        File jsonFile = new File("examples/test.json");
        FileReader reader = null;
        try {
            reader = new FileReader(jsonFile);
            String json = readJson(reader);

            McTopoJSON mctopojson = new McTopoJSON(new McJsonHandler());
            List<Geometry> geometry = mctopojson.decode(json, null);
            System.out.println(geometry.toString());

            mctopojson = new McTopoJSON();
            geometry = mctopojson.decode(json, null);
            System.out.println(geometry.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (McException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String readJson(Reader reader) {
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
            e.printStackTrace();
        }
        return sb.toString();
    }
}
