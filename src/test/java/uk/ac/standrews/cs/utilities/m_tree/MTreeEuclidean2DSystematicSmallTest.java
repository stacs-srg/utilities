/*
 * Copyright 2017 Systems Research Group, University of St Andrews:
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
package uk.ac.standrews.cs.utilities.m_tree;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;

@RunWith(Parameterized.class)
public class MTreeEuclidean2DSystematicSmallTest extends MTreeEuclidean2DSystematicTest {

    private static final int[] tree_sizes = new int[]{1, 2, 3, 5, 10, 50};
    private static final int number_of_repetitions = 100;

    @Parameters(name = "tree size={0}, repetition={1}, duplicates={2}")
    public static Collection<Object[]> generateData() {

        final Object[][] parameters = new Object[number_of_repetitions * tree_sizes.length * 2][];

        for (int i = 0; i < number_of_repetitions; i++) {
            for (int j = 0; j < tree_sizes.length; j++) {
                parameters[(i * tree_sizes.length + j) * 2] = new Object[]{tree_sizes[j], i, false};
                parameters[(i * tree_sizes.length + j) * 2 + 1] = new Object[]{tree_sizes[j], i, true};
            }
        }

        return Arrays.asList(parameters);
    }

    public MTreeEuclidean2DSystematicSmallTest(final int number_of_points, final int repetition_number, final boolean duplicate_values) {

        super(number_of_points, repetition_number, duplicate_values);
    }
}
