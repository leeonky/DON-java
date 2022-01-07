package com.github.leeonky.util;

class IntegerParser extends Parser<Integer, Long> {
    private final int limit;
    private final int limitBeforeMul;

    public IntegerParser(NumberContext numberContext) {
        super(numberContext, () -> new LongParser(numberContext));
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

    @Override
    public Number convertByPostfix(String postfix, Integer value) {
        switch (postfix) {
            case "y":
                if (value > Byte.MAX_VALUE || value < Byte.MIN_VALUE)
                    throw new NumberOverflowException(numberContext.getContent());
                return value.byteValue();
            case "s":
                if (value > Short.MAX_VALUE || value < Short.MIN_VALUE)
                    throw new NumberOverflowException(numberContext.getContent());
                return value.shortValue();
        }
        return value;
    }
}
