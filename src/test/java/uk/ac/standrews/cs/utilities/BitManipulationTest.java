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
package uk.ac.standrews.cs.utilities;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class BitManipulationTest {

    private static final int BITS_PER_BYTE = 8;

    private byte bits;

    @ParameterizedTest
    @ValueSource(bytes = {0, 1, 2, 8, 32, -1, Byte.MAX_VALUE, Byte.MIN_VALUE})
    public void check(byte initialBits) {

        this.bits = initialBits;

        assertAllBitsSetCorrectlyTogether(true);
        assertAllBitsSetCorrectlyTogether(false);

        assertEachBitSetCorrectlyIndividually();

        assertSettingEachBitDoesntAffectOtherBits(true);
        assertSettingEachBitDoesntAffectOtherBits(false);
    }

    private void assertAllBitsSetCorrectlyTogether(final boolean bit) {

        setAllBits(bit);
        assertAllBitsAre(bit);
    }

    private void setAllBits(final boolean bit) {

        for (int bit_position = 0; bit_position < BITS_PER_BYTE; bit_position++) {
            writeBit(bit, bit_position);
        }
    }

    private void assertAllBitsAre(final boolean bit) {

        for (int bit_position = 0; bit_position < BITS_PER_BYTE; bit_position++) {
            assertBitIs(bit_position, bit);
        }
    }

    private void assertEachBitSetCorrectlyIndividually() {

        for (int bit_position = 0; bit_position < BITS_PER_BYTE; bit_position++) {
            assertBitSetCorrectly(bit_position, true);
            assertBitSetCorrectly(bit_position, false);
        }
    }

    private void assertBitSetCorrectly(final int bit_position, final boolean bit) {

        writeBit(bit, bit_position);
        assertBitIs(bit_position, bit);
    }

    private void assertSettingEachBitDoesntAffectOtherBits(final boolean initial_bit) {

        setAllBits(initial_bit);
        final byte start_pattern = bits;

        for (int bit_position = 0; bit_position < BITS_PER_BYTE; bit_position++) {
            assertSettingBitDoesntAffectOtherBits(start_pattern, bit_position);
        }
    }

    private void assertSettingBitDoesntAffectOtherBits(final byte start_pattern, final int bit_position) {

        final boolean bit = BitManipulation.readBit(bits, bit_position);
        writeBit(!bit, bit_position);
        writeBit(bit, bit_position);
        assertEquals(bits, start_pattern);
    }

    private void assertBitIs(final int bit_position, final boolean bit) {

        assertEquals(bit, BitManipulation.readBit(bits, bit_position));
    }

    private void writeBit(final boolean bit, final int bit_position) {

        bits = BitManipulation.writeBit(bits, bit, bit_position);
    }
}
