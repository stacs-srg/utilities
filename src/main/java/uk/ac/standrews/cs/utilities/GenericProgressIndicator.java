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
package uk.ac.standrews.cs.utilities;

import java.util.function.Consumer;

/**
 * Progress indicator that passes on progress step information to a given consumer.
 *
 * @author Masih Hajiarab Derkani (m@derkani.org)
 */
@SuppressWarnings("unused")
public class GenericProgressIndicator extends ProgressIndicator {

    private final Consumer<Double> progress_consumer;

    /**
     * Creates a progress indicator with a given progress consumer.
     *
     * @param number_of_updates the number of progress updates to be indicated
     * @param progress_consumer the progress consumer
     */
    @SuppressWarnings("unused")
    public GenericProgressIndicator(final int number_of_updates, Consumer<Double> progress_consumer) {

        super(number_of_updates);
        this.progress_consumer = progress_consumer;
    }

    /**
     * Indicates progress to a given proportion of completion.
     *
     * @param proportion_complete the proportion complete
     */
    @Override
    public void indicateProgress(final double proportion_complete) {

        if (progress_consumer != null) {
            progress_consumer.accept(proportion_complete);
        }
    }
}
