package com.github.leeonky.jsontable;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static com.github.leeonky.jsontable.Numbers.parseNumber;
import static org.assertj.core.api.Assertions.assertThat;

class NumbersTest {

    @Nested
    class IntegerNumber {

        @Nested
        class Radix10 {

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
            void negative() {
                assertParse("-24", -24);
                assertParse("-2147483648", -2147483648);
            }

            @Test
            void over_flow() {
                assertParse("2147483648", 2147483648L);
                assertParse("2147483657", 2147483657L);
                assertParse("-2147483649", -2147483649L);
                assertParse("-2147483658", -2147483658L);
            }

            @Test
            void invalid_number() {
                assertParse("+", null);
                assertParse("-", null);
                assertParse("1_", null);
                assertParse("1x", null);
            }
        }

        @Nested
        class Radix16 {

            @Test
            void parse_int_number() {
                assertParse("0x0", 0);
                assertParse("0xf", 0xf);
                assertParse("0XF", 0XF);
                assertParse("0x1_000", 0x1_000);
                assertParse("+0xff", 0xff);
                assertParse("0x7fffffff", 2147483647);
            }

            @Test
            void negative() {
                assertParse("-0x1f", -0x1f);
                assertParse("-0x80000000", -0x80000000);
            }

            @Test
            void over_flow() {
                assertParse("0x80000000", 0x80000000L);
                assertParse("-0x80000001", -0x80000001L);
            }

            @Test
            void invalid_number() {
                assertParse("+0x", null);
                assertParse("-0x", null);
                assertParse("0x1_", null);
                assertParse("0x1x", null);
            }
        }
    }

    @Nested
    class ParseLong {

        @Nested
        class Radix10 {

            @Test
            void parse_long() {
                assertParse("100000000005", 100000000005L);
                assertParse("100000000005_000", 100000000005_000L);
                assertParse("9223372036854775807", 9223372036854775807L);
            }

            @Test
            void negative() {
                assertParse("0x80000010", 0x80000010L);
                assertParse("-0x80000010", -0x80000010L);
                assertParse("-9223372036854775808", -9223372036854775808L);
            }

            @Test
            void over_flow() {
                assertParse("9223372036854775808", new BigInteger("9223372036854775808"));
                assertParse("9223372036854775811", new BigInteger("9223372036854775811"));
                assertParse("-9223372036854775809", new BigInteger("-9223372036854775809"));
                assertParse("-9223372036854775811", new BigInteger("-9223372036854775811"));
            }

            @Test
            void invalid_number() {
                assertParse("100000000005_", null);
                assertParse("100000000005xx", null);
            }
        }

        @Nested
        class Radix16 {

            @Test
            void parse_long() {
                assertParse("0xfffffffffff", 0xfffffffffffL);
                assertParse("0xfff_ffff_ffff", 0xfff_ffff_ffffL);
                assertParse("0x7fffffffffffffff", 9223372036854775807L);
            }

            @Test
            void negative() {
                assertParse("0x80000010", 0x80000010L);
                assertParse("-0x80000010", -0x80000010L);
                assertParse("-0x8000000000000000", -9223372036854775808L);
            }

            @Test
            void over_flow() {
                assertParse("0x8000000000000000", new BigInteger("9223372036854775808"));
                assertParse("-0x8000000000000001", new BigInteger("-9223372036854775809"));
            }

            @Test
            void invalid_number() {
                assertParse("100000000005_", null);
                assertParse("100000000005xx", null);
            }
        }
    }

    @Nested
    class ParseBigInteger {

        @Nested
        class Radix10 {

            @Test
            void parse_big_int() {
                assertParse("10000000000000000005", new BigInteger("10000000000000000005"));
                assertParse("100000000000000000015", new BigInteger("100000000000000000015"));
                assertParse("100000000000000000_00_05", new BigInteger("1000000000000000000005"));
            }

            @Test
            void negative() {
                assertParse("-10000000000000000005", new BigInteger("-10000000000000000005"));
                assertParse("-1000000000000000_00_05", new BigInteger("-10000000000000000005"));
            }

            @Test
            void invalid_number() {
                assertParse("10000000000000000005_", null);
                assertParse("10000000000000000005xx", null);
            }
        }

        @Nested
        class Radix16 {

            @Test
            void parse_big_int() {
                assertParse("0x80000000000000001", new BigInteger("80000000000000001", 16));
                assertParse("0x800000000000000012", new BigInteger("800000000000000012", 16));
                assertParse("0x800000000000_000_012", new BigInteger("800000000000000012", 16));
            }

            @Test
            void negative() {
                assertParse("-0x10000000000000000005", new BigInteger("-10000000000000000005", 16));
                assertParse("-0x1000000000000000_00_05", new BigInteger("-10000000000000000005", 16));
            }

            @Test
            void invalid_number() {
                assertParse("0x10000000000000000005_", null);
                assertParse("0x10000000000000000005xx", null);
            }
        }
    }

    @Nested
    class ParseFloat {

        @Nested
        class FromInteger {

            @Test
            void dot_in_integer() {
                assertParse("1.5", 1.5d);
                assertParse("1.", 1.0d);
                assertParse("10.05", 10.05d);
                assertParse("1__0.0__5", 1__0.0__5d);
                assertParse(".5", .5d);
                assertParse("-.5", -.5d);
                assertParse("-0.0", -0.0d);
            }

            @Test
            void invalid_double() {
                assertParse("0x1.5", null);
                assertParse("1.1_", null);
                assertParse("1.n", null);
            }

            @Test
            void power_number_in_integer() {
                assertParse("10E05", 10E5);
                assertParse("10E1_5", 10E1_5);
                assertParse("10E15", 10E15);
                assertParse("10E-5", 10E-5);
                assertParse("10E+5", 10E5);
                assertParse("0E5", 0E5);
                assertParse("-0E5", -0E5);

//                TODO
//                assertParse("0.E5", 0E5);
//                assertParse(".0E5", 0E5);
//                assertParse("-0.E5", -0E5);
//                assertParse("-.0E5", -0E5);
            }

            @Test
            void invalid_power_number() {
                assertParse("10E0.5", null);
                assertParse("10E5_", null);
                assertParse("10E0xF", null);
                assertParse("10EA", null);
                assertParse("0x1E1", 0x1E1);
            }
        }

        @Nested
        class FromLong {

            @Test
            void dot_in_long() {
                assertParse("2147483648.5", 2147483648.5d);
                assertParse("2147483648.", 2147483648.0d);
                assertParse("2147483648.05", 2147483648.05d);
                assertParse("2147483648.0__5", 2147483648.0__5d);
            }

            @Test
            void invalid_double() {
                assertParse("0x2147483648.5", null);
                assertParse("2147483648.1_", null);
                assertParse("2147483648.n", null);
            }
        }

        @Nested
        class FromBigInteger {

            @Test
            void dot_in_big_integer() {
                assertParse("100000000000000000000.5", 100000000000000000000.5d);
                assertParse("100000000000000000000.", 100000000000000000000.0d);
                assertParse("100000000000000000000.05", 100000000000000000000.05d);
                assertParse("100000000000000000000.0__5", 100000000000000000000.0__5d);
            }

            @Test
            void invalid_double() {
                assertParse("0x100000000000000000015.5", null);
                assertParse("100000000000000000015.1_", null);
                assertParse("100000000000000000015.n", null);
            }
        }

//        TODO double has E
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

// TODO double => BigDecimal
// TODO postfix Ll Dd Ff Ss Ll Yy BI bi BD bd
// TODO BigDecimal intently
// TODO 1E10
