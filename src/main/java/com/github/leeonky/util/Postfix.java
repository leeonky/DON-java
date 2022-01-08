package com.github.leeonky.util;

import java.util.function.Function;

class Postfix<N extends Number & Comparable<N>> {
    private final String postfix;
    private final N maxValue;
    private final N minValue;
    private final Function<N, Number> convertor;

    public Postfix(String postfix, N maxValue, N minValue, Function<N, Number> convertor) {
        this.postfix = postfix;
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.convertor = convertor;
    }

    public Number transform(N value, String content) {
        if (isOverflow(value))
            throw new NumberOverflowException(content);
        return convertor.apply(value);
    }

    protected boolean isOverflow(N value) {
        return value.compareTo(maxValue) > 0 || value.compareTo(minValue) < 0;
    }

    public boolean matches(Parser<N, ?> parser) {
        return parser.numberContext.endsWith(postfix) || parser.numberContext.endsWith(postfix.toUpperCase());
    }
}
