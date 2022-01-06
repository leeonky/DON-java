package com.github.leeonky.util;

public class NumberParser {
    public static Number parseNumber(String content) {
        if (content.length() == 0)
            return null;
        Token token = new Token(content);
        int sign = token.getSign();
        int radix = token.getRadix();
        return token.parseFromInteger(sign, radix);
    }
}
