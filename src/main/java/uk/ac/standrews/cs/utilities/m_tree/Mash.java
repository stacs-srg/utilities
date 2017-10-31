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

import uk.ac.standrews.cs.utilities.lsh.MinHash;

import java.util.List;
import java.util.Set;

/**
 * Created by al on 28/09/2017.
 */
public class Mash<T> extends MTree<T> {

    private final MinHash minHash;
    private final KeyMaker km;

    /**
     * Creates a hybrid MTree with associated LSH with a given key generator and distance function
     * @param distance - the distance function to use in the MTree.
     * @param km - a key mapper which generates a key from a given value of type T.
     */
    public Mash(Distance<T> distance, KeyMaker km ) {
        super(distance);
        minHash = new MinHash<Node>();
        this.km = km;
    }

    /**
     * Add some data to the MTree
     *
     * @param data the data to be added to the tree
     */
    @Override
    public void add(T data) {
        String key = km.makeKey(data);
        Set<Node> node_hints = minHash.getClosest(key);
        Node mTreeNode = addWithHint( data, node_hints); // add the data to the MTree.
        minHash.put( key,mTreeNode ); // save the new node in the lsh structure.
    }

    /**
     * return the nearest neighbour to the query
     *
     * @param query - some data for which to find the nearest neighbour
     * @return the nearest neighbour of T.
     */
    @Override
    public DataDistance<T> nearestNeighbour(T query) {
        String key = km.makeKey(query);
        Set<Node> nodes = minHash.getClosest(key);
        return nnWithHint( query, nodes );
    }

    /**
     * Find the closest N nodes to @param query.
     *
     * @param query - some data for which to find the nearest N neighbours
     * @param n     the number of neighbours to return
     * @return n neighbours (or as many as possible)
     */
    @Override
    public List<DataDistance<T>> nearestN(T query, int n) {
        String key = km.makeKey(query);
        Set<Node> node_hints = minHash.getClosest(key);

        if( node_hints.isEmpty() ) { // just add normally                       // copied from nearestNeighbour above
            return super.nearestN( query,n );
        }

        // We have some help so try and jump to the right place.
        // TODO Problem: how to choose the best from the Set?
        // TODO for now just choose the first
        // TODO could have a look see and choose a good one here!
        Node node = node_hints.iterator().next();
        node = refineCandidate(node,query);
        ClosestSet results = new ClosestSet(n);
        super.nearestN( node, n, query, results );
        return results.values();
    }

    //---------------------------------------------------------------------------------------
    //----------------------------------- Private methods -----------------------------------
    //---------------------------------------------------------------------------------------

    /**
     * Add some data to the MTree
     * @param data the data to be added to the tree
     * @param node_hints - some nodes that are likely to be close to the right insertion point.
     */
    private Node addWithHint(T data, Set<Node> node_hints ) {

        if( node_hints.isEmpty() ) { // just add normally
            if( root == null ) { // new tree
                super.add(data);  // NUM_ENTRIES INCREMENTED HERE - but not in helper functions.
                return root;
            } else {
                num_entries++;
                // no hints but we have a root
                return super.add( root,data );
            }
        }
        // We have some help so try and jump to the right place.
        // TODO Problem: how to choose the best from the Set?
        // TODO for now just choose the first
        // TODO could have a look see and choose a good one here!
        num_entries++;
        Node node = node_hints.iterator().next();
        return insert_zoom(node,data);
    }

    private Node insert_zoom(Node candidate, T data) {

        // There are THREE possibilities:
        //  1. We are at the root - insert at the root.
        //  2. We are inside the circle - need to move back up tree to highest enclosing node
        //  3. We are outside of the circle - need to move back up the tree until we are inside the first found.

        if( candidate == root ) {   // case 1 easy.
            return add( candidate,data );
        }

        double distance_to_target = distance_wrapper.distance(candidate.data, data);

        if( distance_to_target <= candidate.radius ) {   // case 2 we are inside where we need to be - move up.

            while(candidate.parent != null /*at root */ && distance_to_target < candidate.parent.radius) {
                // this look moves us up the tree and stops at root or largest covering radius.
                candidate = candidate.parent; // move back up the tree.
                distance_to_target = distance_wrapper.distance(candidate.data, data);
            }
            return super.add(candidate, data);
        }
        // case 3 we are outside the covering radius, so move up the tree till we are inside.
        while( candidate.parent != null && distance_to_target > candidate.radius ) {
            // same code as while loop above but different condition.
            candidate = candidate.parent; // move back up the tree.
            distance_to_target = distance_wrapper.distance(candidate.data, data);
        }
        return super.add(candidate, data);
    }

