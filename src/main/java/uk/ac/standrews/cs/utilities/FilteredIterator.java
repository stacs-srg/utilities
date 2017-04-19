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
package uk.ac.standrews.cs.utilities;

import java.util.Iterator;
import java.util.function.Predicate;

public class FilteredIterator<T> implements Iterator<T> {

    private final Iterator<T> iterator;
    private final Predicate<T> predicate;

    private T next = null;

    @SuppressWarnings("WeakerAccess")
    public FilteredIterator(final Iterator<T> iterator, final Predicate<T> Predicate) {

        this.iterator = iterator;
        this.predicate = Predicate;

        loadNext();
    }

    private void loadNext() {

        if (iterator.hasNext()) {
            next = iterator.next();
            while (iterator.hasNext() && !predicate.test(next)) {
                next = iterator.next();
            }
            if (!predicate.test(next)) {
                next = null;
            }
        } else {
            next = null;
        }
    }

    @Override
    public boolean hasNext() {

        return next != null;
    }

    @Override
    public T next() {

        T data = next;
        loadNext();

        return data;
    }

    @Override
    public void remove() {

        throw new UnsupportedOperationException("remove");
    }
}
