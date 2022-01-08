package com.github.leeonky.util;

class IntegerParser extends Parser<Integer, Long> {
    private final int limit;
    private final int limitBeforeMul;

    @SuppressWarnings("unchecked")
    public IntegerParser(NumberContext numberContext) {
        super(numberContext, () -> new LongParser(numberContext),
                new Postfix<>("y", (int) Byte.MAX_VALUE, (int) Byte.MIN_VALUE, Integer::byteValue),
                new Postfix<>("s", (int) Short.MAX_VALUE, (int) Short.MIN_VALUE, Integer::shortValue)
        );
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