    private DataDistance<T> nnWithHint(T query, Set<Node> node_hints) {
        if( node_hints.isEmpty() ) { // just add normally
            return super.nearestNeighbour( root,null,query );
        }
        // We have some help so try and jump to the right place.
        // TODO Problem: how to choose the best from the Set?
        // TODO for now just choose the first
        // TODO could have a look see and choose a good one here!
        Node node = node_hints.iterator().next();
        return nearestNeighbour_zoom( node,query );
    }

    private DataDistance<T> nearestNeighbour_zoom(Node candidate, T query) {
        // There are THREE possibilities:
        //  1. We are at the root - search at the root.
        //  2. We are outside of the circle - need to move back up the tree until we are inside the first found.

        //  3. We are inside the circle - need to move back up tree to highest enclosing node

        if( candidate == root ) {   // Case 1 easy.
            return super.nearestNeighbour(query);
        }

        float distance_to_target = distance_wrapper.distance(candidate.data, query);

        // Case 2 we are outside the covering radius, so move up the tree till we are inside.
        if( distance_to_target > candidate.radius ) {
            while( candidate.parent != null && distance_to_target >= candidate.radius ) {
                // same code as while loop above but different condition.
                candidate = candidate.parent; // move back up the tree.
                distance_to_target = distance_wrapper.distance(candidate.data, query);
            }
            return super.nearestNeighbour(candidate, new DataDistance(candidate.data, distance_to_target), query);
        }

        // if (distance_to_node - node.radius < closest_thus_far.distance)
        // By definition the parent also satisfies this condition (nesting of radii).
        // Need to see if parent is closer.

        while(parent_closer(candidate,distance_to_target,query)) {
                // this look moves us up the tree and stops at root or largest covering radius.
                candidate = candidate.parent; // move back up the tree.
                distance_to_target = distance_wrapper.distance(candidate.data, query);
        }
        return super.nearestNeighbour(candidate, null , query); // new DataDistance(candidate.data, distance_to_target)
    }

    private Node refineCandidate(Node candidate, T query) {
        // There are THREE possibilities:
        //  1. We are at the root - search at the root.
        //  2. We are outside of the circle - need to move back up the tree until we are inside the first found.

        //  3. We are inside the circle - need to move back up tree to highest enclosing node

        if( candidate == root ) {   // Case 1 easy.
            return root;
        }

        double distance_to_target = distance_wrapper.distance(candidate.data, query);

        // Case 2 we are outside the covering radius, so move up the tree till we are inside.
        if( distance_to_target > candidate.radius ) {
            while( candidate.parent != null && distance_to_target >= candidate.radius ) {
                // same code as while loop above but different condition.
                candidate = candidate.parent; // move back up the tree.
                distance_to_target = distance_wrapper.distance(candidate.data, query);
            }
            return candidate;
        }

        // if (distance_to_node - node.radius < closest_thus_far.distance)
        // By definition the parent also satisfies this condition (nesting of radii).
        // Need to see if parent is closer.

        while(parent_closer(candidate,distance_to_target,query)) {
            // this look moves us up the tree and stops at root or largest covering radius.
            candidate = candidate.parent; // move back up the tree.
            distance_to_target = distance_wrapper.distance(candidate.data, query);
        }
        return candidate;
    }

    /**
     * @param node - a node to examine to see if the parent if closer to a query
     * @param distance_to_node - the distance from the node to the query
     * @param query - the query we are examining for parental closeness
     * @return true if the parent is closer to the query than the node.
     */
    private boolean parent_closer( Node node, double distance_to_node, T query  ) {
        Node parent = node.parent;
        if( parent == null ) {
            return false;
        }
        double parent_distance = distance_wrapper.distance(parent.data, query);
        return parent_distance <= distance_to_node && parent_distance < parent.radius;
    }



}