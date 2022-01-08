package com.github.leeonky.util;

import java.math.BigInteger;

import static java.math.BigInteger.valueOf;

class BigIntegerParser extends Parser<BigInteger, BigInteger> {
    private static final OverflowPostfix<BigInteger> POSTFIX_BYTE = new OverflowPostfix<>("y");
    private static final OverflowPostfix<BigInteger> POSTFIX_SHORT = new OverflowPostfix<>("s");
    private static final OverflowPostfix<BigInteger> POSTFIX_LONG = new OverflowPostfix<>("l");
    private static final Postfix<BigInteger> POSTFIX_BIG_INTEGER = new Postfix<BigInteger>("bi", null, null, v -> v) {
        @Override
        protected boolean isOverflow(BigInteger value) {
            return false;
        }
    };
    private final BigInteger radixBigInteger;

    @SuppressWarnings("unchecked")
    public BigIntegerParser(NumberContext numberContext) {
        super(numberContext, null,
                POSTFIX_BYTE,
                POSTFIX_SHORT,
                POSTFIX_LONG,
                POSTFIX_BIG_INTEGER);
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
