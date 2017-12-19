package uk.ac.standrews.cs.utilities.m_tree;

import uk.ac.standrews.cs.utilities.lsh.MinHash;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @param <T>
 *
 * One of a family of MashHelpers.
 * family includes - top down - find candidate helper nodes by:
 *           1. searching in a breadth first op down manner.
 *           2. bottom up - starting at leaves and recursing back up the tree until we find nodes
 *           3.random
 *           4. Using just leaves.
 *           5. TODO anything else?
 */
public class TopDownMashHelper<T> {

    private Mash<T> tree; // the tree which this helper is helping.
    private final KeyMaker<T> km;
    private final MinHash mh;
    private final int number_of_hints;
    private TreeSet<DataCount> hints;
    private final Distance<T> distance_wrapper;

    /**
     *
     * @param tree - the tree which this class is helping
     */
    public TopDownMashHelper(Mash<T> tree, KeyMaker<T> km, int number_of_hints, Distance<T> distance_wrapper) {
        this.tree = tree;
        this.number_of_hints = number_of_hints;
        hints = new TreeSet<DataCount>();
        this.mh = new MinHash<T>(2,50,5);
        this.km = km;
        this.distance_wrapper = distance_wrapper;
    }

    /**
     *
     */
    public void initialiseHints() {
        MTree.Node root = tree.root;
        if( root == null ) {
            return ;
        }

        intialiseHints(root);
//        for (DataCount dc : hints) {
//            System.out.println(dc.count + ":" + dc.node.data + " r= " + dc.node.radius );
//        }

        for( DataCount dc : hints ) {

            mh.put( km.makeKey(dc.node.data), dc.node );
        }
    }

    /**
     *
     * performs a top-down breadth first search of the tree and returns a list of candidate tree nodes.
     * @return a list of tree nodes to be used as hints for searching.
     * @param node - the root node of the (sub) MTree (not null)
     */
    private void intialiseHints( MTree<T>.Node node ) {

        if( node != null ) {

            int deps = node.getNumberOfDescendants();

            if (hints.size() < number_of_hints) {  // hints not full up yet.

                hints.add(new DataCount(node, deps));
                addChildrenToHints( node.children );

            } else {
                DataCount first = hints.first(); // the lowest count in the hints list.
                if( first.count < deps ) {  // lowest in list is lower than the current node's number of dependencies
                    // the newly encountered node has more children than the lowest - so get rid of lowest and add the current node
                    hints.remove( first );
                    hints.add(new DataCount(node, deps));
                    addChildrenToHints( node.children );
                }
                // no need to add children if first.count >= deps since children must have fewer dependants.
            }

        }
    }

    private void addChildrenToHints(List<MTree<T>.Node> children) {
        for (MTree<T>.Node child : children) {

                intialiseHints(child);
        }
    }

    public MTree<T>.Node getHint(T data) {

        Set<MTree<T>.Node> closest_set =  mh.getClosest(km.makeKey(data));

        System.out.println( "Matches for " + data + " :" );
        for( MTree<T>.Node node : closest_set ) {
            System.out.println( "\t" + node.data + " r= " + node.radius + " d= " + distance_wrapper.distance( data, node.data ));
        }

        return chooseBest( closest_set, data );

    }

    private MTree<T>.Node chooseBest(Set<MTree<T>.Node> closest_set, T data ) {

        // We have some help so try and jump to the right place.
        // TODO Problem: how to choose the best from the Set?
        // TODO for now just choose the first
        // TODO could have a look see and choose a good one here!

        System.out.println( "Candidate Size = " + closest_set.size() );
        if( closest_set.size() == 0 ) {
            System.out.println( "Zero matches found in lsh");
            return null;
        } else {
            System.out.println( closest_set.size() + " matches found in lsh");
            return refine( closest_set.iterator().next(),data ); // TODO Refine once working.
        }
    }

    private class DataCount implements Comparable<DataCount> {
        public MTree<T>.Node node;
        public int count;

        public DataCount( MTree<T>.Node node, int count ) {
            this.node = node;
            this.count = count;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DataCount dataCount = (DataCount) o;
            return count == dataCount.count &&
                    Objects.equals(node, dataCount.node);
        }

        @Override
        public int hashCode() {

            return Objects.hash(node, count);
        }

        @Override
        public int compareTo(DataCount other) {
            return this.count == other.count ? 0 : this.count < other.count ? -1 : 1; // want lowest count first.
        }
    }

    private MTree<T>.Node refine(MTree<T>.Node candidate, T query) {
        // There are THREE possibilities:
        //  1. We are at the root - search at the root.
        //  2. We are outside of the circle - need to move back up the tree until we are inside the first found.

        //  3. We are inside the circle - need to move back up tree to highest enclosing node

        System.out.println( "Pivot chosen = " + candidate.data + " r= " + candidate.radius + " d = " + distance_wrapper.distance(candidate.data, query) ) ;

        if( candidate == tree.root ) {   // Case 1 easy.
            return tree.root;
        }

        double distance_to_target = distance_wrapper.distance(candidate.data, query);

        // Case 2 we are outside the covering radius, so move up the tree till we are inside.
        if( distance_to_target > candidate.radius ) {
            while( candidate.parent != null && distance_to_target >= candidate.radius ) {
                // same code as while loop above but different condition.
                candidate = candidate.parent; // move back up the tree.
                distance_to_target = distance_wrapper.distance(candidate.data, query);
                System.out.println( "Zooming out" );
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
            System.out.println( "Zooming up" );
        }
        return candidate;
    }

    /**
     * @param node - a node to examine to see if the parent if closer to a query
     * @param distance_to_node - the distance from the node to the query
     * @param query - the query we are examining for parental closeness
     * @return true if the parent is closer to the query than the node.
     *
     * TODO this code is copied from Mash class - tidy up.
     */
    private boolean parent_closer(MTree<T>.Node node, double distance_to_node, T query  ) {
        MTree<T>.Node parent = node.parent;
        if( parent == null ) {
            return false;
        }
        double parent_distance = distance_wrapper.distance(parent.data, query);
        return parent_distance <= distance_to_node && parent_distance < parent.radius;
    }


}
