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
package uk.ac.standrews.cs.utilities.metrics.implementation;

import java.util.Objects;

public class QgramDistribution implements Comparable<QgramDistribution> {

    public String key;
    public double count; // count is used for both counts and frequencies depending on context.

    public QgramDistribution(String key) {
        this.key = key;
        this.count = 1;
    }

    public QgramDistribution(String key, double count) {
        this.key = key;
        this.count = count;
    }

    /**
     * @param o - value to be compared
     * @return true if o is a QgramDistribution with the same key (NOTE count ignored)
     */
    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QgramDistribution that = (QgramDistribution) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {

        return Objects.hash(key);
    }

    @Override
    public int compareTo(QgramDistribution other) {
        return key.compareTo(other.key);
    }
}
