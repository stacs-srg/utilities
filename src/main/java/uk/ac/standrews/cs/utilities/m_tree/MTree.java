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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by al@st-andrews.ac.uk on 13/01/2017.
 * Code to implement an M-Tree.
 * Code based on ACM SAC Tutorial, March 2007 by Zezula, Amato, Dohnal - Similarity Search: The Metric Space Approach pp 129-
 * URL for tutorial: http://www.nmis.isti.cnr.it/amato/similarity-search-book/SAC-07-tutorial.pdf
 */
public class MTree<T> {

    private static final int MAX_LEVEL_SIZE = 20;
    final Distance<T> distance_wrapper;

    Node root = null;


    int num_entries = 0;

    public MTree(Distance<T> d) {

        distance_wrapper = d;
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

    /**
     * Find the nodes withing @param r of @param T.
     *
     * @param query - some data for which to find the neighbours within distance r
     * @param r     the distance from query over which to search
     * @return all those nodes within r of @param T.
     */
    public List<DataDistance<T>> rangeSearch(T query, float r) {

        ArrayList<DataDistance<T>> results = new ArrayList<>();
        rangeSearch(root, query, r, results);
        return results;
    }

    /**
     * return the nearest neighbour to the query
     *
     * @param query - some data for which to find the nearest neighbour
     * @return the nearest neighbour of T.
     */
    public DataDistance<T> nearestNeighbour(T query) {
        return nearestNeighbour(root, null, query);
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
            root = new Node(data, null);
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

    /**
     * Find the nodes withing @param RQ of @param T.
     *
     * @param N  - the node we are searching
     * @param Q  the query data
     * @param RQ the search radius
     *           <p>
     *           Algorithm RangeSearch from https://en.wikipedia.org/wiki/M-tree
     *           Input: Node N of M-Tree MT,  Q: query object, R(Q): search radius
     *           Output: all the DB objects such that
     *           d(O,j,Q) ≤ RQ(Q)
     *           <p>
     *           <pre>
     *           {
     *             let Op be the parent object of node N;
     *
     *             if N is not a leaf then {
     *               for each entry(Or) in N do {
     *                     if |d(Op,Q) − d(Or,Op)| ≤ R(Q)+R(Or) then {
     *                       Compute d(Or,Q);
     *           			if d(Or,Q) ≤ RQ(Q)+R(Or)} then
     *                         RangeSearch(*ptr(T(Or)),Q,R(Q));
     *                     }
     *               }
     *             }
     *             else { // it is leaf
     *               for each entry(Oj) in N do {
     *                     if |d(Op,Q) - d(Oj,Op)| ≤ R(Q) then {
     *                       Compute d(Oj,Q);
     *                       if d(Oj,Q) ≤ R(Q) then
     *                         add oid(Oj) to the result;
     *                     }
     *               }
     *             }
     *           }
     *           </pre>
     */
    void rangeSearch(Node N, T Q, float RQ, ArrayList<DataDistance<T>> results) {

        Node parent = N.parent;

        if (N.isLeaf()) {

            float distanceNodeToQ = distance_wrapper.distance(N.data, Q);
            if (distanceNodeToQ <= RQ) {
                results.add(new DataDistance<>(N.data, distanceNodeToQ));
            }

        } else {
            for (Node child : N.children) {

                float distanceQtoParent = parent == null ? Float.MAX_VALUE : distance_wrapper.distance(parent.data, Q);
                float distanceChildToParent = parent == null ? Float.MAX_VALUE : distance_wrapper.distance(child.data, parent.data);

                if (parent == null || Math.abs(distanceQtoParent - distanceChildToParent) <= RQ + child.radius) {

                    float distanceChildToQ = distance_wrapper.distance(child.data, Q);
                    if (distanceChildToQ <= RQ + child.radius) {
                        rangeSearch(child, Q, RQ, results);
                    }
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
        if (Math.abs(distance_wrapper.distance(node.data, query) - node.radius + Float.MIN_VALUE) < Float.MIN_VALUE) {
            // search data is outside of range
            return false;

        } else { // the data may be inside this ball
            // need to check children
            for (Node child : node.children) {
                if (contains(child, query)) {
                    return true;
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

        if (node.data.equals(query)) {
            return new DataDistance<>(node.data, 0.0F);
        }

        final float distance_to_node = distance_wrapper.distance(node.data, query);
        if (closest_thus_far == null) {
            closest_thus_far = new DataDistance<>(node.data, distance_to_node);
        }

        if (node.isLeaf()) { // is not equal and we are at a leaf
            if (distance_to_node < closest_thus_far.distance) { // this node is closer
                return new DataDistance<>(node.data, distance_to_node);
            }
            return closest_thus_far; // we are not any closer.
        }
        return search_children( node, closest_thus_far, query );
    }

    /**
     * Search the children of a node for results
     * @param node
     * @param closest_thus_far
     * @param query
     * @return
     */
    DataDistance<T> search_children( Node node, DataDistance<T> closest_thus_far,T query ) {
        float distance_to_node = closest_thus_far.distance;
        if (distance_to_node - node.radius < closest_thus_far.distance) {
            // may be interesting results in the children
            for (Node child : node.children) {
                DataDistance<T> nn = nearestNeighbour(child, closest_thus_far, query);
                if (distance_wrapper.distance(nn.value, query) < closest_thus_far.distance) {
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
     * Insert some data into a leaf of the Tree.
     *
     * @param node - the parent of the current node.
     * @param data - the data to add into the children
     */
    private Node leafInsert(Node node, T data) {

        if ((node.isFull())) {
            return split(node, new Node(data, null));

        } else {
            if (node.isEmpty()) {
                Node newnode = new Node(node.data, node);
                node.addChild(newnode); // we making a leaf into an intermediate node - add Node to its own children
            }
            Node newnode = new Node(data, node);
            node.addChild(newnode); // children is not yet full - put the data into the children
            return newnode;
        }
    }

    /**
     * Find the most appropriate node in which data should be inserted.
     * Choose child such that: No enlargement of radius is needed,
     * In case of ties, choose the closest one to the new node.
     *
     * @param node - the node into which we are inserting the data
     * @param data the new data.
     */
     Node add(Node node, T data) {

        Node enclosing_pivot = null;
        Node closest_pivot = null;
        float smallest_distance = -1.0F; // illegal distance

        // find the most appropriate child.
        for (Node child : node.children) {
            float new_distance = distance_wrapper.distance(child.data, data);

            if (new_distance < child.radius) { // we are inside the radius of the current existing pivot - new node falls in within this ball

                if (new_distance < smallest_distance || smallest_distance == -1.0F) { // we are closer to this pivot than any previous pivots

                    enclosing_pivot = child;
                    smallest_distance = new_distance;
                }

            } else if (enclosing_pivot == null) { // not found any pivot within whose radius the new data falls

                if (closest_pivot == null) {
                    // no candidates yet so make this one the closest
                    smallest_distance = new_distance;
                    closest_pivot = child;
                } else if (new_distance < smallest_distance) { // this pivot is closer.
                    smallest_distance = new_distance;
                    closest_pivot = child;
                }
            }
        }

        if (enclosing_pivot == null) { // didn't find an enclosing pivot - put it in the closest.

            if (closest_pivot == null || distance_wrapper.distance(node.data, data) <= smallest_distance) { // this node is closer to the new data
                return leafInsert(node, data);

            } else { // one of the children is closer
                float old_radius = closest_pivot.radius;
                Node newnode = add(closest_pivot, data);
                // now check to see if the radius has changed
                float new_radius = closest_pivot.radius;

                if (new_radius > old_radius) { // the radius has grown
                    float new_distance_to_pivot = distance_wrapper.distance(node.data, closest_pivot.data);

                    // now check if the parent radius needs to be adjusted too.
                    if (new_distance_to_pivot + new_radius > node.radius) {
                        node.radius = new_distance_to_pivot + new_radius;
                    }
                }
                return newnode;
            }
        } else { // we found an enclosing pivot - no need to make the radius bigger
            return add(enclosing_pivot, data);
        }
    }

    /**
     * Helper method for splitting levels (children) of node in the tree
     *
     * @param N  the node which is being split
     * @param oN a new node being added
     */
    private Node split(Node N, Node oN) {

        // Insertion into a leaf may cause the node to overflow.
        // The overflow of a node N is resolved by allocating a new node new_pivot at the same level and
        //  by redistributing the m + 1 entries between the node subject to overflow and the new pivot
        // This node split requires two new pivots to be selected and the
        // corresponding covering radii adjusted to reflect the current membership of the
        // two new nodes. Naturally, the overflow may propagate towards the root node and,
        // if the root splits, a new root is created and the tree grows up one level.

        N.addChild(oN);  // add oN into children - now over full

        // but we are about to perform a split - makes computation easier.
        // Select a new pivot from the children (with data added).

        // select a second pivot on which to partition S to S1 and S2 according to N and new_pivot:
        Node pivot_node = selectPivot(N, N.children);

        Node new_pivot = new Node(pivot_node.data, null);

        // Partition children of N to s1 and s2 according to N and new_pivot
        PairOfNodeLists partition = partitionChildrenIntoPivots(N, new_pivot, N.children);

        List<Node> s1 = partition.nl1; // keep names same as original pseudo code.
        List<Node> s2 = partition.nl2; // keep names same as original pseudo code.

        N.children = new ArrayList<>(); // get rid of existing children of the node before reallocation

        // allocate the children from s1 and s2 to N and new pivot
        for (Node n : s1) {
            N.addChild(n);                // radii are adjusted as nodes are added
        }
        for (Node n : s2) {
            new_pivot.addChild(n);        // radii are adjusted as nodes are added
        }

        // Now have the new_pivot unallocated so we try and put it in the parent of N

        if (N == root) { // it was the root had filled up

            // we need to make new_pivot the new root

            root = new_pivot;

            // make the tree one level deeper.

            root.addChild(N);

        } else {
            // it is a regular node - not the root - it has a a parent into which we can try to insert new_node

            // let Np and pp be the parent node and parent data of N
            //  Replace entry pp with p1
            //  If Np is full, then Split(Np,new_pivot) else store new_pivot in node Np

            Node Np = N.parent;
            if (Np.isFull()) {
                split(Np, new_pivot);
            } else {
                Np.addChild(new_pivot);
            }
        }
        return new_pivot;
    }

    /**
     * Takes two pivots and a list of Nodes and re-partitions into two Lists of nodes
     * Partitioning based on proximity - those nodes closest to p1 go into the first partition
     * those nodes closest to p2 go into the second.
     *
     * @param p1 the first data
     * @param p2 the second data
     * @param s  a list of nodes to partition.
     * @return two partitions of nodes
     */
    private PairOfNodeLists partitionChildrenIntoPivots(Node p1, Node p2, List<Node> s) {

        List<Node> partition1 = new ArrayList<>();
        List<Node> partition2 = new ArrayList<>();

        for (Node child : s) {
            float r1 = distance_wrapper.distance(p1.data, child.data);
            float r2 = distance_wrapper.distance(p2.data, child.data);

            if (r1 < r2) {
                partition1.add(child);
            } else {
                partition2.add(child);
            }
        }
        return new PairOfNodeLists(partition1, partition2);
    }

    /**
     * Select a new pivot
     * Choose node from s that has the smallest radius and is not the existing @param pivot
     *
     * @param pivot      - the existing pivot
     * @param candidates - a list of nodes from which to choose a data
     * @return a new pivot with the smallest radius
     */
    private Node selectPivot(Node pivot, List<Node> candidates) {

        Node smallest_not_pivot = null;

        for (Node child : candidates) {
            if ( ! child.data.equals( pivot.data ) ){ // not identical since we are looking at children - // TODO should be equals?
                if (smallest_not_pivot == null) {
                    smallest_not_pivot = child;
                } else if (child.radius < smallest_not_pivot.radius) {
                    smallest_not_pivot = child;
                }
            }
        }
        if (smallest_not_pivot != null) {
            smallest_not_pivot.parent = null; // we are about to re-insert this into the tree at a new position.
        }
        return smallest_not_pivot;
    }

    private class PairOfNodeLists {

        List<Node> nl1;
        List<Node> nl2;

        PairOfNodeLists(List<Node> nl1, List<Node> nl2) {
            this.nl1 = nl1;
            this.nl2 = nl2;
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

        Node(T oN, Node parent) {
            data = oN;
            radius = 0.0F;
            distance_to_parent = parent == null ? 0 : distance_wrapper.distance(oN, parent.data);
            children = new ArrayList<>();
            this.parent = parent;
        }

        /**
         * Adds a child to the current node
         * Pre condition this is only called where a split has already occurred and parent is not full
         *
         * @param newNode the node to add to the Node
         */
        void addChild(Node newNode) {

            children.add(newNode);
            float new_distance = distance_wrapper.distance(this.data, newNode.data);
            newNode.distance_to_parent = new_distance;
            newNode.parent = this;
            float new_radii = new_distance + newNode.radius;
            if (new_radii > radius) {
                radius = new_radii;
            }
        }

        boolean isLeaf() {
            return radius == 0.0F;
        }

        boolean isFull() {
            return children.size() >= MTree.MAX_LEVEL_SIZE;
        }

        boolean isEmpty() {
            return children.isEmpty();
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
