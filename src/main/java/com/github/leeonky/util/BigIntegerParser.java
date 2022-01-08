package com.github.leeonky.util;

import java.math.BigInteger;

import static java.math.BigInteger.valueOf;

class BigIntegerParser extends Parser<BigInteger, BigInteger> {
    private final BigInteger radixBigInteger;

    @SuppressWarnings("unchecked")
    public BigIntegerParser(NumberContext numberContext) {
        super(numberContext, null,
                new Postfix<>("l", valueOf(Long.MAX_VALUE), valueOf(Long.MIN_VALUE), BigInteger::longValue));
        radixBigInteger = valueOf(numberContext.getRadix());
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
        number = number.multiply(radixBigInteger).subtract(valueOf(digit));
    }

    @Override
    public boolean isOverflow(int digit) {
        return false;
    }
}
