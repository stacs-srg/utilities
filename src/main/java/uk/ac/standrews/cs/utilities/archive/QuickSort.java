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

import java.util.Comparator;
import java.util.List;

/**
 * Performs quicksort. Adapted from code by Cay Horstmann.
 *
 * @param <Element> the element type
 */
@SuppressWarnings("unused")
@Deprecated
public class QuickSort<Element> {

    private final List<Element> list;
    private final Comparator<Element> comparator;

    /**
     * Initialises a sorter for a given list and comparator.
     *
     * @param list       the list
     * @param comparator the comparator
     */
    public QuickSort(final List<Element> list, final Comparator<Element> comparator) {

        this.list = list;
        this.comparator = comparator;
    }

    /**
     * Sorts the list.
     */
    public void sort() {

        sort(0, list.size() - 1);
    }

    private void sort(final int low, final int high) {

        if (low < high) {

            final int p = partition(low, high);
            sort(low, p);
            sort(p + 1, high);
        }
    }

    private int partition(final int low, final int high) {

        final Element pivot = list.get(low);
        int i = low - 1;
        int j = high + 1;

        while (i < j) {
            i++;
            while (comparator.compare(list.get(i), pivot) < 0) {
                i++;
            }

            j--;
            while (comparator.compare(list.get(j), pivot) > 0) {
                j--;
            }
            if (i < j) {
                swap(i, j);
            }
        }
        return j;
    }

    private void swap(final int index1, final int index2) {

        final Element temp = list.get(index1);
        list.set(index1, list.get(index2));
        list.set(index2, temp);
    }
}
