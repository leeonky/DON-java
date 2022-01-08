package com.github.leeonky.util;

import java.math.BigInteger;

class LongParser extends Parser<Long, BigInteger> {
    private static final OverflowPostfix<Long> POSTFIX_BYTE = new OverflowPostfix<>("y");
    private static final OverflowPostfix<Long> POSTFIX_SHORT = new OverflowPostfix<>("s");
    private static final Postfix<Long> POSTFIX_LONG = new Postfix<>("l", Long.MAX_VALUE, Long.MIN_VALUE, Long::longValue);
    private static final Postfix<Long> POSTFIX_BIG_INTEGER = new Postfix<>("bi", Long.MAX_VALUE, Long.MIN_VALUE, BigInteger::valueOf);
    private final long limit;
    private final long limitBeforeMul;

    @SuppressWarnings("unchecked")
    public LongParser(NumberContext numberContext) {
        super(numberContext, () -> new BigIntegerParser(numberContext),
                POSTFIX_BYTE,
                POSTFIX_SHORT,
                POSTFIX_LONG,
                POSTFIX_BIG_INTEGER);
        limit = numberContext.getSign() == 1 ? -Long.MAX_VALUE : Long.MIN_VALUE;
        limitBeforeMul = limit / numberContext.getRadix();
    }

    @Override
    public Long combineSignAndResult() {
        return -numberContext.getSign() * number;
    }

    @Override
    public BigInteger appendOverflowDigit(int digit) {
        return BigInteger.valueOf(number).multiply(BigInteger.valueOf(numberContext.getRadix())).subtract(BigInteger.valueOf(digit));
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
