package com.github.leeonky.util;

import java.math.BigDecimal;
import java.util.Iterator;

class NumberContext {
    private int currentIndex;
    private final String content;
    private final int sign;
    private final int radix;
    private final int length;

    public NumberContext(String code) {
        content = code;
        length = content.length();
        sign = parseSign();
        radix = parseRadix();
    }

    private boolean compareAndTake(String str) {
        if (content.startsWith(str, currentIndex)) {
            currentIndex += str.length();
            return true;
        }
        return false;
    }

    private int parseRadix() {
        if (compareAndTake("0x") || compareAndTake("0X"))
            return 16;
        return 10;
    }

    public Number tryParseDoubleOrDecimal(Object currentNumber, char c, Postfix<?> postfix) {
        if (isDot(c))
            return parseDoubleWithDot(String.valueOf(currentNumber), postfix);
        if (isPowerChar(c))
            return parseDoubleWithPower(String.valueOf(currentNumber), getSign());
        return null;
    }

    private boolean isDot(char c) {
        return c == '.' && getRadix() == 10 && afterDigit() && beforeDigit();
    }

    private boolean beforeDigit() {
        return !atTheEnd() && !notDigit(content.charAt(currentIndex));
    }

    private boolean afterDigit() {
        return currentIndex > 1 && !notDigit(content.charAt(currentIndex - 2));
    }

    private boolean isPowerChar(char c) {
        return (c == 'e' || c == 'E') && getRadix() == 10 && afterDigit() && !atTheEnd();
    }

    private Number parseDoubleWithPower(String firstPart, int sign) {
        StringBuilder stringBuilder = createStringBuffer(firstPart);
        stringBuilder.append('E');
        if (parseSign() == -1)
            stringBuilder.append('-');
        for (char c : leftChars()) {
            if (notDigit(c))
                return null;
            stringBuilder.append(c);
        }
        return toDoubleOrBigDecimal(sign, stringBuilder.toString(), null);
    }

    private StringBuilder createStringBuffer(String firstPart) {
        StringBuilder stringBuilder = new StringBuilder(content.length());
        if (firstPart.equals("0"))
            stringBuilder.append('-');
        stringBuilder.append(firstPart);
        return stringBuilder;
    }

    private Number toDoubleOrBigDecimal(int sign, String content, Postfix<?> postfix) {
        if (postfix != null) {
            return postfix.transformFloat(sign, content, this);
        }
        double value = Double.parseDouble(content);
        if (Double.isInfinite(value))
            return sign == 1 ? new BigDecimal(content).negate() : new BigDecimal(content);
        else
            return -sign * value;
    }

    private boolean notDigit(char c) {
        return c > '9' || c < '0';
    }

    private Number parseDoubleWithDot(String firstPart, Postfix<?> postfix) {
        StringBuilder stringBuilder = createStringBuffer(firstPart);
        stringBuilder.append('.');
        for (char c : leftChars()) {
            if (isPowerChar(c))
                return parseDoubleWithPower(stringBuilder.toString(), getSign());
            if (notDigit(c))
                return null;
            stringBuilder.append(c);
            if (isPostfixPosition(postfix))
                break;
        }
        return toDoubleOrBigDecimal(getSign(), stringBuilder.toString(), postfix);
    }

    public boolean atTheEnd() {
        return currentIndex == content.length();
    }

    private int parseSign() {
        if (compareAndTake("-"))
            return -1;
        compareAndTake("+");
        return 1;
    }

    public Iterable<Character> leftChars() {
        return () -> new Iterator<Character>() {

            @Override
            public boolean hasNext() {
                return !atTheEnd();
            }

            @Override
            public Character next() {
                char c = content.charAt(currentIndex++);
                if (c != '_' || atTheEnd())
                    return c;
                return next();
            }
        };
    }

    public int getSign() {
        return sign;
    }

    public int getRadix() {
        return radix;
    }

    public String getContent() {
        return content;
    }

    public boolean leftChar(int length) {
        return currentIndex == this.length - length;
    }

    boolean isPostfixPosition(Postfix<?> postfix) {
        return postfix != null && leftChar(postfix.getPostfixLength());
    }
}
