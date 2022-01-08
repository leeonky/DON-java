package com.github.leeonky.util;

import java.math.BigInteger;

class BigIntegerParser extends Parser<BigInteger, BigInteger> {
    private final BigInteger radixBigInteger;

    public BigIntegerParser(NumberContext numberContext) {
        super(numberContext, null);
        radixBigInteger = BigInteger.valueOf(numberContext.getRadix());
    }

    @Override
    public BigInteger combineSignAndResult() {
        return numberContext.getSign() == 1 ? number.negate() : number;
    }

    @Override
    public BigInteger appendOverflowDigit(int digit) {
        return null;
    }

    @Override
    public void appendDigit(int digit) {
        number = number.multiply(radixBigInteger).subtract(BigInteger.valueOf(digit));
    }

    @Override
    public boolean isOverflow(int digit) {
        return false;
    }
}
