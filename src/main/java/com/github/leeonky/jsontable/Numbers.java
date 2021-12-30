package com.github.leeonky.jsontable;

public class Numbers {

    public static Number parseNumber(String content) {
        return new Numbers().parse(content);
    }

    private Number parse(String content) {
        if (content.length() == 0)
            return null;
        Token token = new Token(content);
        int negative = token.getSign();
        Integer value = token.getInteger();
        if (value == null)
            return null;
        return negative * value;
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

        public Integer getInteger() {
            int value = 0;
            while (index < chars.length) {
                int digit = Character.digit(chars[index++], 10);
                if (digit < 0)
                    return null;
                value = value * 10 + digit;
            }
            return value;
        }

        public int getSign() {
            if (takeChar('-'))
                return -1;
            takeChar('+');
            return 1;
        }
    }
}
