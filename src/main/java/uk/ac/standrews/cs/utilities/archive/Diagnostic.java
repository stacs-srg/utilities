/*
 * Copyright 2019 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module utilities.
 *
 * utilities is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * utilities is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with utilities. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.utilities.archive;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Provides support for various diagnostic output.
 * <p>
 * A global threshold diagnostic level may be set by the user; the default value
 * is NONE, the highest level. Each call to produce diagnostic output is
 * parameterised by a diagnostic level. The output is only actually generated if
 * the given level is higher than or equal to the current global threshold
 * level. For example, if the global threshold is set to FULL then all output
 * will be generated, while if the global threshold is set to NONE then only
 * calls that also specify the level NONE will produce output.
 * <p>
 * The ordering of levels is FULL {@literal < } INIT {@literal < } RUN {@literal < } RUNALL {@literal < } RESULT {@literal < } FINAL {@literal < } NONE.
 *
 * @author al, graham
 */
public class Diagnostic {

    /**
     * The lowest diagnostic level.
     */
    public static Diagnostic FULL = new Diagnostic(0, "FULL");

    /**
     * An intermediate diagnostic level.
     */
    @SuppressWarnings("WeakerAccess")
    public static Diagnostic INIT = new Diagnostic(1, "INIT");

    /**
     * An intermediate diagnostic level.
     */
    @SuppressWarnings("unused")
    public static Diagnostic RUN = new Diagnostic(2, "RUN");

    /**
     * An intermediate diagnostic level.
     */
    @SuppressWarnings("unused")
    public static Diagnostic RUNALL = new Diagnostic(3, "RUNALL");

    /**
     * An intermediate diagnostic level.
     */
    @SuppressWarnings("unused")
    public static Diagnostic RESULT = new Diagnostic(4, "RESULT");

    /**
     * An intermediate diagnostic level.
     */
    @SuppressWarnings("unused")
    public static Diagnostic FINAL = new Diagnostic(5, "FINAL");

    /**
     * The highest diagnostic level.
     */
    @SuppressWarnings("unused")
    public static Diagnostic NONE = new Diagnostic(6, "NONE");

    // ********************************************************************************************************************************************/

    private static boolean local_reporting = true;
    private static Diagnostic threshold = NONE;
    private static final String DIAGNOSTIC_CLASS_NAME = Diagnostic.class.getName();
    private static final String ERROR_CLASS_NAME = Error.class.getName();

    private final int level_value;
    private final String level_description;

    /**
     * Sets the global threshold diagnostic level.
     *
     * @param level the new level
     */
    @SuppressWarnings("unused")
    public static void setLevel(Diagnostic level) {

        threshold = level;
    }

    /**
     * Gets the current global threshold diagnostic level.
     *
     * @return the current level
     */
    @SuppressWarnings("unused")
    public static Diagnostic getLevel() {

        return threshold;
    }

    /**
     * Tests the current reporting threshold.
     *
     * @param level a reporting level
     * @return true if the given level is greater than or equal to the current reporting threshold
     */
    @SuppressWarnings("unused")
    public static boolean aboveTraceThreshold(Diagnostic level) {

        return level.level_value >= threshold.level_value;
    }

    // ********************************************************************************************************************************************/

    /**
     * Outputs trace information if the specified level is equal or higher to the current reporting threshold.
     *
     * @param level the trace level
     */
    @SuppressWarnings("unused")
    public static void trace(Diagnostic level) {

        outputTrace(getMethodInCallChain(), level, true);
    }

    /**
     * Outputs trace information.
     *
     * @param msg a descriptive message
     */
    @SuppressWarnings("WeakerAccess")
    public static void trace(@SuppressWarnings("SameParameterValue") String msg) {

        outputTrace(getMethodInCallChain() + " : " + msg, true);
    }

    /**
     * Outputs trace information if the specified level is equal or higher to the current reporting threshold.
     *
     * @param msg   a descriptive message
     * @param level the trace level
     */
    public static void trace(String msg, Diagnostic level) {

        outputTrace(getMethodInCallChain() + " : " + msg, level, true);
    }

    /**
     * Outputs trace information if the specified level is equal or higher to the current reporting threshold.
     *
     * @param msg1  a descriptive message
     * @param msg2  another message
     * @param level the trace level
     */
    @SuppressWarnings("unused")
    public static void trace(String msg1, String msg2, Diagnostic level) {

        outputTrace(getMethodInCallChain() + " : " + msg1 + msg2, level, true);
    }

