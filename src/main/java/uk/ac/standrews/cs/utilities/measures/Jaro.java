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
package uk.ac.standrews.cs.utilities.measures;

/**
 * SimMetrics - SimMetrics is a java library of Similarity or Distance
 * Metrics, e.g. Levenshtein Distance, that provide float based similarity
 * measures between String Data. All metrics return consistant measures
 * rather than unbounded similarity scores.
 * <p>
 * Copyright (C) 2005 Sam Chapman - Open Source Release v1.1
 * <p>
 * Please Feel free to contact me about this library, I would appreciate
 * knowing quickly what you wish to use it for and any criticisms/comments
 * upon the SimMetric library.
 * <p>
 * email:       s.chapman@dcs.shef.ac.uk
 * www:         http://www.dcs.shef.ac.uk/~sam/
 * www:         http://www.dcs.shef.ac.uk/~sam/stringmetrics.html
 * <p>
 * address:     Sam Chapman,
 * Department of Computer Science,
 * University of Sheffield,
 * Sheffield,
 * S. Yorks,
 * S1 4DP
 * United Kingdom,
 * <p>
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * <p>
 * Code included for speed tests - modified to comply with our interfaces.
 */
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

public class Jaro extends StringMeasure {

    @Override
    public String getMeasureName() {
        return "Jaro";
    }

    @Override
    public boolean maxDistanceIsOne() { return true; }

    @Override
    public double calculateDistance(final String x, final String y) {

        return 1.0 - similarity(clean(x), clean(y));
    }

    protected double similarity(final String x, final String y) {

        final int halfLength = Math.max(0, Math.max(x.length(), y.length()) / 2 - 1);
        final char[] charsOfX = x.toCharArray();
        final char[] charsOfY = y.toCharArray();
        final int[] common1 = getCommonCharacters(charsOfX, charsOfY, halfLength);
        final int[] common2 = getCommonCharacters(charsOfY, charsOfX, halfLength);

        double transpositions = 0.0;
        int commonCharacters = 0;

        for (int length = common1.length; commonCharacters < length && common1[commonCharacters] > -1; ++commonCharacters) {
            if (common1[commonCharacters] != common2[commonCharacters]) {
                ++transpositions;
            }
        }

        if (commonCharacters == 0) {
            return 0.0;
        } else {
            final double commonRatioX = (float) commonCharacters / (float) x.length();
            final double commonRatioY = (float) commonCharacters / (float) y.length();
            final double transpositionRatio = ((float) commonCharacters - transpositions / 2.0) / (float) commonCharacters;

            return (commonRatioX + commonRatioY + transpositionRatio) / 3.0;
        }
    }

    private static int[] getCommonCharacters(final char[] chars1, final char[] chars2, int separation) {

        final int[] common = new int[Math.min(chars1.length, chars2.length)];
        final boolean[] matched = new boolean[chars2.length];

        int commonIndex = 0;
        int i = 0;

        for (int length = chars1.length; i < length; ++i) {

            final char character = chars1[i];
            final int index = indexOf(character, chars2, i - separation, i + separation + 1, matched);

            if (index > -1) {
                common[commonIndex++] = character;
                matched[index] = true;
            }
        }

        if (commonIndex < common.length) {
            common[commonIndex] = -1;
        }

        return common;
    }

    private static int indexOf(final char character, final char[] buffer, final int fromIndex, final int toIndex, final boolean[] matched) {

        int j = Math.max(0, fromIndex);

        for (int length = Math.min(toIndex, buffer.length); j < length; ++j) {
            if (buffer[j] == character && !matched[j]) {
                return j;
            }
        }

        return -1;
    }

    public static void main(String[] a) {

        new Jaro().printExamples();
    }
}

