/**
 * 
 */
package org.mibcxb.topojson;

import java.io.Reader;

import org.mibcxb.McException;

/**
 * @author chenxb
 * 
 */
public interface ITopoJSONHandler {
    String name();

    Topology decode(String topojson) throws McException;

    Topology decode(Reader reader) throws McException;

    String encode(Topology topology) throws McException;
}
