package com.github.leeonky.util;

import java.math.BigDecimal;
import java.math.BigInteger;

import static java.math.BigInteger.valueOf;

public class NumberParser {
    private static final NumberPostfix BYTE_POSTFIX = new NumberPostfix(1) {
        @Override
        public Number convertFrom(int number, String content) {
            if (number > Byte.MAX_VALUE || number < Byte.MIN_VALUE)
                throw new NumberOverflowException(content);
            return (byte) number;
        }

        @Override
        public Number convertFrom(long number, String content) {
            if (number > Byte.MAX_VALUE || number < Byte.MIN_VALUE)
                throw new NumberOverflowException(content);
            return (short) number;
        }
    }, SHORT_POSTFIX = new NumberPostfix(1) {
        @Override
        public Number convertFrom(int number, String content) {
            if (number > Short.MAX_VALUE || number < Short.MIN_VALUE)
                throw new NumberOverflowException(content);
            return (short) number;
        }

        @Override
        public Number convertFrom(long number, String content) {
            if (number > Short.MAX_VALUE || number < Short.MIN_VALUE)
                throw new NumberOverflowException(content);
            return (short) number;
        }
    }, LONG_POSTFIX = new NumberPostfix(1) {
        @Override
        public Number convertFrom(int number, String content) {
            return (long) number;
        }

        @Override
        public Number convertFrom(long number, String content) {
            return number;
        }
    }, BIG_INTEGER_POSTFIX = new NumberPostfix(2) {
        @Override
        public Number convertFrom(int number, String content) {
            return valueOf(number);
        }

        @Override
        public Number convertFrom(long number, String content) {
            return valueOf(number);
        }

        @Override
        public Number convertFromBigInteger(String numberString, int radix, String content) {
            return new BigInteger(numberString, radix);
        }
    }, FLOAT_POSTFIX = new NumberPostfix(1) {
        @Override
        public Number convertFrom(int number, String content) {
            return null;
        }

        @Override
        public Number convertFrom(long number, String content) {
            return null;
        }

        @Override
        public Number convertFromDecimal(String numberString, String content) {
            return Float.parseFloat(numberString);
        }

        @Override
        public Number convertFromBigInteger(String numberString, int radix, String content) {
            return Float.parseFloat(numberString);
        }
    };

    public static abstract class NumberPostfix {
        private final int length;

        protected NumberPostfix(int length) {
            this.length = length;
        }

        public abstract Number convertFrom(int number, String content);

        public abstract Number convertFrom(long number, String content);

        public int getLength() {
            return length;
        }

        public Number convertFromBigInteger(String numberString, int radix, String content) {
            throw new NumberOverflowException(content);
        }

        public Number convertFromDecimal(String numberString, String content) {
            throw new NumberOverflowException(content);
        }
    }

    public Number parse(String content) {
        if (content == null)
            return null;
        int length = content.length();
        if (length == 0)
            return null;
        int sign = 1;
        int index = 0;
        if (content.charAt(index) == '+') {
            if (++index == length)
                return null;
        }
        if (content.charAt(index) == '-') {
            if (++index == length)
                return null;
            sign = -1;
        }
        int radix = 10;
        if (content.startsWith("0x", index) || content.startsWith("0X", index)) {
            if ((index += 2) == length)
                return null;
            radix = 16;
        }

        NumberPostfix postfix = null;
        switch (content.charAt(length - 1)) {
            case 'y':
            case 'Y':
                postfix = BYTE_POSTFIX;
                break;
            case 's':
            case 'S':
                postfix = SHORT_POSTFIX;
                break;
            case 'l':
            case 'L':
                postfix = LONG_POSTFIX;
                break;
            case 'f':
            case 'F':
                if (radix == 10)
                    return directlyParse(content, length, sign, index, radix, 1, FLOAT_POSTFIX);
                break;
            default:
                if (content.endsWith("bi") || content.endsWith("BI"))
                    return directlyParse(content, length, sign, index, radix, 2, BIG_INTEGER_POSTFIX);
                break;
        }
        if (postfix != null && index == (length -= postfix.getLength()))
            return null;
        return parseFromInteger(content, length, sign, index, radix, postfix);
    }

    private Number directlyParse(String content, int length, int sign, int index, int radix, int subLength, NumberPostfix postfix) {
        if (index == (length -= subLength))
            return null;
        return continueParseBigInteger(radix, index, content, length, postfix, newStringBuilder(length, sign));
    }

    private StringBuilder newStringBuilder(int length, int sign) {
        StringBuilder stringBuilder = new StringBuilder(length);
        if (sign == -1)
            stringBuilder.append('-');
        return stringBuilder;
    }

    private Number parseFromInteger(String content, int length, int sign, int index, int radix, NumberPostfix postfix) {
        int number = 0;
        int limit = sign == 1 ? -Integer.MAX_VALUE : Integer.MIN_VALUE;
        int limitBeforeMul = limit / radix;
        while (index < length) {
            char c = content.charAt(index++);
            if (c == '_' && index != length)
                continue;
            int digit = getDigit(radix, c);
            if (digit < 0) {
                if (isFloatDot(radix, c, index, length, content))
                    return parseDoubleWithDot(toStringBuilder(radix, sign, number, length), radix, content, index, length, postfix);
                if (isPowerChar(radix, c, index, length, content))
                    return parseDoubleWithPower(content, index, length, toStringBuilder(radix, sign, number, length), postfix);
                return null;
            }
            if (isOverflow(digit, number, limit, limitBeforeMul, radix))
                return continueParseLong(sign, radix, number, digit, index, content, length, postfix);
            number = number * radix - digit;
        }
        number = -number * sign;
        return postfix != null ? postfix.convertFrom(number, content) : number;
    }

