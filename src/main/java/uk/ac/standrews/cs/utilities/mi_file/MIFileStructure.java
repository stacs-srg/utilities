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
package uk.ac.standrews.cs.utilities.mi_file;

import java.util.HashMap;
import java.util.Map;

/**
 * A class used to hold the structure of the MIFile for analysis purposes.
 * 
 * Created by al on 30/10/2017.
 */
public class MIFileStructure {

    public final int ks;                 //number of nearest pivots used when searching
    public final int ki;                 //number of nearest pivots used when indexing
    public final int n_ro;               // number of reference objects
    public final int max_pos_diff;       //consider just objects with at most max_pos_diff with respect to the query - AMATO
    public final int number_of_items;    // number of items are in the data set
    public final int number_of_keys;     // the number of keys in the MIFile

    public HashMap<Integer,Integer> list_distribution = new HashMap<>(); // Used by showStructure to record posting list size distributions

    public <T> MIFileStructure( MIFile<T> mif ) {
        this.ks = mif.ks;
        this.ki = mif.ki;
        this.n_ro = mif.n_ro;
        this.max_pos_diff = mif.max_pos_diff;
        this.number_of_items = mif.number_of_items;


        HashMap<T, PostingList<T>> entries = mif.inverted_index;

        this.number_of_keys = entries.keySet().size();

        for( Map.Entry<T,PostingList<T>> entry : entries.entrySet() ) {

            int number_of_entries = entry.getValue().getEntries().size();

            Integer count = list_distribution.get(number_of_entries);
            if( count == null ) {
                list_distribution.put(number_of_entries,1);
            } else {
                list_distribution.put(number_of_entries,count + 1 );
            }
        }
    }

    
    
}
