package com.github.leeonky.jsontable;

public class Numbers {
    public static Number parseNumber(String content) {
        if (content.length() == 0)
            return null;
        int negative = 1;
        int charIndex = 0;
        if (content.charAt(0) == '-') {
            negative = -1;
            charIndex++;
        } else if (content.charAt(0) == '+') {
            charIndex++;
        }
        int value = 0;
        while (charIndex < content.length()) {
            int digit = Character.digit(content.charAt(charIndex++), 10);
            if (digit < 0)
                return null;
            value = value * 10 + digit;
        }
        return negative * value;
    }
}
