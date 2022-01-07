package com.github.leeonky.util;

import java.util.function.Supplier;

public abstract class Parser<T extends Number, O extends Number> {
    protected final NumberContext numberContext;
    protected T number;
    protected final Supplier<Parser<O, ?>> overflowParser;

    public Parser(NumberContext numberContext, Supplier<Parser<O, ?>> overflowParser) {
        this.numberContext = numberContext;
        this.overflowParser = overflowParser;
    }

    public Number parse(T base) {
        number = base;
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
        }
        return combineSignAndResult();
    }

    public abstract T combineSignAndResult();

    public abstract O appendOverflowDigit(int digit);

    public abstract void appendDigit(int digit);

    public abstract boolean isOverflow(int digit);
}
