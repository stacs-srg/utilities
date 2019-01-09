package uk.ac.standrews.cs.utilities.metrics;

import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

import java.io.UnsupportedEncodingException;
import java.util.zip.Deflater;

/**
 * For more information about using compression for similarity measures see:
 *                            - Cilibrasi, R. and Vitanyi, P.: Clustering by compression. IEEE Trans.
 *                            Infomat. Th. Submitted, 2004. See: http://arxiv.org/abs/cs.CV/0312044
 *                            - Keogh, E., Lonardi, S. and Ratanamahatana, C.A.: Towards parameter-free
 *                            data mining. Proceedings of the 2004 ACM SIGKDD international conference
 *                            on Knowledge discovery and data mining, pp. 206-215, Seattle, 2004.
 *                            - http://aspn.activestate.com/ASPN/Cookbook/Python/Recipe/306626
 */

// Code from Peter's Febrl system

public class Compression implements NamedMetric<String> {

    public Compression() {
    }

    public double distance(String str1, String str2) {
        return 1 - similarity(str1,str2);
    }

    public double similarity( String str1, String str2 ) {

        double check = CheckValues.checkNullAndEmpty(str1, str2);
        if (check != -1) return check;

        try {
            double c1 = zlibcompress( str1 );
            double c2 = zlibcompress( str2 );
            double c12 = 0.5 * ( zlibcompress(str1+str2) + zlibcompress(str2+str1) );

            if (c12 == 0.0) {
                return 0.0; // Maximal distance
            }

            double w = 1.0 - (c12 - Math.min(c1,c2)) / Math.max(c1,c2);

            return w;

        } catch (UnsupportedEncodingException e) {
            return 0.0; // // Maximal distance
        }
    }

    private double zlibcompress(String str1) throws UnsupportedEncodingException {
        Deflater compresser = new Deflater();
        byte[] input = str1.getBytes("UTF-8");
        byte[] output = new byte[100];
        compresser.setInput(input);
        compresser.finish();
        double compressedDataLength = compresser.deflate(output);
        compresser.end();
        return compressedDataLength;
    }


    @Override
    public String getMetricName() {
        return "Compression";
    }

    public static void main(String[] a) {
        Compression cmpr = new Compression();

        System.out.println("Compression:" );

        System.out.println("empty string/empty string: " + cmpr.distance("", ""));
        System.out.println("empty string/cat: " + cmpr.distance("", "cat"));
        System.out.println("cat/empty string: " + cmpr.distance("cat", ""));
        System.out.println("cat/cat: " + cmpr.distance("cat", "cat"));
        System.out.println( "pillar/caterpillar: " +  cmpr.distance( "pillar", "caterpillar" ) );  //  6/11 correct
        System.out.println( "bat/cat: " + cmpr.distance( "bat", "cat" ) );
        System.out.println( "cat/cart: " + cmpr.distance( "cat", "cart" ) );
        System.out.println( "cat/caterpillar: " +cmpr.distance( "cat", "caterpillar" ) );
        System.out.println( "cat/zoo: " + cmpr.distance( "cat", "zoo" ) );
        System.out.println( "n/zoological: " + cmpr.distance( "n", "zoological" ) );
    }

}
