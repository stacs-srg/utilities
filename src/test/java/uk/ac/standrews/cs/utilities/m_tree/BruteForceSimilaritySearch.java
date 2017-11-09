/*
 * Copyright 2017 Systems Research Group, University of St Andrews:
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
package uk.ac.standrews.cs.utilities.m_tree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BruteForceSimilaritySearch<T> {

    final Distance<T> distance_metric;

    private List<T> values;
    private float[][] distances;

    private boolean arrays_initialised = false;
    final Comparator<? super DataDistance<T>> distance_comparator = (Comparator<DataDistance<T>>) (o1, o2) -> Float.compare(o1.distance, o2.distance);

    public BruteForceSimilaritySearch(final Distance<T> distance_metric) {

        this.distance_metric = distance_metric;
        values = new ArrayList<>();
    }

    public void add(final T data) {

        values.add(data);
        arrays_initialised = false;
    }

    public boolean contains(final T data) {

        return values.contains(data);
    }

    public DataDistance<T> nearestNeighbour(final T query) {

        return nearestN(query, 1).get(0);
    }

    public List<DataDistance<T>> nearestN(final T query, final int n) {

        initialiseArraysIfNecessary();

        final List<DataDistance<T>> distances_relative_to_query = getDistancesRelativeToQuery(query);
        distances_relative_to_query.sort(distance_comparator);
        return distances_relative_to_query.subList(0, n);
    }

    private List<DataDistance<T>> getDistancesRelativeToQuery(final T query) {

        final List<DataDistance<T>> result = new ArrayList<>();

        int query_index = 0;
        while (!query.equals(values.get(query_index))) {
            query_index++;
        }

        for (int i = 0; i < values.size(); i++) {
            result.add(new DataDistance<>(values.get(i), distances[query_index][i]));
        }

        return result;
    }

    public List<DataDistance<T>> rangeSearch(final T query, final float r) {

        initialiseArraysIfNecessary();

        final List<DataDistance<T>> distances_relative_to_query = getDistancesRelativeToQuery(query);
        distances_relative_to_query.sort(distance_comparator);

        int n = values.size();
        while (distances_relative_to_query.get(n-1).distance > r && n > 0) { n--; }

        return distances_relative_to_query.subList(0, n);
    }

    private void initialiseArraysIfNecessary() {

        if (!arrays_initialised) {

            final int number_of_values = values.size();
            distances = new float[number_of_values][number_of_values];

            for (int i = 0; i < number_of_values; i++) {
                for (int j = 0; j < number_of_values; j++) {
                    distances[i][j] = distance_metric.distance(values.get(i), values.get(j));
                }
            }

            arrays_initialised = true;
        }
    }
}
