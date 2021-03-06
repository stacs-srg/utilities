/*
 * Copyright 2021 Systems Research Group, University of St Andrews:
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

/**
 * @author graham
 */
@SuppressWarnings("unused")
public class ExceptionFactory {

    public static Exception makeLabelledException(final Exception e) {

        final String caller_name = Diagnostic.getMethodInCallChain(2);

        return new Exception() {

            public String getMessage() {
                return caller_name + " - " + e.getMessage();
            }

            public StackTraceElement[] getStackTrace() {
                return e.getStackTrace();
            }

            public void printStackTrace() {
                e.printStackTrace();
            }
        };
    }

    public static Exception makeLabelledException(String message) {

        String caller_name = Diagnostic.getMethodInCallChain(2);

        return new Exception(caller_name + " - " + message);
    }
}
