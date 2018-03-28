/*
 * Copyright 2018 Systems Research Group, University of St Andrews:
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
package uk.ac.standrews.cs.utilities.dreampool;

import it.uniroma3.mat.extendedset.intset.ConciseSet;

/**
 * Maintains a ring of data (points at some distance bounds from the centroid).
 *
 * Created by al on 26/2/2018.
 */
public class Ring<T> {

    // private final List<T> contents;
    private ConciseSet contents;
    private final Pool owner;
    private final int ring_number;
    private final float r_min;
    private final float r_max;
    private final Ring inner_ring;
    private boolean consolodated = false; // have the contents from this ring been consolodated (merged) with inner rings.

    public Ring( Pool owner, int ring_number, float r_min, float r_max, Ring inner_ring) {
        // contents = new ArrayList<>();
        contents = new ConciseSet();
        this.ring_number = ring_number;
        this.owner = owner;
        this.r_min = r_min;
        this.r_max = r_max;
        this.inner_ring = inner_ring;
    }


    public void add( int index ) {
        contents.add(index);
    }

//    public void add(T object) {
//
//        contents.add(object);
//    }
//
//    public void add(List<T> objects) {
//
//        contents.addAll(objects);
//    }


    public int size() {
        if( inner_ring == null ) {
            return contents.size();
        } else {
            return contents.size() + inner_ring.size();
        }
    }

    public int getRing_number() {
        return ring_number;
    }

    public Pool<T> getOwner() {
        return owner;
    }

    public float getRmin() {
        return r_min;
    }

    public float getRmax() {
        return r_max;
    }

    public Ring getInnerRing() {
        return inner_ring;
    }


//    public ConciseSet getRingContents() {
//        // public List<T> getRingContents() {
//        return contents;
//    }

//    /**
//     * @return the contents of this ring and all inner rings
//     */
//    public ConciseSet getContents() {
//        //public ArrayList<T> getContents() {
//        // ArrayList<T> result = new ArrayList<>();
//        ConciseSet result = new ConciseSet();
//        addRecursiveContents( result );
//        return result;
//    }

    public ConciseSet getContents() {
        return contents;
    }


//    /**
//     * @return the contents of this ring and all inner rings
//     */
//      public ArrayList<T> getContents() {
//         ArrayList<T> result = new ArrayList<>();
//         addRecursiveContents( result );
//         return result;
//    }

//    private boolean contains(T element) {
//        return contents.contains(element);
//    }

    //-------------------------------------------------------------

//    private List<T> addRecursiveContents( List<T> result ) {
//        result.addAll( contents );
//        if( inner_ring != null ) {
//            inner_ring.addRecursiveContents(result);
//        }
//        return result;
//    }

//    private ConciseSet addRecursiveContents( ConciseSet result ) {
//        result.addAll( contents );
//        if( inner_ring != null ) {
//            inner_ring.addRecursiveContents(result);
//        }
//        return result;
//    }

    /**
     * Folds the sets from the inner rings into the set for this ring.
     * Must always be done from inner to outer.
     */

    public void consolidateSets() throws Exception {
        if( consolodated ) {
            throw new Exception( "Ring is consolidated" );
        }
        if( inner_ring != null ) {

            if (!inner_ring.consolodated) {
                throw new Exception("Inner ring has not been consolidated");
            }

            contents.addAll(inner_ring.contents);
        }
        consolodated = true;
    }
}
