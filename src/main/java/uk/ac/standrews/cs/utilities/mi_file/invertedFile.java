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
package uk.ac.standrews.cs.utilities.mi_file;

import java.util.HashMap;

/**
 * Created by al on 11/10/2017.
 */
public class InvertedFile<T> {

    private HashMap<T,PostingList<T>> map = new HashMap<>();
    final private int maxScore;     // the highest score you can get in a posting list (equal to no. ref objects).
    final private int lexiconSize;  // number of reference used for searching

    /**
     *
     * @param maxScore - the number of reference objects
     * @param lexiconSize -
     */
    public InvertedFile(int maxScore, int lexiconSize ) {
        this.maxScore = maxScore;
        this.lexiconSize = lexiconSize;
    }

    public synchronized void insert(T key, T data, int score) throws Exception {

        if (score < 0 || score > maxScore) {
            throw new Exception("Score associated to the entry being inserted must be in the interval " + 0 + ".." + maxScore);
        }

        PostingListEntry ple = new PostingListEntry( data,score );
        PostingList<T> list = map.get( key );
        if( list == null ) {
            list = new PostingList();
            map.put(key,list);
        }
        list.add( ple );
    }


    public PostingList<T> getPostingList(T pivot, int from, int to) {
        PostingList<T> list = map.get( pivot );
        if( list != null ) {
            list = list.getSubRange( from, to );
        }
        return list;
    }

}
