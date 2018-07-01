/*
 * Copyright 2018 Systems Research Group, University of St Andrews:
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
package uk.ac.standrews.cs.utilities;

/**
 * A progress indicator that prints percentage completion.
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 * @author Ozgur Akgun (ozgur.akgun@st-andrews.ac.uk)
 */
public class PercentageProgressIndicatorStderr extends ProgressIndicator {

    private static final int HUNDRED_PERCENT = 100;

    /**
     * Creates a progress indicator.
     *
     * @param number_of_updates the number of progress updates to be indicated
     */
    public PercentageProgressIndicatorStderr(final int number_of_updates) {
        super(number_of_updates);
    }

    /**
     * Prints the current proportion of completion as a percentage to standard out.
     *
     * @param proportion_complete the current proportion of completion
     */
    public void indicateProgress(final double proportion_complete) {

        System.err.println(Math.round(proportion_complete * HUNDRED_PERCENT) + "% -- elapsed " + String.format("%8.2f seconds", elapsedSeconds()));
    }
}
