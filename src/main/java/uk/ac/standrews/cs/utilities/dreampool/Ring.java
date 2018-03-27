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
package uk.ac.standrews.cs.utilities.dreampool;

import java.util.ArrayList;
import java.util.List;

/**
 * Maintains a ring of data (points at some distance bounds from the centroid).
 *
 * Created by al on 26/2/2018.
 */
public class Ring<T> {

    private final List<T> contents;
    private final Pool owner;
    private final int ring_number;
    private final float r_min;
    private final float r_max;
    private final Ring inner_ring;

    public Ring( Pool owner, int ring_number, float r_min, float r_max, Ring inner_ring) {
        contents = new ArrayList<>();
        this.ring_number = ring_number;
        this.owner = owner;
        this.r_min = r_min;
        this.r_max = r_max;
        this.inner_ring = inner_ring;
    }

    public void add(T object) {

        contents.add(object);
    }

    public void add(List<T> objects) {

        contents.addAll(objects);
    }


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

    public List<T> getRingContents() {
        return contents;
    }


    public ArrayList<T> getAllContents() {
        ArrayList<T> result = new ArrayList<>();
        addRecursiveContents( result );
        return result;
    }

    private boolean contains(T element) {
        return contents.contains(element);
    }


    public boolean outwardlyContains(T element) {
        Ring<T>[] rings = owner.getRings();

        for( int i = rings.length - 1; i >= ring_number; i-- ) {
            if( rings[i].contains(element) ) {
                return true;
            }
        }
        return false;
    }


    //-------------------------------------------------------------

    private List<T> addRecursiveContents( List<T> result ) {
        result.addAll( contents );
        if( inner_ring != null ) {
            inner_ring.addRecursiveContents(result);
        }
        return result;
    }


}
