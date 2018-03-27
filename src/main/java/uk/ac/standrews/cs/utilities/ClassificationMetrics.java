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
package uk.ac.standrews.cs.utilities;

/**
 * Find list of all formulae here: https://en.wikipedia.org/wiki/Precision_and_recall
 */
@SuppressWarnings("unused")
public class ClassificationMetrics {

    // Precision is TP / (TP + FP).
    public static double precision(int true_positives, int false_positives) {

        return (double) true_positives / (true_positives + false_positives);
    }

    // Recall is TP / (TP + FN)
    public static double recall(int true_positives, int false_negatives) {

        return (double) true_positives / (true_positives + false_negatives);
    }

    // Accuracy is (TP + TN) / (TP + TN + FP + FN)
    public static double accuracy(int true_positives, int true_negatives, int false_positives, int false_negatives) {

        int total_cases = true_positives + true_negatives + false_positives + false_negatives;
        return (double) (true_positives + true_negatives) / total_cases;
    }

    // F1 is (2 * TP) / (2 * TP + FP + FN)
    public static double F1(int true_positives, int false_positives, int false_negatives) {

        return (double) (true_positives * 2) / (true_positives * 2 + false_positives + false_negatives);
    }
}
