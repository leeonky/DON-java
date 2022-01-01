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
        return token.parseInteger(token.getSign());
    }

    private static class Token {
        int index;
        final char[] chars;

        private Token(String code) {
            chars = code.toCharArray();
        }

        private boolean takeChar(char c) {
            boolean result;
            if (result = chars[index] == c)
                index++;
            return result;
        }

        public static class IntegerBoundary {
            final int limit;
            final int limitBeforeMul;

            public IntegerBoundary(int negative, int radix) {
                limit = negative > 0 ? -Integer.MAX_VALUE : Integer.MIN_VALUE;
                limitBeforeMul = limit / radix;
            }

            private boolean isOverflow(int value, int digit) {
                return value < limitBeforeMul || value * 10 < limit + digit;
            }
        }

        public Number parseInteger(int negative) {
            if (isTheEnd())
                return null;
            int value = 0;
            IntegerBoundary integerBoundary = new IntegerBoundary(negative, 10);
            for (char c : leftChars()) {
                if (c == '_') {
                    if (isTheEnd())
                        return null;
                    continue;
                }
                int digit = Character.digit(c, 10);
                if (digit < 0)
                    return null;
                if (integerBoundary.isOverflow(value, digit))
                    return parseLong(negative, ((long) value * 10) - digit);
                value = value * 10 - digit;
            }
            return -negative * value;
        }

        private Number parseLong(int negative, long value) {
            for (char c : leftChars()) {
                if (c == '_') {
                    if (isTheEnd())
                        return null;
                    continue;
                }
                int digit = Character.digit(c, 10);
                value = value * 10 - digit;
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
