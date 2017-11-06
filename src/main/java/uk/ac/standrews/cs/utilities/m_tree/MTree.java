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

import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;
import uk.ac.standrews.cs.utilities.archive.Diagnostic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by al@st-andrews.ac.uk on 13/01/2017.
 * Code to implement an M-Tree.
 * Code based on ACM SAC Tutorial, March 2007 by Zezula, Amato, Dohnal - Similarity Search: The Metric Space Approach pp 129-
 * URL for tutorial: http://www.nmis.isti.cnr.it/amato/similarity-search-book/SAC-07-tutorial.pdf
 */
public class MTree<T> {

    private static final int DEFAULT_MAX_LEVEL_SIZE = 20;
    private final int max_level_size; // size of a level
    private static final float EPSILON = 0.00001f; // A small float to avoid checking with zero.
    final Distance<T> distance_wrapper;

    Node root = null;

    int num_entries = 0;

    public MTree(Distance<T> d, int max_level_size) {

        distance_wrapper = d;
        this.max_level_size = max_level_size;
    }

    public MTree(Distance<T> d) {

        this(d, DEFAULT_MAX_LEVEL_SIZE);

    }

    /**
     * @return the number of nodes in the tree
     */
    public int size() {

        //return num_entries;
        return calculateSize(root);
    }

    /**
     * Find the closest N nodes to @param query.
     *
     * @param query - some data for which to find the nearest N neighbours
     * @param n     the number of neighbours to return
     * @return n neighbours (or as many as possible)
     */
    public List<DataDistance<T>> nearestN(T query, int n) {

        ClosestSet results = new ClosestSet(n);
        nearestN(root, n, query, results);
        return results.values();
    }

    private static int count_leaf_comparisons = 0;
    private static int count_intermediate_comparisons = 0;
    private static int count_depth = 0;
    private static int deepest = 0;

    /**
     * Find the nodes withing @param r of @param T.
     *
     * @param query - some data for which to find the neighbours within distance r
     * @param r     the distance from query over which to search
     * @return all those nodes within r of @param T.
     */
    public List<DataDistance<T>> rangeSearch(T query, float r) {

        count_leaf_comparisons = 0;
        count_intermediate_comparisons = 0;
        count_depth = 0;
        deepest = 0;

        ArrayList<DataDistance<T>> results = new ArrayList<>();
        rangeSearch(root, query, r, results);
        System.err.println( "leaf comparisons = " + count_leaf_comparisons);
        System.err.println( "intermediate comparisons = " + count_intermediate_comparisons);
        System.err.println( "max depth = " + deepest );
        return results;
    }

    /**
     * return the nearest neighbour to the query
     *
     * @param query - some data for which to find the nearest neighbour
     * @return the nearest neighbour of T.
     */
    public DataDistance<T> nearestNeighbour(T query) {
        if( root == null ) {
            return null;
        } else {
            return nearestNeighbour(root, new DataDistance<>(root.data, distance_wrapper.distance(root.data, query)), query);
        }
    }

    /**
     * @param data - some data for which to search
     * @return true if the tree contains the data
     */
    @SuppressWarnings("WeakerAccess")
    public boolean contains(T data) {
        return root != null && contains(root, data);
    }

    /**
     * Add some data to the MTree
     *
     * @param data the data to be added to the tree
     */
    public void add(T data) {

        num_entries++;
        if (root == null) {
            root = new Node(data, null, 0.0f);
        } else {
            add(root, data);
        }
    }

    //------------------------- Utility methods

    /**
     * Method to extract values from DataDistance lists
     *
     * @param data_distances a list of distances from which to extract values
     * @return the set of values from the list
     */
    public List<T> mapValues(List<DataDistance<T>> data_distances) {

        List<T> result = new ArrayList<>();
        for (DataDistance<T> dd : data_distances) {
            result.add(dd.value);
        }
        return result;
    }

    /**
     * Method to extract distances from DataDistance lists
     *
     * @param data_distances a list of distances from which to extract values
     * @return the set of distances from the list
     */
    @SuppressWarnings("unused")
    public List<Float> mapDistances(List<DataDistance<T>> data_distances) {

        List<Float> result = new ArrayList<>();
        for (DataDistance<T> dd : data_distances) {
            result.add(dd.distance);
        }
        return result;
    }

    //------------------------- Private methods

