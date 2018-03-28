package uk.ac.standrews.cs.utilities.dreampool;

import uk.ac.standrews.cs.utilities.m_tree.Distance;
import uk.ac.standrews.cs.utilities.metrics.CartesianPoint;

/**
 * @author Richard Connor
 * Changed by al to return floats to fit in with Distance elsewhere
 *
 * @param <T>
 */
public class Euc<T extends CartesianPoint> implements Distance<T> {

	public float distance(T x, T y) {
		double[] ys = y.getPoint();
		float acc = 0;
		int ptr = 0;
		for( double xVal : x.getPoint()){
			final double diff = xVal - ys[ptr++];
			acc += diff * diff;
		}
		return (float) Math.sqrt(acc);
	}

}
