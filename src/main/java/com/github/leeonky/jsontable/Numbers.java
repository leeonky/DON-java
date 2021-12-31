package com.github.leeonky.jsontable;

public class Numbers {

    public static Number parseNumber(String content) {
        return new Numbers().parse(content);
    }

    private Number parse(String content) {
        if (content.length() == 0)
            return null;
        Token token = new Token(content);
        return token.getInteger(token.getSign());
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

        public Number getInteger(int negative) {
            if (isTheEnd())
                return null;
            int value = 0;
            while (index < chars.length) {
                char c = chars[index++];
                if (c == '_') {
                    if (isTheEnd())
                        return null;
                    continue;
                }
                int digit = Character.digit(c, 10);
                if (digit < 0)
                    return null;
                value = value * 10 + digit;
            }
            return negative * value;
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
    }
}