    /**
     * Outputs trace information if the specified level is equal or higher to the current reporting threshold,
     * without a trailing newline.
     *
     * @param module the module from which this call has been made
     * @param level  the trace level
     */
    @SuppressWarnings("unused")
    public static void traceNoLn(String module, Diagnostic level) {

        outputTrace(module, level, false);
    }

    /**
     * Outputs trace information if the specified level is equal or higher to the current reporting threshold,
     * without including the source location.
     *
     * @param msg   a descriptive message
     * @param level the trace level
     */
    @SuppressWarnings("unused")
    public static void traceNoSource(String msg, Diagnostic level) {

        outputTrace(msg, level, true);
        outputTrace(msg, true);
    }

    @SuppressWarnings("unused")
    public static void traceNoSource(String msg) {

        outputTrace(msg, true);
    }

    /**
     * Outputs trace information if the specified level is equal or higher to the current reporting threshold,
     * without a trailing newline, and without including the source location.
     *
     * @param msg   a descriptive message
     * @param level the trace level
     */
    @SuppressWarnings("unused")
    public static void traceNoSourceNoLn(String msg, Diagnostic level) {

        outputTrace(msg, level, false);
    }

    // ********************************************************************************************************************************************/

    /**
     * Toggles whether diagnostic messages should be output to local standard output.
     *
     * @param local_reporting true if messages should be output locally
     */
    @SuppressWarnings("unused")
    public static void setLocalErrorReporting(boolean local_reporting) {

        Diagnostic.local_reporting = local_reporting;
    }

    /**
     * Returns information on the most recent user method in the current call chain.
     *
     * @return returns information on the most recent user method in the current call chain
     */
    @SuppressWarnings("WeakerAccess")
    public static String getMethodInCallChain() {

        // Get a stack trace.
        StackTraceElement[] trace = new Exception().getStackTrace();

        // Ignore calls within Diagnostic or Error class.
        // Start from 1 - depth 0 is this method.
        for (int i = 1; i < trace.length; i++) {

            StackTraceElement call = trace[i];
            String calling_class_name = call.getClassName();

            if (!calling_class_name.equals(DIAGNOSTIC_CLASS_NAME) && !calling_class_name.equals(ERROR_CLASS_NAME))
                return calling_class_name + "::" + call.getMethodName();
        }

        return "";
    }

    /**
     * Returns information on one of the methods in the current call chain.
     *
     * @param depth the depth in the current chain, where 1 corresponds to the method calling this one.
     * @return a string containing the class and method name of the corresponding call
     */
    @SuppressWarnings("WeakerAccess")
    public static String getMethodInCallChain(@SuppressWarnings("SameParameterValue") int depth) {

        // Get a stack trace.
        StackTraceElement[] trace = new Exception().getStackTrace();

        if (trace.length > depth) return trace[depth].getClassName() + "::" + trace[depth].getMethodName();
        else return "";
    }

    /**
     * Prints a stack trace.
     */
    @SuppressWarnings("unused")
    public static void printStackTrace() {

        // Get a stack trace.
        StackTraceElement[] trace = new Exception().getStackTrace();

        // Ignore calls within Diagnostic or Error class.
        // Start from 1 - depth 0 is this method.
        for (int i = 1; i < trace.length; i++) {

            StackTraceElement call = trace[i];
            String calling_class_name = call.getClassName();

            if (!calling_class_name.equals(DIAGNOSTIC_CLASS_NAME) && !calling_class_name.equals(ERROR_CLASS_NAME))
                System.out.println(calling_class_name + "::" + call.getMethodName() + " line " + call.getLineNumber());
        }
    }

    /**
     * @see Object#toString()
     */
    public String toString() {

        return level_description;
    }

    // ********************************************************************************************************************************************/

    private Diagnostic(int level_value, String level_description) {

        this.level_value = level_value;
        this.level_description = level_description;
    }

    // ********************************************************************************************************************************************/

    private static void outputTrace(String message, Diagnostic level, boolean new_line) {

        // Synchronize with respect to the Error methods too.
        synchronized (Error.class) {
            if (level.level_value >= threshold.level_value)
                outputTrace(message, new_line);
        }
    }

    private static void outputTrace(String message, boolean new_line) {

        // Synchronise with respect to the Error methods too.
        synchronized (Error.class) {

            if (local_reporting) {
                if (new_line) {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
                    Date date = new Date();
                    System.out.print(dateFormat.format(date) + " :: ");
                }
                System.out.print(message);
                if (new_line)
                    System.out.println();
            }
        }
    }
}
