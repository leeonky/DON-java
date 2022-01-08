package com.github.leeonky.util;

import java.math.BigInteger;

class IntegerParser extends Parser<Integer, Long> {
    private static final Postfix<Integer> POSTFIX_BYTE = new Postfix<>("y", (int) Byte.MAX_VALUE, (int) Byte.MIN_VALUE, Integer::byteValue);
    private static final Postfix<Integer> POSTFIX_SHORT = new Postfix<>("s", (int) Short.MAX_VALUE, (int) Short.MIN_VALUE, Integer::shortValue);
    private static final Postfix<Integer> POSTFIX_LONG = new Postfix<>("l", Integer.MAX_VALUE, Integer.MIN_VALUE, Integer::longValue);
    private static final Postfix<Integer> POSTFIX_BIG_INTEGER = new Postfix<Integer>("bi", Integer.MAX_VALUE, Integer.MIN_VALUE, BigInteger::valueOf);
    private final int limit;
    private final int limitBeforeMul;

    @SuppressWarnings("unchecked")
    public IntegerParser(NumberContext numberContext) {
        super(numberContext, () -> new LongParser(numberContext),
                POSTFIX_BYTE,
                POSTFIX_SHORT,
                POSTFIX_LONG,
                POSTFIX_BIG_INTEGER);
        limit = numberContext.getSign() == 1 ? -Integer.MAX_VALUE : Integer.MIN_VALUE;
        limitBeforeMul = limit / numberContext.getRadix();
    }

    @Override
    public Integer combineSignAndResult() {
        return -numberContext.getSign() * number;
    }

    @Override
    public Long appendOverflowDigit(int digit) {
        return ((long) number * numberContext.getRadix()) - digit;
    }

    @Override
    public void appendDigit(int digit) {
        number = number * numberContext.getRadix() - digit;
    }

    @Override
    public boolean isOverflow(int digit) {
        return number < limitBeforeMul || number * numberContext.getRadix() < limit + digit;
    }
}
