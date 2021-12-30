package com.github.leeonky.jsontable;

import org.junit.jupiter.api.Test;

import static com.github.leeonky.jsontable.Numbers.parseNumber;
import static org.assertj.core.api.Assertions.assertThat;

class NumbersTest {

    @Test
    void parse_int_number() {
        assertParse("0", 0);
        assertParse("1", 1);
        assertParse("24", 24);
        assertParse("+24", 24);
    }

    @Test
    void parse_negative_int_number() {
        assertParse("-24", -24);
    }

    @Test
    void invalid_number() {
        assertParse("", null);
        assertParse("notNumber", null);
    }

    private void assertParse(String inputCode, Number expected) {
        assertThat(parseNumber(inputCode)).isEqualTo(expected);
    }
}