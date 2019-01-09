package uk.ac.standrews.cs.utilities.metrics;

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.util.Collection;

import static uk.ac.standrews.cs.utilities.metrics.Jaccard.intersection;

public class Dice implements NamedMetric<String> {
    @Override
    public String getMetricName() {
        return "Dice";
    }

    @Override
    public double distance(String x, String y) {
        return 1.0 - this.compare(x, y);
    }

    public double compare(String A, String B) {

        double check = CheckValues.checkNullAndEmpty(A, B);
        if (check != -1) return check;

        Collection agrams = Shingle.ngrams(A,2);
        Collection bgrams = Shingle.ngrams(B,2);

        if (agrams.isEmpty() && bgrams.isEmpty()) {
            return 1.0;
        } else {
            return !agrams.isEmpty() && !bgrams.isEmpty() ? 2.0 * intersection(agrams, bgrams).size() / (double) (agrams.size() + bgrams.size()) : 0.0;
        }
    }


    public static void main(String[] a) {
        Dice dice = new Dice();

        System.out.println("Dice:" );

        System.out.println("empty string/empty string: " + dice.distance("", ""));
        System.out.println("empty string/cat: " + dice.distance("", "cat"));
        System.out.println("cat/empty string: " + dice.distance("cat", ""));
        System.out.println("cat/cat: " + dice.distance("cat", "cat"));
        System.out.println( "pillar/caterpillar: " +  dice.distance( "pillar", "caterpillar" ) );  //  6/11 correct
        System.out.println( "bat/cat: " + dice.distance( "bat", "cat" ) );
        System.out.println( "cat/cart: " + dice.distance( "cat", "cart" ) );
        System.out.println( "cat/caterpillar: " +dice.distance( "cat", "caterpillar" ) );
        System.out.println( "cat/zoo: " + dice.distance( "cat", "zoo" ) );
        System.out.println( "n/zoological: " + dice.distance( "n", "zoological" ) );
    }

}
