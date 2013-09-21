package org.mibcxb.topojson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
            McTopoJSON mctopojson = new McTopoJSON();
            List<Geometry> geometry = mctopojson.decode(reader, null);

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
}