    private Number parseDoubleWithPower(String content, int index, int length, StringBuilder stringBuilder, NumberPostfix postfix) {
        stringBuilder.append('E');
        int eSign = 1;
        if (content.charAt(index) == '+') {
            if (++index == length)
                return null;
        }
        if (content.charAt(index) == '-') {
            if (++index == length)
                return null;
            eSign = -1;
        }
        if (eSign == -1)
            stringBuilder.append('-');
        while (index < length) {
            char c = content.charAt(index++);
            if (c == '_' && index != length)
                continue;
            if (notDigit(c))
                return null;
            stringBuilder.append(c);
        }
        return toDoubleOrBigDecimal(stringBuilder, postfix, content);
    }

    private boolean isPowerChar(int radix, char c, int index, int length, String content) {
        return (c == 'e' || c == 'E') && radix == 10
                && afterDigit(index, content) && beforeSignOrDigit(index, length, content);
    }

    private boolean beforeSignOrDigit(int index, int length, String content) {
        if (index >= length)
            return false;
        char c = content.charAt(index);
        return (isDigit(c) || c == '-' || c == '+');
    }

    private boolean isFloatDot(int radix, char c, int index, int length, String content) {
        return c == '.' && radix == 10 && afterDigit(index, content) && beforeDigit(index, length, content);
    }

    private boolean beforeDigit(int index, int length, String content) {
        return index < length && isDigit(content.charAt(index));
    }

    private boolean afterDigit(int index, String content) {
        return index > 1 && isDigit(content.charAt(index - 2));
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private Number parseDoubleWithDot(StringBuilder stringBuilder, int radix, String content, int index, int length, NumberPostfix postfix) {
        stringBuilder.append('.');
        while (index < length) {
            char c = content.charAt(index++);
            if (c == '_' && index != length)
                continue;
            if (isPowerChar(radix, c, index, length, content))
                return parseDoubleWithPower(content, index, length, stringBuilder, postfix);
            if (notDigit(c))
                return null;
            stringBuilder.append(c);
        }
        return toDoubleOrBigDecimal(stringBuilder, postfix, content);
    }

    private boolean notDigit(char c) {
        return c < '0' || c > '9';
    }

    private Number toDoubleOrBigDecimal(StringBuilder stringBuilder, NumberPostfix postfix, String content) {
        String numberString = stringBuilder.toString();
        if (postfix != null)
            return postfix.convertFromDecimal(numberString, content);
        double d = Double.parseDouble(numberString);
        if (Double.isInfinite(d))
            return new BigDecimal(numberString);
        return d;
    }

    private Number continueParseLong(int sign, int radix, long number, int digit, int index,
                                     String content, int length, NumberPostfix postfix) {
        number = number * radix - digit;
        long limit = sign == 1 ? -Long.MAX_VALUE : Long.MIN_VALUE;
        long limitBeforeMul = limit / radix;
        while (index < length) {
            char c = content.charAt(index++);
            if (c == '_' && index != length)
                continue;
            digit = getDigit(radix, c);
            if (digit < 0) {
                if (isFloatDot(radix, c, index, length, content))
                    return parseDoubleWithDot(toStringBuilder(radix, sign, number, length), radix, content, index, length, postfix);
                if (isPowerChar(radix, c, index, length, content))
                    return parseDoubleWithPower(content, index, length, toStringBuilder(radix, sign, number, length), postfix);
                return null;
            }
            if (isOverflow(digit, number, limit, limitBeforeMul, radix))
                return continueParseBigInteger(radix, index, content, length, postfix,
                        toStringBuilder(radix, sign, number, length).append(c));
            number = number * radix - digit;
        }
        number = -number * sign;
        return postfix == null ? number : postfix.convertFrom(number, content);
    }

    private Number continueParseBigInteger(int radix, int index, String content, int length,
                                           NumberPostfix postfix, StringBuilder stringBuilder) {
        while (index < length) {
            char c = content.charAt(index++);
            if (c == '_' && index != length)
                continue;
            int digit = getDigit(radix, c);
            if (digit < 0) {
                if (isFloatDot(radix, c, index, length, content))
                    return parseDoubleWithDot(stringBuilder, radix, content, index, length, postfix);
                if (isPowerChar(radix, c, index, length, content))
                    return parseDoubleWithPower(content, index, length, stringBuilder, postfix);
                return null;
            }
            stringBuilder.append(c);
        }
        return postfix == null ? new BigInteger(stringBuilder.toString(), radix)
                : postfix.convertFromBigInteger(stringBuilder.toString(), radix, content);
    }

    private StringBuilder toStringBuilder(int radix, int sign, long number, int length) {
        return newStringBuilder(length, sign).append(Long.toString(-number, radix));
    }

    private boolean isOverflow(int digit, int number, int limit, int limitBeforeMul, int radix) {
        return number < limitBeforeMul || number * radix < limit + digit;
    }

    private boolean isOverflow(int digit, long number, long limit, long limitBeforeMul, int radix) {
        return number < limitBeforeMul || number * radix < limit + digit;
    }

    private int getDigit(int radix, char c) {
        int value;
        if (radix > 10) {
            if (c >= 'a')
                value = c - 'a' + 10;
            else if (c >= 'A')
                value = c - 'A' + 10;
            else
                value = c - '0';
        } else
            value = c - '0';
        if (value >= 0 && value < radix)
            return value;
        return -1;
    }
}
