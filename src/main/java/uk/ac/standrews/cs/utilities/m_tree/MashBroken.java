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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A hybrid class of an Mtree with an accelerator in the form of an LSH
 * Created by al on 28/09/2017.
 */
public class MashBroken<T> extends MTree<T> {

    private final MinHash minHash;
    private final KeyMaker km;

    /**
     * Creates a hybrid MTree with associated LSH with a given key generator and distance function
     * @param distance - the distance function to use in the MTree.
     * @param km - a key mapper which generates a key from a given value of type T.
     */
    public MashBroken(Distance<T> distance, KeyMaker km ) {
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
        Set<Node> node_hints = minHash.getClosest(key);
        if( node_hints.isEmpty() ) { // just add normally
            return super.nearestNeighbour( root,null,query );
        }
        // TODO Problem: how to choose the best from the Set?
        // TODO for now just choose the first
        // TODO could have a look see and choose a good one here!
        Node node = node_hints.iterator().next();
        node = findStartPoint(node,query);
        DataDistance dd = new DataDistance(node.data, distance_wrapper.distance(node.data, query));
        return super.nearestNeighbour(node ,dd,query);
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

        // We have some help so try and jump to the right place.
        // TODO Problem: how to choose the best from the Set?
        // TODO for now just choose the first
        // TODO could have a look see and choose a good one here!
        String key = km.makeKey(query);
        Set<Node> node_hints = minHash.getClosest(key);
        Node node = node_hints.iterator().next();
        node = findStartPoint(node,query);
        ClosestSet results = new ClosestSet(n);
        super.nearestN(node , n, query, results );
        return results.values();
    }

    /**
     * Find the nodes withing @param r of @param T.
     *
     * @param query - some data for which to find the neighbours within distance r
     * @param r     the distance from query over which to search
     * @return all those nodes within r of @param T.
     */
    @Override
    public List<DataDistance<T>> rangeSearch(T query, float r) {

        // We have some help so try and jump to the right place.
        // TODO Problem: how to choose the best from the Set?
        // TODO for now just choose the first
        // TODO could have a look see and choose a good one here!
        String key = km.makeKey(query);
        Set<Node> node_hints = minHash.getClosest(key);
        Node node = node_hints.iterator().next();
        node = findStartPoint(node,query);
        ArrayList<DataDistance<T>> results = new ArrayList<>();
        super.rangeSearch(node, query, r, results);
        return results;
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
        return super.add(findStartPoint(node,data),data);
        // return insert_zoom(node,data);
    }

    /**
     * Finds the point at which to start searching/inserting etc.
     * This method refines a guess and finds a safe point at which to examine the MTree.
     * @param candidate - a candidate point in the MTree
     * @param data The data that we are inserting, querying etc.
     * @return the least node that can cover the given query.
     */
    private Node findStartPoint( Node candidate, T data ) {

        // There are THREE possibilities:
        //  1. We are at the root - insert at the root.
        //  2. We are outside of the circle - need to move back up the tree until we are inside the first found.
        //  3. We are inside the circle - need to move back up tree to highest-closest enclosing node

        if( candidate == root ) {   // Case 1 easy.
            return add( candidate,data );
        }

        float distance_to_target = distance_wrapper.distance(candidate.data, data);

        // Case 2 we are outside the covering radius, so move up the tree till we are inside.
        if (distance_to_target > candidate.radius) {
            while (candidate.parent != null && distance_to_target > candidate.radius) {
                // same code as while loop above but different condition.
                candidate = candidate.parent; // move back up the tree.
                distance_to_target = distance_wrapper.distance(candidate.data, data);
            }
            return candidate;
        }

        // Case 3 we are inside where we need to be
        // By definition the parent also satisfies this condition (nesting of radii).
        // Need to see if parent is closer.

        while (is_parent_closer(candidate, distance_to_target, data)) {
            // this look moves us up the tree and stops at root or largest covering radius.
            candidate = candidate.parent; // move back up the tree.
            distance_to_target = distance_wrapper.distance(candidate.data, data);
        }
        return candidate;
    }

    /**
     * @param node - a node to examine to see if the parent if closer to a query
     * @param distance_to_node - the distance from the node to the query
     * @param query - the query we are examining for parental closeness
     * @return true if the parent is closer to the query than the node.
     */
    private boolean is_parent_closer(Node node, float distance_to_node, T query  ) {
        Node parent = node.parent;
        if( parent == null ) {
            return false;
        }
        float parent_distance = distance_wrapper.distance(parent.data, query); // point-point distance
        return parent_distance < distance_to_node;
        // if (distance_to_node - node.radius < closest_thus_far.distance)
    }

}
