package com.github.leeonky.util;

public class Numbers {
    public static Number parseNumber(String content) {
        if (content == null || content.length() == 0)
            return null;
        NumberContext numberContext = new NumberContext(content);
        if (numberContext.atTheEnd())
            return null;
        return new IntegerParser(numberContext).parse(0);
    }
}
