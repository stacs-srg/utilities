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

/**
 * SimMetrics - SimMetrics is a java library of Similarity or Distance
 * Metrics, e.g. Levenshtein Distance, that provide float based similarity
 * measures between String Data. All metrics return consistant measures
 * rather than unbounded similarity scores.
 *
 * Copyright (C) 2005 Sam Chapman - Open Source Release v1.1
 *
 * Please Feel free to contact me about this library, I would appreciate
 * knowing quickly what you wish to use it for and any criticisms/comments
 * upon the SimMetric library.
 *
 * email:       s.chapman@dcs.shef.ac.uk
 * www:         http://www.dcs.shef.ac.uk/~sam/
 * www:         http://www.dcs.shef.ac.uk/~sam/stringmetrics.html
 *
 * address:     Sam Chapman,
 *              Department of Computer Science,
 *              University of Sheffield,
 *              Sheffield,
 *              S. Yorks,
 *              S1 4DP
 *              United Kingdom,
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Code included for speed tests - modified to comply with our interfaces.
 */
import uk.ac.standrews.cs.utilities.metrics.coreConcepts.NamedMetric;

public class Jaro implements NamedMetric<String> {

    @Override
    public String getMetricName() {
        return "Jaro";
    }

    public double distance(String a, String b) {
        return 1.0 - this.compare(a, b);
    }

    @Override
    public double normalisedDistance(String a, String b) {
        return distance(a, b);
    }

    public double compare(String a, String b) {

        double check = NamedMetric.checkNullAndEmpty(a, b);
        if (check != -1) return check;

        if (!a.isEmpty() && !b.isEmpty()) {

            int halfLength = Math.max(0, Math.max(a.length(), b.length()) / 2 - 1);
            char[] charsA = a.toCharArray();
            char[] charsB = b.toCharArray();
            int[] commonA = getCommonCharacters(charsA, charsB, halfLength);
            int[] commonB = getCommonCharacters(charsB, charsA, halfLength);

            double transpositions = 0.0;
            int commonCharacters = 0;

            for(int length = commonA.length; commonCharacters < length && commonA[commonCharacters] > -1; ++commonCharacters) {
                if (commonA[commonCharacters] != commonB[commonCharacters]) {
                    ++transpositions;
                }
            }

            if (commonCharacters == 0) {
                return 0.0;
            } else {
                double aCommonRatio = (float)commonCharacters / (float)a.length();
                double bCommonRatio = (float)commonCharacters / (float)b.length();
                double transpositionRatio = ((float)commonCharacters - transpositions / 2.0) / (float)commonCharacters;
                return (aCommonRatio + bCommonRatio + transpositionRatio) / 3.0;
            }
        } else {
            return 0.0;
        }
    }

    private static int[] getCommonCharacters(char[] charsA, char[] charsB, int separation) {

        int[] common = new int[Math.min(charsA.length, charsB.length)];
        boolean[] matched = new boolean[charsB.length];
        int commonIndex = 0;
        int i = 0;

        for(int length = charsA.length; i < length; ++i) {
            char character = charsA[i];
            int index = indexOf(character, charsB, i - separation, i + separation + 1, matched);
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

    private static int indexOf(char character, char[] buffer, int fromIndex, int toIndex, boolean[] matched) {

        int j = Math.max(0, fromIndex);

        for(int length = Math.min(toIndex, buffer.length); j < length; ++j) {
            if (buffer[j] == character && !matched[j]) {
                return j;
            }
        }

        return -1;
    }

    public static void main(String[] a) {

        NamedMetric.printExamples(new Jaro());
    }
}

