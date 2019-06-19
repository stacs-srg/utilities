/*
 * Copyright 2019 Systems Research Group, University of St Andrews:
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

public class PrintMetricExamples {

    public static void main(String[] args) {

        BagDistance.main(null);
        Cosine.main(null);
        DamerauLevenshtein.main(null);
        Dice.main(null);
        Jaccard.main(null);
        Jaro.main(null);
        JaroWinkler.main(null);
        JensenShannon.main(null);
        JensenShannonKullbackLeibler.main(null);
        Levenshtein.main(null);
        LongestCommonSubstring.main(null);
        NeedlemanWunsch.main(null);
        SED.main(null);
        SmithWaterman.main(null);
    }
}
