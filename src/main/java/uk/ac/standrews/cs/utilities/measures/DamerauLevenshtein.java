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

// package blogspot.software_and_algorithms.stern_library.string;

/* Copyright (c) 2012 Kevin L. Stern
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * Code included for speed tests - modified to comply with our interfaces.
 */
import uk.ac.standrews.cs.utilities.measures.coreConcepts.StringMeasure;

import java.util.HashMap;
import java.util.Map;

/**
 * The Damerau-Levenshtein Algorithm is an extension to the Levenshtein
 * Algorithm which solves the edit distance problem between a source string and
 * a target string with the following operations:
 *
 * <ul>
 * <li>Character Insertion</li>
 * <li>Character Deletion</li>
 * <li>Character Replacement</li>
 * <li>Adjacent Character Swap</li>
 * </ul>
 * <p>
 * Note that the adjacent character swap operation is an edit that may be
 * applied when two adjacent characters in the source string match two adjacent
 * characters in the target string, but in reverse order, rather than a general
 * allowance for adjacent character swaps.
 * <p>
 * <p>
 * This implementation allows the client to specify the costs of the various
 * edit operations with the restriction that the cost of two swap operations
 * must not be less than the cost of a delete operation followed by an insert
 * operation. This restriction is required to preclude two swaps involving the
 * same character being required for optimality which, in turn, enables a fast
 * dynamic programming solution.
 * <p>
 * <p>
 * The running time of the Damerau-Levenshtein algorithm is O(n*m) where n is
 * the length of the source string and m is the length of the target string.
 * This implementation consumes O(n*m) space.
 *
 * @author Kevin L. Stern
 */
public class DamerauLevenshtein extends StringMeasure {

    private final int deleteCost, insertCost, replaceCost, swapCost;

    /**
     * @param deleteCost  the cost of deleting a character.
     * @param insertCost  the cost of inserting a character.
     * @param replaceCost the cost of replacing a character.
     * @param swapCost    the cost of swapping two adjacent characters.
     */
    public DamerauLevenshtein(int deleteCost, int insertCost, int replaceCost, int swapCost) {

        /*
         * Required to facilitate the premise to the algorithm that two swaps of the
         * same character are never required for optimality.
         */
        if (2 * swapCost < insertCost + deleteCost) {
            throw new IllegalArgumentException("Unsupported cost assignment");
        }

        this.deleteCost = deleteCost;
        this.insertCost = insertCost;
        this.replaceCost = replaceCost;
        this.swapCost = swapCost;
    }

    @Override
    public String getMeasureName() {
        return "Damerau-Levenshtein";
    }

    @Override
    public boolean maxDistanceIsOne() { return false; }

    /**
     * Compute the Damerau-Levenshtein distance between the specified source
     * string and the specified target string.
     */
    @Override
    public double calculateDistance(final String x, final String y) {

        final String cleanX = clean(x);
        final String cleanY = clean(y);

        if (cleanX.isEmpty()) return cleanY.length();
        if (cleanY.isEmpty()) return cleanY.length();

        final int[][] table = new int[cleanX.length()][cleanY.length()];
        final Map<Character, Integer> sourceIndexByCharacter = new HashMap<>();

        if (cleanX.charAt(0) != cleanY.charAt(0)) {
            table[0][0] = Math.min(replaceCost, deleteCost + insertCost);
        }

        sourceIndexByCharacter.put(cleanX.charAt(0), 0);

        for (int i = 1; i < cleanX.length(); i++) {

            final int deleteDistance = table[i - 1][0] + deleteCost;
            final int insertDistance = (i + 1) * deleteCost + insertCost;
            final int matchDistance = i * deleteCost + (cleanX.charAt(i) == cleanY.charAt(0) ? 0 : replaceCost);
            table[i][0] = Math.min(Math.min(deleteDistance, insertDistance), matchDistance);
        }

        for (int j = 1; j < cleanY.length(); j++) {

            final int deleteDistance = (j + 1) * insertCost + deleteCost;
            final int insertDistance = table[0][j - 1] + insertCost;
            final int matchDistance = j * insertCost + (cleanX.charAt(0) == cleanY.charAt(j) ? 0 : replaceCost);
            table[0][j] = Math.min(Math.min(deleteDistance, insertDistance), matchDistance);
        }

        for (int i = 1; i < cleanX.length(); i++) {

            int maxSourceLetterMatchIndex = cleanX.charAt(i) == cleanY.charAt(0) ? 0 : -1;

            for (int j = 1; j < cleanY.length(); j++) {

                Integer candidateSwapIndex = sourceIndexByCharacter.get(cleanY.charAt(j));
                final int jSwap = maxSourceLetterMatchIndex;
                final int deleteDistance = table[i - 1][j] + deleteCost;
                final int insertDistance = table[i][j - 1] + insertCost;
                int matchDistance = table[i - 1][j - 1];

                if (cleanX.charAt(i) != cleanY.charAt(j)) {
                    matchDistance += replaceCost;
                } else {
                    maxSourceLetterMatchIndex = j;
                }

                final int swapDistance;
                if (candidateSwapIndex != null && jSwap != -1) {
                    final int iSwap = candidateSwapIndex;
                    final int preSwapCost;

                    if (iSwap == 0 && jSwap == 0) {
                        preSwapCost = 0;
                    } else {
                        preSwapCost = table[Math.max(0, iSwap - 1)][Math.max(0, jSwap - 1)];
                    }

                    swapDistance = preSwapCost + (i - iSwap - 1) * deleteCost + (j - jSwap - 1) * insertCost + swapCost;
                } else {
                    swapDistance = Integer.MAX_VALUE;
                }

                table[i][j] = Math.min(Math.min(Math.min(deleteDistance, insertDistance), matchDistance), swapDistance);
            }
            sourceIndexByCharacter.put(cleanX.charAt(i), i);
        }

        return table[cleanX.length() - 1][cleanY.length() - 1];
    }

    public static void main(String[] a) {

        new DamerauLevenshtein(1, 1, 1, 1).printExamples();
    }
}
