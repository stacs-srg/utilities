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
package uk.ac.standrews.cs.utilities.mi_file;

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.DataDistance;
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.Metric;

import java.util.*;

/**
 * Text from Giuseppe Amato:
 * <p>
 * This is the main class to use MI-Files. MI-Files allow you to perform approximate similarity search
 * on huge datasets. The technique is based on the use of a space transformation where data objects
 * are represented by ordered sequences of reference objects. The sequence of reference objects that represent
 * a data object is ordered according to the distance of the reference objects from the data object being represented.
 * Distance between two data objects is measured by computing the spearmann footrule distance between
 * the two sequence of reference objects that represent them. The closer the two data objects
 * the most similar the two sequence of reference objects. The index is based on the use of inverted files.
 * More details on the technique can be found in the paper "Approximate Similarity Search in NamedMetric Spaces Using
 * Inverted Files", by Giuseppe Amato and Pasquale Savino, presented at Infoscale 2008.
 * <p>
 * Giuseppe Amato
 * ISTI-CNR, Pisa, Italy
 * giuseppe.amato@isti.cnr.it
 * <p>
 * This version created by al on 10/10/2017.
 */
public class MIFile<T> {

    final Metric<T> distance_wrapper;
    final int n_ro;         // number of reference objects - AMATO
    private final Set<T> reference_objects;
    private final InvertedFile<T> invFile;
    protected int number_of_items;    // Keep a note of how many items are in the data set
    HashMap<T, PostingList<T>> inverted_index = new HashMap<>();
    int ks;                 //number of nearest pivots used when searching - AMATO
    int ki;                 //number of nearest pivots used when indexing  - AMATO
    int max_pos_diff;       //consider just objects with at most max_pos_diff with respect to the query - AMATO

    /**
     * @param d
     * @param reference_objects If an existing MI-File is being opened, it must be the same than the number used when the index was created.
     * @param ki                Specifies the number of reference objects used for indexing. It must be smaller than n_ro
     * @param ks                Specifies the number of reference objects used for searching.
     */
    public MIFile(Metric<T> d, Set<T> reference_objects, int ki, int ks) throws Exception {

        distance_wrapper = d;
        this.reference_objects = reference_objects;
        n_ro = reference_objects.size();
        if (ki >= n_ro) {
            throw new Exception("Illegal number of reference objects - Ki (" + ki + ") must be smaller than number of reference objects (" + n_ro + ")");
        }
        this.ki = ki;
        this.ks = ks;
        this.max_pos_diff = ki;
        invFile = new InvertedFile<T>(n_ro);
        number_of_items = 0;
    }

    /**
     * Insert an object in the index, during the bulk loading procedure.
     * <p>
     * Inserted objects are internally automatically associated with an unique identifier that identifies the object in the
     * MI_File database. When a kNN search is executed the result is a list of such identifiers.
     * The real objects from the database should be retrieved
     * by using this.getDataset().getObjectFromOffset(identifier), or useing the kNNRetrieve() or kNNRetrieveAndSort()
     * methods. The MI_File makes the association between objects
     * and internal identifiers by calling the method setInternalId() of the object during the insertion.
     * Therefore any other use of the method setInternalId() for other purposes is dangerous.
     * <p>
     * Amato - code adapted slightly from Giuseppe's version.
     * Interface changed to match typing and MTree interface.
     *
     * @param data the object to be inserted.
     */
    public void add(T data) throws Exception {

        TreeMap<Double, T> knp = kNearestReferenceObjects(data, ki);

        int score = 0; // the position of each of the pivots (reference objects).

        for (Map.Entry<Double, T> e : knp.entrySet()) {
            invFile.insert(e.getValue(), data, score++);
        }
        number_of_items++;
    }

    /**
     * Find the closest N nodes to @param query.
     *
     * @param query - some data for which to find the nearest N neighbours
     * @param n     the number of neighbours to return
     * @return n neighbours (or as many as possible)
     */
    public List<DataDistance<T>> nearestN(T query, int n) {

        TreeMap<Double, T> query_k_nearest_reference_objects = kNearestReferenceObjects(query, ks);

        return incrementalkNNSearch(query, query_k_nearest_reference_objects, n);
    }

    /**
     * Does the dataset contain the query item.
     *
     * @param query the query to be looked up
     * @return true if query is in the data set and false otherwise
     */
    public boolean contains(T query) {

        List<DataDistance<T>> nearest = nearestN(query, n_ro); // be conservative with search (n=5)
        for (DataDistance<T> dd : nearest) {
            if (dd.value.equals(query)) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return number_of_items;
    }

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

    //******************************************************************************************************************

    /**
     * Retrieves the n nearest reference objects closest to an object. These reference objects will be the object
     * representation in the transformed space.
     *
     * @param object the object to be represented by ordering of reference objects.
     * @param k      the number of reference objects used to represent other objects.
     * @return the ordered list of reference objects that represent the object
     * <p>
     * Signature of method from Amato - code adapted slightly from Giuseppe's version.
     */
    private TreeMap<Double, T> kNearestReferenceObjects(T object, int k) {   // was called kNearestReferenceObjectsSequential in  Giuseppe's version.
        TreeMap<Double, T> result = new TreeMap<>(); // maps from distance to an object

        for (T reference : reference_objects) {

            double dist = distance_wrapper.distance(object, reference);

            T o = reference;

            if (result.size() < k) {
                while (o != null) {

                    o = result.put(dist, o);  // problem is that there may be two objects at this distance so insert may overwrite old -
                    dist += 0.0001f;                     // so increment the distance a little and reinsert the overwritten entry to the map
                }
            } else if (dist < result.lastKey()) {

                while (o != null) {

                    o = result.put(dist, o);  // See comment above that explains (!) this code
                    dist += 0.0001f;
                }
                result.remove(result.lastKey());  // get rid of the previous highest value (before this addition.
            }
        }
        return result;
    }

    private List<DataDistance<T>> incrementalkNNSearch(T query, TreeMap<Double, T> query_k_nearest_reference_objects, int k) {

        CandidateSet candidateSet = new CandidateSet(query, distance_wrapper, ki + 1, k);

        boolean stop = false;

        Iterator<Map.Entry<Double, T>> q_iter = query_k_nearest_reference_objects.entrySet().iterator();

        for (int position = 0; q_iter.hasNext(); position++) { // for each RO in order
            Map.Entry<Double, T> nextRO = q_iter.next();
            T pivot = nextRO.getValue();                        // the data of the reference object

            int low_inv_file_pos = position - max_pos_diff; // was Math.min( 0, position - max_pos_diff ) in als version!
            int high_inv_file_pos = Math.min(position + max_pos_diff, ki);  //With ki upper bound

            PostingList<T> posting_list_of_current_pivot = invFile.getPostingList(pivot, low_inv_file_pos, high_inv_file_pos);

            if (posting_list_of_current_pivot != null) {
                for (PostingListEntry<T> data_in_posting_list : posting_list_of_current_pivot.getEntries()) {
                    T object = data_in_posting_list.getObject();
                    int score = data_in_posting_list.getScore();
                    candidateSet.put(object, score, position, pivot, query_k_nearest_reference_objects.size(), position + 1);
                }
            }

        }

        return candidateSet.getDataDistances();
    }

    public MIFileStructure showStructure() {
        MIFileStructure mifs = new MIFileStructure(this);
        return mifs;
    }


}