    private void show_structure(TreeStructure ts, Node node, int depth) {
        if (node == null) { // safety net
            return;
        }
        if (depth > ts.max_depth) {
            ts.max_depth = depth;
        }
        ts.total_tree_size += ObjectSizeCalculator.getObjectSize(node); // the size of the node.
        ts.total_tree_size += ObjectSizeCalculator.getObjectSize(node.children); // the size of the children array.
        ts.total_tree_size += ObjectSizeCalculator.getObjectSize(node.data); // the size of the referenced data object.
        // only other pointer field is parent which has already been included.

        if (node.isLeaf()) {
            ts.number_leaves++;

        } else {
            ts.number_internals++;
            ts.recordChildren(node.children.size());
            for (Node child : node.children) {
                show_structure(ts, child, depth + 1);
            }
        }
    }

    /**
     * Find the number of nodes in a (sub) tree
     *
     * @param node - the (sub) tree whose requested_result_set_size is required
     */
    private int calculateSize(Node node) {

        if (node == null) {
            return 0;

        } else if (node.isLeaf()) {
            return 1;

        } else {
            int size = 0;
            for (Node child : node.children) {
                size += calculateSize(child);
            }
            return size;
        }
    }

    /**
     * Debug method primarily but may be useful
     * Displays the tree.
     */
    @SuppressWarnings("unused")
    public void showTree() {
        showTree(root, 0);
        System.out.println("----------------------");
    }

    /**
     * Displays the (subtree) tree rooted at @param node
     *
     * @param node   the node for which the subtree is to displayed
     * @param indent the amount of indent to be used in displaying the (sub) tree
     */
    private void showTree(Node node, int indent) {

        if (node == null) {
            printIndent(indent);
            System.out.println("null");

        } else if (node.children.size() == 0) {
            printIndent(indent);
            System.out.println(node.data + " R: " + node.radius + " d to parent: " + node.distance_to_parent + " isLeaf: " + node.isLeaf());

        } else {
            printIndent(indent);
            System.out.println(node.data + " R: " + node.radius + " d to parent: " + node.distance_to_parent + " isLeaf: " + node.isLeaf() + " children: ");
            for (Node child : node.children) {
                showTree(child, indent + 1);
            }
        }
    }

