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
package uk.ac.standrews.cs.utilities;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract progress indicator class. Subclasses define how an amount of progress is displayed.
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
public abstract class ProgressIndicator {

    private final AtomicInteger number_of_updates = new AtomicInteger();
    private final AtomicInteger number_of_steps_since_last_update = new AtomicInteger();
    private final AtomicInteger total_steps = new AtomicInteger();
    private final AtomicInteger number_of_steps_completed = new AtomicInteger();
    private final AtomicInteger number_of_steps_per_update = new AtomicInteger();
    private final long startTime;

    private double proportion_complete;

    /**
     * Creates a progress indicator.
     *
     * @param number_of_updates the number of progress updates to be indicated
     */
    @SuppressWarnings("WeakerAccess")
    public ProgressIndicator(final int number_of_updates) {

        this.number_of_updates.set(number_of_updates);
        number_of_steps_since_last_update.set(0);
        this.startTime = System.nanoTime();
    }

    protected double elapsedSeconds() {
        long elapsedNano = System.nanoTime() - this.startTime;
        return (double) elapsedNano / 1e9;
    }

    /**
     * Sets the total number of steps that represent completion.
     *
     * @param total_steps the total number of steps
     */
    public void setTotalSteps(final int total_steps) {

        this.total_steps.set(total_steps);
        number_of_steps_per_update.set(number_of_updates.get() <= 0 ? Integer.MAX_VALUE : total_steps / number_of_updates.get());
    }

    /**
     * Records another progress step.
     */
    public void progressStep() {

        number_of_steps_completed.getAndIncrement();
        number_of_steps_since_last_update.getAndIncrement();

        if (number_of_steps_per_update.get() > 0 && number_of_steps_since_last_update.get() >= number_of_steps_per_update.get()) {

            proportion_complete = Math.min((double) number_of_steps_completed.get() / (double) total_steps.get(), 1.0);
            number_of_steps_since_last_update.set(0);

            indicateProgress(proportion_complete);
        }
    }

    /**
     * Gets the current proportion of completion.
     *
     * @return the proportion of completion, between 0 and 1
     */
    @SuppressWarnings("unused")
    public double getProportionComplete() {

        return proportion_complete;
    }

    /**
     * Indicates the current proportion of completion.
     *
     * @param proportion_complete the current proportion of completion
     */
    public abstract void indicateProgress(final double proportion_complete);
}
