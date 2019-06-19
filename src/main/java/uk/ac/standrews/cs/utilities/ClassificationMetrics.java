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
package uk.ac.standrews.cs.utilities;

/**
 * Find list of all formulae here: https://en.wikipedia.org/wiki/Precision_and_recall
 */
@SuppressWarnings("unused")
public class ClassificationMetrics {

    // Precision is TP / (TP + FP).
    public static double precision(long true_positives, long false_positives) {

        final long denominator = true_positives + false_positives;
        return denominator == 0 ? 0.0 : (double) true_positives / denominator;
    }

    // Recall is TP / (TP + FN)
    public static double recall(long true_positives, long false_negatives) {

        final long denominator = true_positives + false_negatives;
        return denominator == 0 ? 0.0 : (double) true_positives / denominator;
    }

    // Accuracy is (TP + TN) / (TP + TN + FP + FN)
    public static double accuracy(long true_positives, long true_negatives, long false_positives, long false_negatives) {

        final long denominator = true_positives + true_negatives + false_positives + false_negatives;
        return denominator == 0 ? 0.0 : (double) (true_positives + true_negatives) / denominator;
    }

    // F1 is (2 * TP) / (2 * TP + FP + FN)
    public static double F1(long true_positives, long false_positives, long false_negatives) {

        final long denominator = true_positives * 2 + false_positives + false_negatives;
        return denominator == 0 ? 0.0 : (double) (true_positives * 2) / denominator;
    }
}
