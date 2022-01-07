package com.github.leeonky.util;

import java.math.BigInteger;

class LongParser extends Parser<Long, BigInteger> {
    private final long limit;
    private final long limitBeforeMul;

    public LongParser(NumberContext numberContext) {
        super(numberContext, () -> new BigIntegerParser(numberContext));
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
