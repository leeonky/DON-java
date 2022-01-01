package com.github.leeonky.jsontable;

import java.util.Iterator;

public class Numbers {

    public static Number parseNumber(String content) {
        return new Numbers().parse(content);
    }

    private Number parse(String content) {
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

            public IntegerBoundary(int negative, int radix) {
                limit = negative > 0 ? -Integer.MAX_VALUE : Integer.MIN_VALUE;
                limitBeforeMul = limit / radix;
                this.radix = radix;
            }

            private boolean isOverflow(int value, int digit) {
                return value < limitBeforeMul || value * radix < limit + digit;
            }
        }

        public Number parseInteger(int negative, int radix) {
            if (isTheEnd())
                return null;
            int value = 0;
            IntegerBoundary integerBoundary = new IntegerBoundary(negative, radix);
            for (char c : leftChars()) {
                if (c == '_') {
                    if (isTheEnd())
                        return null;
                    continue;
                }
                int digit = Character.digit(c, radix);
                if (digit < 0)
                    return null;
                if (integerBoundary.isOverflow(value, digit))
                    return parseLong(((long) value * radix) - digit, negative, radix);
                value = value * radix - digit;
            }
            return -negative * value;
        }

        private Number parseLong(long value, int negative, int radix) {
            for (char c : leftChars()) {
                if (c == '_') {
                    if (isTheEnd())
                        return null;
                    continue;
                }
                int digit = Character.digit(c, radix);
                if (digit < 0)
                    return null;
                value = value * radix - digit;
            }
            return -negative * value;
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
