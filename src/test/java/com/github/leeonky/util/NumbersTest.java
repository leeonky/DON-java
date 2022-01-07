package com.github.leeonky.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;

import static com.github.leeonky.util.Numbers.parseNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
                assertParse("1234567890", 1234567890);
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
                assertParse(null, null);
                assertParse("+", null);
                assertParse("-", null);
                assertParse("1_", null);
                assertParse("1x", null);
                assertParse("F", null);
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
                assertParse("0x", null);
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
                assertParse("10.05", 10.05d);
                assertParse("1__0.0__5", 1__0.0__5d);
                assertParse("-0.0", -0.0d);
                assertParse("0.123456789", 0.123456789d);
            }

            @Test
            void dot_should_between_number() {
                assertParse("1.", null);
                assertParse("-.5", null);
                assertParse(".5", null);
                assertParse("1.n", null);
            }

            @Test
            void invalid_double() {
                assertParse("0x1.5", null);
                assertParse("1.1_", null);
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

            }

            @Test
            void invalid_power_number() {
                assertParse("10E0.5", null);
                assertParse("10E5_", null);
                assertParse("10E0xF", null);
                assertParse("10Ea", null);
                assertParse("e1", null);
                assertParse("-e1", null);
                assertParse("0x1E1", 0x1E1);
                assertParse("10E", null);
            }
        }

        @Nested
        class FromLong {

            @Test
            void dot_in_long() {
                assertParse("2147483648.5", 2147483648.5d);
                assertParse("2147483648.05", 2147483648.05d);
                assertParse("2147483648.0__5", 2147483648.0__5d);
            }

            @Test
            void dot_should_between_number() {
                assertParse("2147483648.", null);
                assertParse("2147483648.n", null);
            }

            @Test
            void invalid_double() {
                assertParse("0x2147483648.5", null);
                assertParse("2147483648.1_", null);
            }

            @Test
            void power_number_in_integer() {
                assertParse("2147483648E05", 2147483648E5);
                assertParse("2147483648E1_5", 2147483648E1_5);
                assertParse("2147483648E15", 2147483648E15);
                assertParse("2147483648E-5", 2147483648E-5);
                assertParse("2147483648E+5", 2147483648E5);

            }

            @Test
            void invalid_power_number() {
                assertParse("2147483648E0.5", null);
                assertParse("2147483648E5_", null);
                assertParse("2147483648E0xF", null);
                assertParse("2147483648EA", null);
                assertParse("0x8FFFFFFFE1", 0x8FFFFFFFE1L);
                assertParse("2147483648E", null);
            }
        }

        @Nested
        class FromBigInteger {

            @Test
            void dot_in_big_integer() {
                assertParse("100000000000000000000.5", 100000000000000000000.5d);
                assertParse("100000000000000000000.05", 100000000000000000000.05d);
                assertParse("100000000000000000000.0__5", 100000000000000000000.0__5d);
            }

            @Test
            void dot_should_between_number() {
                assertParse("100000000000000000000.", null);
                assertParse("100000000000000000015.n", null);
            }

            @Test
            void invalid_double() {
                assertParse("0x100000000000000000015.5", null);
                assertParse("100000000000000000015.1_", null);
            }

            @Test
            void power_number_in_integer() {
                assertParse("100000000000000000015E05", 100000000000000000015E5);
                assertParse("100000000000000000015E1_5", 100000000000000000015E1_5);
                assertParse("100000000000000000015E15", 100000000000000000015E15);
                assertParse("100000000000000000015E-5", 100000000000000000015E-5);
                assertParse("100000000000000000015E+5", 100000000000000000015E5);

            }

            @Test
            void invalid_power_number() {
                assertParse("100000000000000000015E0.5", null);
                assertParse("100000000000000000015E5_", null);
                assertParse("100000000000000000015E0xF", null);
                assertParse("100000000000000000015EA", null);
                assertParse("0x100000000000000000015EA", new BigInteger("100000000000000000015EA", 16));
                assertParse("100000000000000000015E", null);
            }
        }

        @Test
        void power_number_in_double() {
            assertParse("0.1E5", 0.1E5);
            assertParse("0.12E5", 0.12E5);
            assertParse("13.24E5", 13.24E5);
        }

        @Test
        void power_char_should_between_number() {
            assertParse(".E0", null);
            assertParse(".e0", null);
            assertParse("0E", null);
            assertParse("0e", null);
            assertParse("0ex", null);
            assertParse("0E.0", null);
        }
    }

    @Test
    void invalid_number() {
        assertParse("+", null);
        assertParse("-", null);
        assertParse("1_", null);
        assertParse("", null);
        assertParse("notNumber", null);
        assertParse(".", null);
    }

    @Nested
    class ParseBigDecimal {

        @Test
        void to_big_decimal_with_huge_power() {
            assertParse("100E400", new BigDecimal("100E400"));
            assertParse("-100E400", new BigDecimal("-100E400"));
        }

        @Test
        void long_float_to_big_decimal() {
            assertParse("1" + String.join("", Collections.nCopies(400, "0")) + ".0", new BigDecimal("1.0E400"));
            assertParse("-1" + String.join("", Collections.nCopies(400, "0")) + ".0", new BigDecimal("-1.0E400"));
        }
    }

    private void assertParse(String inputCode, Number expected) {
        if (expected instanceof BigDecimal) {
            assertThat(((BigDecimal) parseNumber(inputCode)).subtract((BigDecimal) expected)).isZero();
        } else
            assertThat(parseNumber(inputCode)).isEqualTo(expected);
    }

    @Nested
    class Postfix {

        @Nested
        class AsByte {

            @Test
            void integer_as_byte() {
                assertParse("0y", (byte) 0);
                assertParse("1y", (byte) 1);
                assertParse("-1y", (byte) -1);
                assertParse("-128y", (byte) -128);
                assertParse("127y", (byte) 127);
                assertParse("-0x80y", (byte) -128);
                assertParse("0x7fy", (byte) 127);

                assertParseOverflow("128y");
                assertParseOverflow("-129y");
            }

            @Test
            void integer_as_short() {
                assertParse("0s", (short) 0);
                assertParse("1s", (short) 1);
                assertParse("-1s", (short) -1);
                assertParse("-32768s", (short) -32768);
                assertParse("32767s", (short) 32767);
                assertParse("-0x8000s", (short) -32768);
                assertParse("0x7fffs", (short) 32767);

                assertParseOverflow("32768s");
                assertParseOverflow("-32769s");
            }
        }
    }

    private void assertParseOverflow(String code) {
        assertThat(assertThrows(NumberOverflowException.class, () -> parseNumber(code)))
                .hasMessageContaining(String.format("Cannon save [%s] with the given postfix type", code));

    }
}

// TODO postfix Ll Dd Ff Ss Ll Yy BI bi BD bd
// TODO BigDecimal intently
// TODO 0B
// TODO configurable radix postfix