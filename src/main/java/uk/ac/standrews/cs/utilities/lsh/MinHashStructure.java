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
package uk.ac.standrews.cs.utilities.lsh;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A class used to hold the structure of a MinHash data structure for analysis purposes.
 * 
 * Created by al on 30/10/2017.
 */
public class MinHashStructure {

    public final int shingle_size;
    public final int signature_size;
    public final int band_size;
    public final int number_of_keys;


    public HashMap<Integer,Integer> list_distribution = new HashMap<>(); // Used by showStructure to record posting list size distributions

    public <T> MinHashStructure( MinHash<T> mh ) {

        shingle_size = mh.shingle_size;
        signature_size = mh.signature_size;
        band_size = mh.band_size;

        HashMap<Band, Set<T>> entries = mh.lsh_map;

        this.number_of_keys = entries.keySet().size();

        for( Map.Entry<Band, Set<T>> entry : entries.entrySet() ) {

            int number_of_entries = entry.getValue().size();

            Integer count = list_distribution.get(number_of_entries);
            if( count == null ) {
                list_distribution.put(number_of_entries,1);
            } else {
                list_distribution.put(number_of_entries,count + 1 );
            }
        }
    }

    
    
}
