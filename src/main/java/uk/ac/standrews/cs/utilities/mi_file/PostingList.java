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
package uk.ac.standrews.cs.utilities.mi_file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by al on 11/10/2017.
 */
public class PostingList<T> {

    private HashMap<Integer,ArrayList<PostingListEntry<T>>> map; // maps from positions to posting lists at that distance.

    public PostingList() {
        map = new HashMap<>();
    }

    private PostingList(PostingList<T> oldPostingList, int from, int to) {
        this();
        for( int index = from; index <= to; index++ ) {  // copy the old list to the new.
            ArrayList<PostingListEntry<T>> old = oldPostingList.map.get(index);
            ArrayList<PostingListEntry<T>> copy = new ArrayList<>();
            if( old != null ) {
                for (PostingListEntry<T> entry : old) {
                    copy.add(entry);
                }
            }
            map.put( index, copy);
        }
    }

    public void add(PostingListEntry ple) {
        int score = ple.getScore();
        ArrayList<PostingListEntry<T>> list = map.get(score);
        if( list == null ) {
            list = new ArrayList<PostingListEntry<T>>();
            map.put( score,list );
        }
        list.add( ple );
    }

    public <T> PostingList<T> getSubRange(int from, int to) {
        return new PostingList( this, from, to );
    }

    public List<PostingListEntry<T>> getEntries() {
        List<PostingListEntry<T>> result = new ArrayList<>();

        for( Map.Entry<Integer, ArrayList<PostingListEntry<T>>> entry : map.entrySet() ) {
            List<PostingListEntry<T>> list = entry.getValue();
            if( list != null ) {
                result.addAll(list);
            }
        }
        return result;
    }

}