    /**
     * Prints out indent in accordance with the param
     *
     * @param indent the amount of indent to be displayed
     */
    private void printIndent(int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print("\t");
        }
    }

    public void check_invariants() {
        check_invariants(root, 0);
        System.out.println("------");

    }

    private void check_invariants(Node node, int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print("\t");
        }
        System.out.println("checking node: " + node.data + "radius: " + node.radius + " is leaf: " + node.isLeaf() + " count children: " + node.children.size());
        if (node.isLeaf()) {
            assert node.children.size() == 0;
        }
        if (node.children.size() == 0) {
            assert node.isLeaf();
        }
        if (!node.isLeaf()) {
            assert node.data == node.children.get(0).data;
            assert node.children.get(0).radius == 0.0f;
        }
        for (Node child : node.children) {
            for (int i = 0; i < indent + 1; i++) {
                System.out.print("\t");
            }
            System.out.println("child: " + child.data + "radius: " + child.radius + " distance to parent: " + child.distance_to_parent + " is leaf: " + child.isLeaf() + " count children: " + child.children.size() + " parent: " + node.data);
            assert child.distance_to_parent <= node.radius;
            float d = distance_wrapper.distance(child.data, node.data);
            assert d == child.distance_to_parent;
            assert child.parent == node;
            assert child.radius <= node.radius;
            assert d <= node.radius;
            if (!child.isLeaf()) {
                check_invariants(child, indent + 1);
            }
        }
    }

    public TreeStructure showStructure() {
        TreeStructure ts = new TreeStructure();
        ts.max_level_size = max_level_size;
        show_structure(ts, root, 0);
        return ts;
    }

    /**
     * Find the nodes withing @param RQ of @param query.
     *
     * @param N  - the node we are searching
     * @param query  the query data
     * @param RQ the search radius
     */

    void rangeSearch(Node N, T query, float RQ, ArrayList<DataDistance<T>> results) {

        if (N.isLeaf()) {

            count_leaf_comparisons++;

            float distanceNodeToQ = distance_wrapper.distance(N.data, query);
            if (distanceNodeToQ <= RQ) {
                results.add(new DataDistance<>(N.data, distanceNodeToQ));
            }

        } else {
            float distanceNodeToQ = distance_wrapper.distance(N.data, query);
            count_intermediate_comparisons++;

            if ( distanceNodeToQ - RQ - N.radius <= EPSILON ) {  // only look at the children if the query is inside the ball.

                for (Node child : N.children) {

                        count_depth++;
                        if( count_depth > deepest) {
                            deepest = count_depth;
                        }
                        rangeSearch(child, query, RQ, results);     // distance between them is less than the sum of the radii.
                        count_depth--;
                }

            }
        }
    }

    /**
     * @param query - some data for which to search
     * @return true if the tree contains the data
     */
    private boolean contains(Node node, T query) {

        if (node.data.equals(query)) {
            return true;
        }

        if (node.isLeaf()) { // is not equal and we are at a leaf;
            return false;
        }

        // node has children and is not equal itself.
        // see if the data is within range of node
        if (distance_wrapper.distance(node.data, query) - node.radius > EPSILON) { // no optimisation possible here
            // search data is outside of range
            return false;

        } else { // the data may be inside this ball
            // need to check children
            for (Node child : node.children) {
                if (!child.data.equals(node.data)) { // first child holds the same data as the node
                    if (contains(child, query)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * return the nearest neighbour to the query in the subtree rooted at @param node
     *
     * @param node             the root of the subtree in which to search
     * @param closest_thus_far - the closest neighbour to query that has been found in the recursive search
     * @param query            - some data for which to find the nearest neighbour
     * @return the nearest neighbour of T.
     */
    DataDistance<T> nearestNeighbour(Node node, DataDistance<T> closest_thus_far, T query) {

        if (node.isLeaf()) {

            final float distance_from_this_leaf_query = distance_wrapper.distance(node.data, query);
            if (distance_from_this_leaf_query < closest_thus_far.distance) { // this node is closer
                return new DataDistance<>(node.data, distance_from_this_leaf_query);
            } else {
                return closest_thus_far;
            }
        } else { // an intermediate node - we don't need to check the intermediate since first child holds the data.

            return search_children(node, closest_thus_far, query);
        }
    }

    /**
     * Search the children of a node for results
     *
     * @param node             - the node in which to search
     * @param closest_thus_far - the closest node to node that we have found so far
     * @param query            - the quest being performed
     * @return the closest node and its distance to query
     */
    DataDistance<T> search_children(Node node, DataDistance<T> closest_thus_far, T query) {

        for (Node child : node.children) {

            if ( child.isLeaf() || distance_wrapper.distance(child.data, query) - child.radius <= EPSILON) { // query may be inside this child.

                DataDistance<T> nn = nearestNeighbour(child, closest_thus_far, query);  // do recursive search
                if (nn.distance < closest_thus_far.distance) {
                    closest_thus_far = nn;
                }
            }
        }

        return closest_thus_far;
    }

    /**
     * Find the closest N nodes to @param query.
     *
     * @param node    the root of the tree in which the search is being carried out
     * @param query   - some data for which to find the closest N neighbours
     * @param results the nearest nodes found thus far
     */
    void nearestN(Node node, int n, T query, ClosestSet results) {

        if (node.isLeaf()) { // we are at a leaf - see if ths is closer than other nodes in results

            float node_distance = distance_wrapper.distance(node.data, query);
            if (results.size() < n) { // fill up the list without checking until at capacity
                results.addInDistanceOrder(node.data, node_distance);

            } else {
                if (node_distance < results.furthestDistance()) { // this node is closer
                    results.addInDistanceOrder(node.data, node_distance);
                }
            }
            return; // leaves have no children so give up here.
        }

        // see if we need to check out the children;
        float node_distance = distance_wrapper.distance(node.data, query);

        if (results.size() == 0 || node_distance - node.radius < results.furthestDistance()) {
            // may be nodes in tree closer than those in results
            for (Node child : node.children) {

                nearestN(child, n, query, results); // have a look at the children
            }
        }
    }

    /**
     * Insert some data into a node of the Tree.
     *
     * @param subTree            - the sub-tree into which the data is inserted.
     * @param data               - the data to add into the children
     * @param distance_to_parent - the distance to the parent from subtree (pre calculated in caller)
     * @return the node inserted into the tree.
     */
    private Node insertIntoNode(Node subTree, T data, float distance_to_parent) {

        if ((subTree.isFull())) {
            Node newLeaf = new Node(data, null, 0.0f);
            split(subTree, newLeaf);
            return newLeaf;

        } else {
            if (subTree.isLeaf()) {
                Node copyOfParent = new Node(subTree.data, subTree, 0.0f);
            }
            Node newLeaf = new Node(data, subTree, distance_to_parent);
            subTree.addChild(newLeaf, distance_to_parent); // children is not yet full - put the data into the children
            return newLeaf;
        }
    }

    /**
     * Find the most appropriate node in which data should be inserted.
     * Choose child such that: No enlargement of radius is needed,
     * In case of ties, choose the closest one to the new node.
     *
     * @param node - the node into which we are inserting the data
     * @param data the new data.
     * @return the new node that is inserted into the tree
     */
    Node add(Node node, T data) {

        Node selected_pivot = node; // the best pivot into which to insert - 3 choices: this node, enclosing child, or nearest child.
        float selected_pivot_distance = distance_wrapper.distance(selected_pivot.data, data); // the distance between data and the selected pivot.

        // Try and see if there is an enclosing child for the data
        for (Node child : node.children) {
            if (child != node.children.get(0)) { // do not do this on the first child which is the same as the parent
                float distance_from_data_to_child = distance_wrapper.distance(child.data, data);

                if (distance_from_data_to_child < child.radius && distance_from_data_to_child < selected_pivot_distance) { // we are inside the radius of the child and closer

                    selected_pivot = child;
                    selected_pivot_distance = distance_from_data_to_child;
                }
            }
        }

        // at this point we are inserting into the node or the child.
        // If the latter we don't do anything more with searching because an enclosing child is the best choice.
        // if the former the selected pivot will be equal to the node, so use this as the check.

        if (selected_pivot == node) { // we didn't find an enclosing child.

            // See if any of the children are closer than the node to data
            for (Node child : node.children) {
                if (child != node.children.get(0)) { // do not do this on the first child which is the same as the parent
                    float distance_from_data_to_child = distance_wrapper.distance(child.data, data);

                    if (distance_from_data_to_child < selected_pivot_distance) { // this pivot is closer than the node and any other we have found
                        selected_pivot_distance = distance_from_data_to_child;
                        selected_pivot = child;
                    }
                }
            }
            // avoid creating ball overlaps in children ....
            if( selected_pivot != node) {    // we are planning to put data in a child.

                if( selected_pivot_distance > selected_pivot.radius ) {  // the chosen child would get bigger

                    // check to see if we have created an overlap in doing this.
                    // and if we have avoid it.

                    for (Node child : node.children) {  // check all the children for overlap
                        if( child != selected_pivot ) {
                            float d_from_selected_pivot_to_child = distance_wrapper.distance(child.data, selected_pivot.data);

                            if( d_from_selected_pivot_to_child - child.radius - selected_pivot.radius <= EPSILON ) {
                                // using this selected_pivot would create overlap.
                                // so don't use and use node instead.
                                selected_pivot = node;
                                selected_pivot_distance = distance_wrapper.distance(selected_pivot.data, data);
                                break;
                            }
                        }
                    }
                }
            }
        }


        Node new_node;

        // at the end of the above tests we have found the best pivot into which to insert
        // may be the node, an enclosing child or a close child.

        if (selected_pivot == node) {                                                  // we didn't find anywhere better amongst the children - insert into this node
            new_node = insertIntoNode(selected_pivot, data, selected_pivot_distance);
            // insertIntoNode sorts out the radius of selected_pivot
        } else {     // recursively insert into one of the children

            new_node = add(selected_pivot, data);  // may have been added to to an enclosing child or a close one, in the latter case the ball of the child will have expanded.

            float distance_pivot_to_node = distance_wrapper.distance(selected_pivot.data, node.data);

            if ( distance_pivot_to_node + selected_pivot.radius > node.radius ) {                 // check if we need to make this node's radius bigger
                node.radius = distance_pivot_to_node + selected_pivot.radius;
            }

        }

        return new_node;
    }

    /**
     * @param children - the children to inspect
     * @return the largest radius of the children
     */
    private float maxR(List<Node> children) {
        float result = 0.0f;
        for (Node child : children) {
            if (child.radius > result) {
                result = child.radius;
            }
        }
        return result;

    }

    /**
     * Helper method for splitting levels (children) of node in the tree
     *
     * @param sub_root the node which is being split
     * @param new_node a new node being added
     */
    private void split(Node sub_root, Node new_node) {

        // Insertion into a leaf may cause the node to overflow.
        // The overflow of a node sub_root is resolved by allocating a new node new_pivot at the same level and
        //  by redistributing the m + 1 entries between the node subject to overflow and the new pivot
        // This node split requires two new pivots to be selected and the
        // corresponding covering radii adjusted to reflect the current membership of the
        // two new nodes. Naturally, the overflow may propagate towards the root node and,
        // if the root splits, a new root is created and the tree grows up one level.

        sub_root.addChild(new_node, distance_wrapper.distance(sub_root.data, new_node.data));  // add new_node into children - now over full

        // but we are about to perform a split - makes computation easier.
        // Select a new pivot from the children (with data added).

        // select a second pivot on which to partition S to S1 and S2 according to N and new_pivot:
        Node new_pivot = selectPivot(sub_root, sub_root.children);

        if (new_pivot == null) {
            // We couldn't find a new pivot.
            // Therefore just tolerate overflow.
            return;
        }

        // Node new_pivot = new Node(pivot_node.data, null, 0.0f);

        // Partition children of sub_root to sub_root_children and s2 according to sub_root and new_pivot
        PairOfNodeLists partition = partitionChildrenIntoPivots(sub_root, new_pivot);

        sub_root.children = new ArrayList<>();  // get rid of existing children of the node before reallocation
        sub_root.radius = 0.0f;                 // and get rid of old radii - insert below will fix up radii correctly
        new_pivot.radius = 0.0f;                // and get rid of old radii - insert below will fix up radii correctly

        // allocate the children from sub_root_children and new_pivots_children to sub_root and new pivot respectively
        for (Node partion1_child : partition.partition1) {
            sub_root.addChild(partion1_child, distance_wrapper.distance(sub_root.data, partion1_child.data));                // radii are adjusted as nodes are added
        }

        for (Node partition2_child : partition.partition2) {
            new_pivot.addChild(partition2_child, distance_wrapper.distance(new_pivot.data, partition2_child.data));        // radii are adjusted as nodes are added
        }

        // Now have the new_pivot unallocated so we try and put it in the parent of sub_root

        if (sub_root == root) { // it was the root had filled up - cannot go any higher so make the new_pivot the new root.

            // we need to make new_pivot the new root

            root = new_pivot;
            new_pivot.parent = null;  // we reused a node whose parent was subroot.

            // make the tree one level deeper by adding old sub_root to the new root.

            new_pivot.addChild(sub_root, distance_wrapper.distance(new_pivot.data, sub_root.data));

        } else {
            // it is a regular node - not the root - it has a a parent into which we can try to insert new_node

            // let Np and pp be the parent node and parent data of sub_root
            //  Replace entry pp with p1
            //  If Np is full, then Split(Np,new_pivot) else store new_pivot in node Np

            Node sub_roots_parent = sub_root.parent;

            if ((sub_roots_parent.isFull())) {
                split(sub_roots_parent, new_pivot);
            } else {
                sub_roots_parent.addChild(new_pivot, distance_wrapper.distance(sub_roots_parent.data, new_pivot.data));

            }

        }
    }

    /**
     * Takes two pivots and a list of Nodes and re-partitions into two Lists of nodes
     * Partitioning based on proximity - those nodes closest to sub_root go into the first partition
     * those nodes closest to new_pivot go into the second.
     *
     * @param sub_root  the first data
     * @param new_pivot the second data
     * @return two partitions of nodes
     */
    private PairOfNodeLists partitionChildrenIntoPivots(Node sub_root, Node new_pivot) {

        List<Node> partition1 = new ArrayList<>();
        List<Node> partition2 = new ArrayList<>();

        int count = 0;

        for (Node child : sub_root.children) {

            if (count != 0) { // do not copy the special first child which is the holds the same data as the parent but has R zero.
                // need to be tricky with above since first child is copy of sub_root not identical
                // and other nodes may have zero distance - I think this is the best way!

                if (child != new_pivot) { // the pivot becomes a new root - don't copy - used == since identical
                    float r1 = distance_wrapper.distance(sub_root.data, child.data);
                    float r2 = distance_wrapper.distance(new_pivot.data, child.data);

                    if (r1 < r2) {
                        partition1.add(child);
                    } else {
                        partition2.add(child);
                    }
                }
            }
            count++;
        }
        return new PairOfNodeLists(partition1, partition2);
    }

    /**
     * Select a new pivot
     * Choose node from s that has the smallest radius and is not the existing @param pivot
     *
     * @param pivot           - the existing pivot
     * @param pivots_children - a list of nodes from which to choose a data
     * @return a new pivot with the smallest radius
     */
    private Node selectPivot(Node pivot, List<Node> pivots_children) {

        Node smallest_not_pivot = null;

        for (Node child : pivots_children) {
            if (child != pivot.children.get(0)) {  // don't select the pivot again - or any note at a distance of zero from pivot
                if (smallest_not_pivot == null) {
                    // this is the first candidate (!= parent) we assign it to be the smallest
                    smallest_not_pivot = child;
                } else if (child.radius < smallest_not_pivot.radius) {
                    // this is smaller than the smallest we know so far
                    smallest_not_pivot = child;
                }
                if (smallest_not_pivot.radius == 0.0f) {  // can't be null - set in arms above.
                    break;                                 // give up if R is zero - can't do better than that!
                }
            }

        }

        if (smallest_not_pivot == null) {
            Diagnostic.trace("Cannot find smallest pivot, smallest_not_pivot is null. Was called with: " + pivot);
        }

        if (smallest_not_pivot != null) {
            smallest_not_pivot.parent = null; // we are about to re-insert this into the tree at a new position, so unlink from parent
        }
        return smallest_not_pivot;
    }

    private class PairOfNodeLists {

        List<Node> partition1;
        List<Node> partition2;

        PairOfNodeLists(List<Node> partition1, List<Node> partition2) {
            this.partition1 = partition1;
            this.partition2 = partition2;
        }
    }

    //----------------------- Helper classes

    /**
     * This is the class used to build the M-tree.
     */
    protected class Node {

        public T data;
        float radius;
        float distance_to_parent;
        Node parent;
        List<Node> children;

        Node(T oN, Node parent, float distance_to_parent) {
            data = oN;
            radius = 0.0f;
            this.distance_to_parent = distance_to_parent;
            children = new ArrayList<>();
            this.parent = parent;
        }

        /**
         * Adds a child to the current node
         * Pre condition this is only called where a split has already occurred and parent is not full
         *
         * @param newNode            the node to add to the Node
         * @param distance_to_parent - the distance from newNode to the parent
         */
        void addChild(Node newNode, float distance_to_parent) {

            if (children.size() == 0) {
                // We are turning a leaf into an intermediate node
                // So we add add this node's data as the first child
                children.add(new Node(data, this, 0f));
            }

            children.add(newNode);
            newNode.distance_to_parent = distance_to_parent;
            newNode.parent = this;
            float new_radius = distance_to_parent + newNode.radius;
            if (new_radius > radius) {
                radius = new_radius;
            }
        }

        boolean isLeaf() {
            return children.size() == 0;
        }

        boolean isFull() {
            return children.size() >= max_level_size;
        }

        public String toString() {
            return "data= " + data + " r= " + radius + " dp= " + distance_to_parent;
        }
    }

    class ClosestSet {

        ArrayList<DataDistance<T>> closest;
        int requested_result_set_size;

        ClosestSet(int n) {
            closest = new ArrayList<>();
            requested_result_set_size = n;
        }

        public int size() {
            return closest.size();
        }

        private void addInDistanceOrder(T data, float distance) {

            int index;
            if (closest.size() == 0) {
                closest.add(0, new DataDistance<>(data, distance));
                return;
            }
            for (index = 0; index < closest.size(); index++) {
                DataDistance<T> next = closest.get(index);
                if (distance <= next.distance) { // found right point to insert
                    closest.add(index, new DataDistance<>(data, distance));
                    checkEvict();
                    return;
                }
            }
            closest.add(index, new DataDistance<>(data, distance)); // add at the end
            checkEvict();
            // if we get here then the new element is further than rest so should not be added.
        }

        float furthestDistance() {

            DataDistance furthest_element = closest.get(closest.size() - 1);
            return furthest_element.distance;
        }

        private void checkEvict() {

            if (closest.size() > requested_result_set_size) {
                closest.remove(closest.size() - 1); // remove the last
            }
        }

        public String toString() {

            if (closest.size() == 0) {
                return "[]";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (DataDistance<T> dd : closest) {
                sb.append("\tdata: ").append(dd.value).append("distance: ").append(dd.distance).append("\n");
            }
            sb.append("\t]");
            return sb.toString();
        }

        List<DataDistance<T>> values() {

            return closest;
        }
    }
}
