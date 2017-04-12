/**
 * Created on Aug 31, 2005 at 2:58:47 PM.
 */
package uk.ac.standrews.cs.utilities.archive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author graham
 */
public class CommandLineInput {

    static BufferedReader reader;

    static {
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    public static String readLine() {

        try {
            return reader.readLine();
        } catch (IOException e) {
            return "";
        }
    }
}
