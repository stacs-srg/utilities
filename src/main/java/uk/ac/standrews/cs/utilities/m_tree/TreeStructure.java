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

import java.util.HashMap;

/**
 * A class used to hold the structure of the tree for analysis purposes.
 * 
 * Created by al on 27/10/2017.
 */
public class TreeStructure {

    public HashMap<Integer,Integer> child_distribution = new HashMap<>(); // Used by showStructure to record children size distributions
    public int max_depth = 0;
    public int number_leaves = 0;
    public int number_internals = 0;

    public void recordChildren(int size) {
        Integer count = child_distribution.get(size);
        if( count == null ) {
            child_distribution.put(size,1);
        } else {
            count++;
        }
    }
    
    
}
