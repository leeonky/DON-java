package com.github.leeonky.jsontable;

import java.math.BigInteger;
import java.util.Iterator;

public class Numbers {

    public static Number parseNumber(String content) {
        if (content.length() == 0)
            return null;
        Token token = new Token(content);
        int sign = token.getSign();
        int radix = token.getRadix();
        return token.parseInteger(sign, radix);
    }

    private static class Token {
        private int index;
        private final char[] chars;

        private Token(String code) {
            chars = code.toCharArray();
        }

        private boolean takeChar(char c) {
            boolean result;
            if (result = chars[index] == c)
                index++;
            return result;
        }

        private boolean startWith(String str) {
            if (index + str.length() < chars.length) {
                for (int i = 0; i < str.length(); i++) {
                    if (str.charAt(i) != chars[index + i])
                        return false;
                }
                index += str.length();
                return true;
            }
            return false;
        }

        public int getRadix() {
            if (startWith("0x") || startWith("0X"))
                return 16;
            return 10;
        }

        public static class IntegerBoundary {
            private final int limit;
            private final int limitBeforeMul;
            private final int radix;

            public IntegerBoundary(int sign, int radix) {
                limit = sign > 0 ? -Integer.MAX_VALUE : Integer.MIN_VALUE;
                limitBeforeMul = limit / radix;
                this.radix = radix;
            }

            private boolean isOverflow(int value, int digit) {
                return value < limitBeforeMul || value * radix < limit + digit;
            }
        }

        public static class LongBoundary {
            private final long limit;
            private final long limitBeforeMul;
            private final int radix;

            public LongBoundary(int sign, int radix) {
                limit = sign > 0 ? -Long.MAX_VALUE : Long.MIN_VALUE;
                limitBeforeMul = limit / radix;
                this.radix = radix;
            }

            private boolean isOverflow(long value, int digit) {
                return value < limitBeforeMul || value * radix < limit + digit;
            }
        }

        public Number parseInteger(int sign, int radix) {
            if (isTheEnd())
                return null;
            int value = 0;
            IntegerBoundary integerBoundary = new IntegerBoundary(sign, radix);
            for (char c : leftChars()) {
                if (c == '_') {
                    if (isTheEnd())
                        return null;
                    continue;
                }
                if (c == '.')
                    return parseDoubleWithDot(String.valueOf(value), sign, radix);
                int digit = Character.digit(c, radix);
                if (digit < 0)
                    return null;
                if (integerBoundary.isOverflow(value, digit))
                    return parseLong(((long) value * radix) - digit, sign, radix);
                value = value * radix - digit;
            }
            return -sign * value;
        }

        private Number parseDoubleWithDot(String integer, int sign, int radix) {
            if (radix != 10)
                return null;
            StringBuilder stringBuilder = new StringBuilder(chars.length);
            if (integer.equals("0"))
                stringBuilder.append('-');
            stringBuilder.append(integer);
            stringBuilder.append('.');
            for (char c : leftChars()) {
                if (c == '_') {
                    if (isTheEnd())
                        return null;
                    continue;
                }
                if (c > '9' || c < '0')
                    return null;
                stringBuilder.append(c);
            }
            return -sign * Double.parseDouble(stringBuilder.toString());
        }

        private Number parseLong(long value, int sign, int radix) {
            LongBoundary longBoundary = new LongBoundary(sign, radix);
            for (char c : leftChars()) {
                if (c == '_') {
                    if (isTheEnd())
                        return null;
                    continue;
                }
                if (c == '.')
                    return parseDoubleWithDot(String.valueOf(value), sign, radix);
                int digit = Character.digit(c, radix);
                if (digit < 0)
                    return null;
                if (longBoundary.isOverflow(value, digit)) {
                    return parseBigInteger(BigInteger.valueOf(value).multiply(BigInteger.valueOf(radix))
                            .subtract(BigInteger.valueOf(digit)), sign, radix);
                }
                value = value * radix - digit;
            }
            return -sign * value;
        }

        private Number parseBigInteger(BigInteger value, int sign, int radix) {
            BigInteger radixBigInteger = BigInteger.valueOf(radix);
            for (char c : leftChars()) {
                if (c == '_') {
                    if (isTheEnd())
                        return null;
                    continue;
                }
                if (c == '.')
                    return parseDoubleWithDot(value.toString(), sign, radix);
                int digit = Character.digit(c, radix);
                if (digit < 0)
                    return null;
                value = value.multiply(radixBigInteger).subtract(BigInteger.valueOf(digit));
            }
            return sign > 0 ? value.negate() : value;
        }

        private boolean isTheEnd() {
            return index == chars.length;
        }

        public int getSign() {
            if (takeChar('-'))
                return -1;
            takeChar('+');
            return 1;
        }

        private Iterable<Character> leftChars() {
            return () -> new Iterator<Character>() {

                @Override
                public boolean hasNext() {
                    return !isTheEnd();
                }

                @Override
                public Character next() {
                    return chars[index++];
                }
            };
        }
    }
}
