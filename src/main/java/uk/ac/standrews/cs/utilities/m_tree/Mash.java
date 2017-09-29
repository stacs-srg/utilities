package uk.ac.standrews.cs.utilities.m_tree;

import uk.ac.standrews.cs.utilities.lsh.MinHash;

import java.util.Set;

/**
 * Created by al on 28/09/2017.
 */
public class Mash<T> extends MTree {

    private final MinHash minHash;
    private final KeyMaker km;

    public Mash( Distance<T> distance, KeyMaker km ) {
        super(distance);
        minHash = new MinHash<Node>();
        this.km = km;
    }

    public void addX(T data) {
        String key = km.makeKey(data);
        Set<Node> nodes = minHash.getClosest(key);
        Node mTreeNode = addWithHint( key, nodes); // add the data to the MTree.
        minHash.put( key,mTreeNode ); // save the new node in the lsh structure.
    }


    public DataDistance<T> nearestNeighbourX(T query) {
        String key = km.makeKey(query);
        Set<Node> nodes = minHash.getClosest(key);
        return nnWithHint( query, nodes );

    }

    //----------------------------------- Private methods -----------------------------------

    /**
     * Add some data to the MTree
     * @param data the data to be added to the tree
     * @param node_hints - some nodes that are likely to be close to the right insertion point.
     */
    private Node addWithHint(String data, Set<Node> node_hints ) {

        num_entries++;
        if( node_hints.isEmpty() ) { // just add normally
            return add( root, data );
        }
        // We have some help so try and jump to the right place.
        // TODO Problem: how to choose the best from the Set?
        // TODO for now just choose the first
        // TODO could have a look see and choose a good one here!
        Node node = node_hints.iterator().next();
        return insert_zoom(node,data);
    }

    private Node insert_zoom(Node candidate, String data) {

        // There are two possibilities:
        //  1. We are inside the circle - normal add into this node.
        //  2. We are outside of the circle - need to move back up the tree towards the root.

        float distance_to_target = distance_wrapper.distance(candidate.data, data);
        if( distance_to_target < candidate.radius ) {
            // a hit just insert_zoom into this node
            return add( candidate,data );
        } else {
            return insert_zoom( candidate.parent, data );
        }
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
        // There are two possibilities:
        //  1. We are inside the circle - we have zoomed to far need to back out this node.
        //  2. We are outside of the circle - we have

        float distance_to_target = distance_wrapper.distance(candidate.data, query);
        if( distance_to_target < candidate.radius ) {
            // a hit just insert_zoom into this node
            return super.nearestNeighbour( candidate.parent,new DataDistance(candidate,distance_to_target),query );
        } else {
            // we are outwith the circle of the candidate need to back up the tree.
            return nearestNeighbour_zoom( candidate.parent, query );
        }
    }

}
