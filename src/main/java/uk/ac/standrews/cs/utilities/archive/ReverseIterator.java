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
/*
 * Created on Dec 20, 2004 at 2:27:32 PM.
 */
package uk.ac.standrews.cs.utilities.archive;

import java.util.Iterator;
import java.util.List;

/**
 * Reverse iterator over a list.
 *
 * @param <T> the element type
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
public class ReverseIterator<T> implements Iterator<T> {

    private int index;
    private final List<T> list;

    /**
     * Initialises the iterator.
     *
     * @param list the list to be reversed
     */
    public ReverseIterator(final List<T> list) {

        this.list = list;
        index = list.size() - 1;
    }

    @Override
    public boolean hasNext() {

        return index >= 0;
    }

    @Override
    public T next() {

        return list.get(index--);
    }

    @Override
    public void remove() {

        throw new UnsupportedOperationException();
    }
}
