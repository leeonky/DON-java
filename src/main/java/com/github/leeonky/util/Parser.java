package com.github.leeonky.util;

import java.util.List;
import java.util.function.Supplier;

import static java.util.Arrays.asList;

abstract class Parser<T extends Number & Comparable<T>, O extends Number & Comparable<O>> {
    protected final NumberContext numberContext;
    protected T number;
    protected final Supplier<Parser<O, ?>> overflowParser;
    private final List<Postfix<T>> postfixes;

    @SuppressWarnings("unchecked")
    public Parser(NumberContext numberContext, Supplier<Parser<O, ?>> overflowParser, Postfix<T>... postfixes) {
        this.numberContext = numberContext;
        this.overflowParser = overflowParser;
        this.postfixes = asList(postfixes);
    }

    public Number parse(T base) {
        number = base;
        Postfix<T> postfix = fetchPostfix();
        if (postfix != null)
            return postfix.transform(combineSignAndResult(), numberContext.getContent());
        for (char c : numberContext.leftChars()) {
            Number doubleDecimal = numberContext.tryParseDoubleOrDecimal(number, c);
            if (doubleDecimal != null)
                return doubleDecimal;
            int digit = Character.digit(c, numberContext.getRadix());
            if (digit < 0)
                return null;
            if (isOverflow(digit))
                return overflowParser.get().parse(appendOverflowDigit(digit));
            appendDigit(digit);
            postfix = fetchPostfix();
            if (postfix != null)
                return postfix.transform(combineSignAndResult(), numberContext.getContent());
        }
        return combineSignAndResult();
    }

    private Postfix<T> fetchPostfix() {
        return postfixes.stream().filter(p -> p.matches(this)).findFirst().orElse(null);
    }

    public abstract T combineSignAndResult();

    public abstract O appendOverflowDigit(int digit);

    public abstract void appendDigit(int digit);

    public abstract boolean isOverflow(int digit);
}
