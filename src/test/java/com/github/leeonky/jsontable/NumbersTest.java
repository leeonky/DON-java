package com.github.leeonky.jsontable;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.github.leeonky.jsontable.Numbers.parseNumber;
import static org.assertj.core.api.Assertions.assertThat;

class NumbersTest {

    @Nested
    class IntegerNumber {

        @Test
        void parse_int_number() {
            assertParse("0", 0);
            assertParse("1", 1);
            assertParse("24", 24);
            assertParse("1_000", 1_000);
            assertParse("+24", 24);
            assertParse("2147483647", 2147483647);
        }

        @Test
        void parse_negative_int_number() {
            assertParse("-24", -24);
            assertParse("-2147483648", -2147483648);
        }

        @Test
        void int_over_flow() {
            assertParse("2147483657", 2147483657L);
            assertParse("2147483648", 2147483648L);
            assertParse("-2147483649", -2147483649L);
            assertParse("-2147483658", -2147483658L);
        }

        @Test
        void invalid_number() {
            assertParse("+", null);
            assertParse("-", null);
            assertParse("1_", null);
        }
    }

    @Nested
    class ParseLong {

        @Test
        void parse_long() {
            assertParse("100000000005", 100000000005L);
            assertParse("100000000005_000", 100000000005_000L);
        }

        @Test
        void invalid_number() {
            assertParse("100000000005_", null);
        }
    }

    @Test
    void invalid_number() {
        assertParse("+", null);
        assertParse("-", null);
        assertParse("1_", null);
        assertParse("", null);
        assertParse("notNumber", null);
    }

    private void assertParse(String inputCode, Number expected) {
        assertThat(parseNumber(inputCode)).isEqualTo(expected);
    }
}