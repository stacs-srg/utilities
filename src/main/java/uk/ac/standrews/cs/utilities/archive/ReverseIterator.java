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
