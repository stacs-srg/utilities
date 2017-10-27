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
