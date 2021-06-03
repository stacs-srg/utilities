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
package uk.ac.standrews.cs.utilities.m_tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class used to hold the structure of the tree for analysis purposes.
 * 
 * Created by al on 27/10/2017.
 */
public class TreeStructure {

    public HashMap<Integer,Integer> child_distribution = new HashMap<>(); // Used by showStructure to record children size distributions
    public HashMap<Integer,List<Double>> overlap_distribution = new HashMap<>(); // Used by showStructure to record overlaps in children

    public int max_depth = 0;
    public int number_leaves = 0;
    public int number_internals = 0;
    public int max_level_size = 0;
    public long total_tree_size = 0;


    public void recordChildren(int number_of_children) {
        Integer count = child_distribution.get(number_of_children);
        if( count == null ) {
            child_distribution.put(number_of_children,1);
        } else {
            child_distribution.put(number_of_children,count + 1 );
        }
    }


    public void record_overlap(int count, double v) {
        List<Double> list =  overlap_distribution.get(count);
        if( list == null ) {
            list = new ArrayList<>();
            list.add(v);
            overlap_distribution.put(count,list);
        } else {
            list.add(v);
        }
    }

    public void printStats() {

        int total_number_nodes = number_leaves + number_internals;

        System.err.println( "Tree report: ") ;
        System.err.println( "Max level size: " + max_level_size );
        System.err.println( "Max depth: " + max_depth ) ;
        System.err.println( "Number of nodes: " + total_number_nodes ) ;
        System.err.println( "Number of leaves: " + number_leaves ) ;
        System.err.println( "Number of internals: " + number_internals ) ;
        System.err.println( "Total tree size: (excluding LXP Strings and values) " + total_tree_size );
        print_child_distributions();
        print_overlap_distributions();
    }

    public void print_child_distributions() {
        System.err.println( "Children distribution:" );

        int counts = 0;
        int products = 0;
        for( Map.Entry<Integer,Integer> entry : child_distribution.entrySet() ) {
            int size = entry.getKey();
            int count = entry.getValue();
            products += size * count;
            counts += count;
            System.err.println( "\tsize: " +  size + " count: " + count );
        }
        System.err.println( "Average number of children (non leaf nodes) : " + (double) products / counts );

    }

    private void print_overlap_distributions() {
        System.err.println( "Overlaps in Children:" );

        int count = 0;
        double total = 0.0f;

        for( Map.Entry<Integer,List<Double>> entry : overlap_distribution.entrySet() ) {
            int size = entry.getKey();
            List<Double> entries = entry.getValue();
            int entries_count = entries.size();
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            double sub_count = 0;

            for( Double f : entries ) {
                count++;
                sub_count += f;
                total += f;
                if( f > max ) { max = f; };
                if( f < min ) { min = f; };
            }
            System.err.println( "\tsize: " +  size + " min overlaps: " + min + " max overlaps: " + max + " average overlaps: " + (double) sub_count / entries_count );
        }
        System.err.println( "Average overlaps for whole tree: " + (double) total / count );

    }
}
