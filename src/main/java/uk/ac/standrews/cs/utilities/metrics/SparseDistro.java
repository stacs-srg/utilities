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


package uk.ac.standrews.cs.utilities.metrics;

import java.util.ArrayList;
import java.util.Iterator;

import static uk.ac.standrews.cs.utilities.metrics.Shingle.ngramIterator;

/**
 * A class to represent sparse distributions of Strings and their frequencies
 */
public class SparseDistro {

    private ArrayList<QgramDistribution> list = new ArrayList<>();

    private boolean counting;  // distribution is counting occurrences not probabilities.

    public SparseDistro(String x) {

        this.counting = true;
        Iterator<String> iter = ngramIterator(x, 2);

        while( iter.hasNext() ) {
            try {
                average_value( iter.next(),1 );
            } catch (Exception e) {
               // cannot happen in this instance - we are in constructor and counting is true;
            }
        }
    }

    /**
     * Convertor Constructor to convert between formats used.
     * @param fv
     */
    public SparseDistro(FeatureVector fv) {
        for( KeyFreqPair kf : fv.getFeatures() ) {
            QgramDistribution new_sc = new QgramDistribution( kf.qgram, kf.frequency );
            list.add( new_sc );
        }
    }

    /**
     * Constructor that makes a copy of another SparseDistro
     * @param source - the distro to be copied.
     */
    public SparseDistro(SparseDistro source) {

        this.counting = source.counting;
        Iterator<QgramDistribution> iter = source.getIterator();

        while( iter.hasNext() ) { // make a deep copy of the distribution.

            QgramDistribution sc = iter.next();
            QgramDistribution new_sc = new QgramDistribution( sc.key );

            list.add( new_sc );
        }
    }

    public int size() {
        return list.size();
    }

    public boolean is_counting() {
        return counting;
    }

    public Iterator<QgramDistribution> getIterator() {
        return list.iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for( QgramDistribution sc : list ) {
            sb.append( sc.key + ":" + sc.count + " " );
        }
        return sb.toString();
    }

    public void average_value(String key, double value) {

        if( counting ) {
            QgramDistribution sc = new QgramDistribution(key, value);

            int position = list.indexOf(sc);

            if (position == -1) { // not seen this pair before.
                insert(sc);
            } else {
                QgramDistribution already = list.get(position);
                already.count = ( already.count + value ) / 2;
            }
        } else {
            throw new RuntimeException( "Cannot average_value to a probability distribution");
        }
    }

    private void insert(QgramDistribution sc ) {
        int i = 0;
        for( ; i < list.size(); i++ ) {
            if( sc.compareTo(list.get(i)) < 1 )  {
                break;
            }
        }
        list.add(i,sc);
    }

    public static void main(String[] args) {
        SparseDistro sd1 = new SparseDistro( "AABBABACVXSAB");
        System.out.println( sd1 );

        SparseDistro sd2 = new SparseDistro( "AABBABACVXSAB").toProbability();
        System.out.println( sd2 );

    }

    /**
     * Converts a distribution from counting to probabilities
     * @return
     */
    public SparseDistro toProbability() {
        if( counting ) {
            counting = false;
            divide_entries( count_counts() );
        }
        return this;
    }

    private void divide_entries(double denominator) {
        for( QgramDistribution distro : list ) {
            distro.count = distro.count / denominator;
        }
    }

    private double count_counts() {
        double total = 0.0;
        for( QgramDistribution distro : list ) {
            total+= distro.count;
        }
        return total;
    }

    public QgramDistribution getEntry(String key) {
        for(  QgramDistribution entry : list ) {

            int compare = key.compareTo(entry.key);
            if( compare == 0 ) {
                // found it
                return entry;
            }
            else if( compare < 1 )  {
                // past it - it isn't there
                return null;
            }
        }
        return null; // not found it and at end
    }

    public double magnitude() {
        double sumsq = 0.0;
        for( QgramDistribution distro : list ) {
            sumsq+= distro.count * distro.count;
        }
        return Math.sqrt( sumsq );
    }
}
