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
 * Created by al on 11/10/2017.
 */
public class CandidateSet<T> {

    private final int maxEntries;
    private final TreeMap<Double, T> orderedRes;         //here we maintain the k current best using the distance as entry
    private final HashMap<T, Double> objectsOfOrderedRes; //here we maintain the k current best using the data as entry
    private final T query;                              // the object for which we are finding NN.
    private final Metric<T> distance_wrapper;
    private int k;                                      // number of objects to be retrieved

    private double kDist = Double.MAX_VALUE;

    private HashMap<T, Double> tempResults = new HashMap<>(); // Temporary results?

    public CandidateSet(T query, Metric<T> distance_wrapper, int maxEntries, int k) {

        this.query = query;
        this.distance_wrapper = distance_wrapper;
        this.maxEntries = maxEntries;
        this.k = k;
        orderedRes = new TreeMap<Double, T>();
        objectsOfOrderedRes = new HashMap<T, Double>();
        tempResults = new HashMap<>();
    }

    public void put(T object, int score, int position, T pivot, int postingListToBeAccessed, int accessedPostingLists) {

        int increment = Math.abs(position - score); //the "spearman footrule" distance is the sum of the position differences

        double currentScore;

        Double i = tempResults.get(object);
        if (i != null) {
            currentScore = i - maxEntries + increment;
            i = currentScore;
        } else {
            currentScore = maxEntries * (postingListToBeAccessed - 1) + increment; /* all ints? */
        }

        double minDist = currentScore - maxEntries * (postingListToBeAccessed - accessedPostingLists); // the minimum distance this object can reach

        if (minDist > kDist) {
            tempResults.remove(object);
        } else {                       //the object can enter the best k. Let's insert it
            if (i == null) {
                i = currentScore;
                tempResults.put(object, i);   //insert in the full result list
            }
            orderedInsert(object, currentScore);  //insert in the best current k
        }
    }

    public List<DataDistance<T>> getDataDistances() {
        List<DataDistance<T>> result = new ArrayList<>();

        for (Map.Entry<Double, T> entry : orderedRes.entrySet()) {
            result.add(new DataDistance<>(entry.getValue(), distance_wrapper.distance(entry.getValue(), query)));

        }
        return result;
    }

    //-------------------------------------------------------------

    private void orderedInsert(T object, double dist) {
        double rdist = 0;

        if (orderedRes.size() < k) { // the list is not full - we can do the insert.

            Double d = eliminateDuplicateObjects(object);  // if it is already in objectsOfOrderedRes we remove it
            if (d != null) {                            // it was in the above collection - so remove it from the other data structure
                eliminateDuplicateMetrics(d);
            } // next do the insertion in the lists
            while (object != null) { // loop is necessary because could be two objects at same distance. We are using the TreeMap in a sorted way - hack.
                dist += rdist;
                d = dist;
                objectsOfOrderedRes.put(object, d);           // might do multiple inserts but this is safe - they will overwrite each other in the map.
                object = orderedRes.put(d, object);           //there could be another object with the same distance: we have to reinsert it
                rdist += 0.0001 * Math.random();               // Hack to insert two objects at (almost) the same distance.
                // if there  was an object at this distance already go around the loop again.
            }
            kDist = orderedRes.lastKey();
        } else if (dist < kDist) {

            Double d = eliminateDuplicateObjects(object);  // if it is already in objectsOfOrderedRes we remove it
            if (d != null) {                            // it was in the above collection - so remove it from the other data structure
                eliminateDuplicateMetrics(d);
            }
            if (orderedRes.size() >= k) {
                T to = orderedRes.remove(orderedRes.lastKey());
                objectsOfOrderedRes.remove(to);
            }
            while (object != null) { // loop is necessary because could be two objects at same distance. We are using the TreeMap in a sorted way - hack.

                dist += rdist;
                d = dist;
                objectsOfOrderedRes.put(object, d);
                object = orderedRes.put(d, object);        //there could be another object with the same distance: we have to reinsert it
                rdist += 0.0001 * Math.random();            // Hack as above.

            }
            kDist = orderedRes.lastKey();
        }
    }

    private Double eliminateDuplicateObjects(T object) {
        return objectsOfOrderedRes.remove(object);
    }

    //if object o is already one of the k current best, we eliminate it
    private T eliminateDuplicateMetrics(double d) {
        return orderedRes.remove(d);
    }
}
