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

import org.roaringbitmap.RoaringBitmap;

import java.util.Set;

/**
 * Maintains a ring of data (points at some distance bounds from the centroid).
 *
 * Created by al on 26/2/2018.
 */
public class Ring<T> {

    // private final List<T> contents;
    private RoaringBitmap contents;
    private final Pool owner;
    private final MPool<T> mpool;
    private final int ring_number;
    private final float r_min;
    private final float r_max;
    private final Ring inner_ring;
    private boolean consolodated = false; // have the contents from this ring been consolodated (merged) with inner rings.

    public Ring( Pool owner, MPool<T> mpool, int ring_number, float r_min, float r_max, Ring inner_ring) {
        // contents = new ArrayList<>();
        contents = new RoaringBitmap();
        this.ring_number = ring_number;
        this.owner = owner;
        this.mpool = mpool;
        this.r_min = r_min;
        this.r_max = r_max;
        this.inner_ring = inner_ring;
    }


    public void add( int index ) {
        contents.add(index);
    }

    public int size() {
        return contents.getCardinality();
    }

    public int getRing_number() {
        return ring_number;
    }

    public Pool<T> getOwner() {
        return owner;
    }

    public MPool<T> getMPool() {
        return mpool;
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


    public RoaringBitmap getConciseContents() {
        return contents;
    }

    public Set<T> getContents() {

        return getMPool().getValues( getConciseContents() );
    }


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

            contents.or(inner_ring.contents); // was addAll
        }
        consolodated = true;
    }
}
