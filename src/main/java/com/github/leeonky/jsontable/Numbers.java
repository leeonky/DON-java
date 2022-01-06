package com.github.leeonky.jsontable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;

public class Numbers {

    public static Number parseNumber(String content) {
        if (content.length() == 0)
            return null;
        Token token = new Token(content);
        int sign = token.getSign();
        int radix = token.getRadix();
        return token.parseFromInteger(sign, radix);
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

        public Number parseFromInteger(int sign, int radix) {
            if (isTheEnd())
                return null;
            int value = 0;
            IntegerBoundary integerBoundary = new IntegerBoundary(sign, radix);
            for (char c : leftChars()) {
                if (isUnderScore(c))
                    continue;
                if (isDot(c, radix))
                    return parseDoubleWithDot(String.valueOf(value), sign, radix);
                if (isPowerChar(radix, c))
                    return parseDoubleWithPower(String.valueOf(value), sign);
                int digit = Character.digit(c, radix);
                if (digit < 0)
                    return null;
                if (integerBoundary.isOverflow(value, digit))
                    return parseLong(((long) value * radix) - digit, sign, radix);
                value = value * radix - digit;
            }
            return -sign * value;
        }

        private boolean isDot(char c, int radix) {
            return c == '.' && radix == 10 && betweenDigit();
        }

        private boolean betweenDigit() {
            return isPreviousDigit() && !isTheEnd() && !notDigit(chars[index]);
        }

        private boolean isPreviousDigit() {
            return index > 1 && !notDigit(chars[index - 2]);
        }

        private boolean isPowerChar(int radix, char c) {
            return (c == 'e' || c == 'E') && radix == 10 && isPreviousDigit() && !isTheEnd() && digitOrSign(chars[index]);
        }

        private boolean digitOrSign(char c) {
            return !notDigit(c) || c == '+' || c == '-';
        }

        private Number parseDoubleWithPower(String firstPart, int sign) {
            StringBuilder stringBuilder = new StringBuilder(chars.length);
            if (firstPart.equals("0"))
                stringBuilder.append('-');
            stringBuilder.append(firstPart);
            stringBuilder.append('E');
            if (getSign() < 0)
                stringBuilder.append('-');
            for (char c : leftChars()) {
                if (isUnderScore(c))
                    continue;
                if (notDigit(c))
                    return null;
                stringBuilder.append(c);
            }
            return toDoubleOrBigDecimal(sign, stringBuilder.toString());
        }

        private Number toDoubleOrBigDecimal(int sign, String content) {
            double value = Double.parseDouble(content);
            if (Double.isInfinite(value)) {
                BigDecimal bigDecimal = new BigDecimal(content);
                return sign > 0 ? bigDecimal.negate() : bigDecimal;
            } else
                return -sign * value;
        }

        private boolean notDigit(char c) {
            return c > '9' || c < '0';
        }

        private Number parseDoubleWithDot(String firstPart, int sign, int radix) {
            StringBuilder stringBuilder = new StringBuilder(chars.length);
            if (firstPart.equals("0"))
                stringBuilder.append('-');
            stringBuilder.append(firstPart);
            stringBuilder.append('.');
            for (char c : leftChars()) {
                if (isUnderScore(c))
                    continue;
                if (isPowerChar(radix, c))
                    return parseDoubleWithPower(stringBuilder.toString(), sign);
                if (notDigit(c))
                    return null;
                stringBuilder.append(c);
            }
            return toDoubleOrBigDecimal(sign, stringBuilder.toString());
        }

        private Number parseLong(long value, int sign, int radix) {
            LongBoundary longBoundary = new LongBoundary(sign, radix);
            for (char c : leftChars()) {
                if (isUnderScore(c))
                    continue;
                if (isDot(c, radix))
                    return parseDoubleWithDot(String.valueOf(value), sign, radix);
                if (isPowerChar(radix, c))
                    return parseDoubleWithPower(String.valueOf(value), sign);
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

        private boolean isUnderScore(char c) {
            return c == '_' && !isTheEnd();
        }

        private Number parseBigInteger(BigInteger value, int sign, int radix) {
            BigInteger radixBigInteger = BigInteger.valueOf(radix);
            for (char c : leftChars()) {
                if (isUnderScore(c))
                    continue;
                if (isDot(c, radix))
                    return parseDoubleWithDot(value.toString(), sign, radix);
                if (isPowerChar(radix, c))
                    return parseDoubleWithPower(value.toString(), sign);
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
