package com.github.leeonky.util;

import java.math.BigInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.math.BigInteger.valueOf;

public class Numbers {
    public static Number parseNumber(String content) {
        if (content == null || content.length() == 0)
            return null;
        NumberContext numberContext = new NumberContext(content);
        if (numberContext.atTheEnd())
            return null;
        return new Parser.IntegerParser(numberContext).parse(0);
    }
}

abstract class Parser<T extends Number & Comparable<T>, O extends Number & Comparable<O>> {
    protected final NumberContext numberContext;
    protected T number;
    protected final Supplier<Parser<O, ?>> overflowParser;
    private final Postfix<T>[] postfixes;

    public Parser(NumberContext numberContext, Supplier<Parser<O, ?>> overflowParser, Postfix<T>[] postfixes) {
        this.numberContext = numberContext;
        this.overflowParser = overflowParser;
        this.postfixes = postfixes;
    }

    public Number parse(T base) {
        number = base;
        //        TODO refactor
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
        for (Postfix<T> postfix : postfixes) {
            if (postfix.matches(this))
                return postfix;
        }
        return null;
    }

    public abstract T combineSignAndResult();

    public abstract O appendOverflowDigit(int digit);

    public abstract void appendDigit(int digit);

    public abstract boolean isOverflow(int digit);

    static class BigIntegerParser extends Parser<BigInteger, BigInteger> {
        private static final Postfix[] postfixes = {
                new Postfix.OverflowPostfix<BigInteger>("y"),
                new Postfix.OverflowPostfix<BigInteger>("s"),
                new Postfix.OverflowPostfix<BigInteger>("l"),
                new Postfix<BigInteger>("bi", null, null, v -> v) {
                    @Override
                    protected boolean isOverflow(BigInteger value) {
                        return false;
                    }
                }};
        private final BigInteger radixBigInteger;

        @SuppressWarnings("unchecked")
        public BigIntegerParser(NumberContext numberContext) {
            super(numberContext, null, postfixes);
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

    static class IntegerParser extends Parser<Integer, Long> {
        private static final Postfix[] postfixes = {
                new Postfix<>("y", (int) Byte.MAX_VALUE, (int) Byte.MIN_VALUE, Integer::byteValue),
                new Postfix<>("s", (int) Short.MAX_VALUE, (int) Short.MIN_VALUE, Integer::shortValue),
                new Postfix<>("l", Integer.MAX_VALUE, Integer.MIN_VALUE, Integer::longValue),
                new Postfix<Integer>("bi", Integer.MAX_VALUE, Integer.MIN_VALUE, BigInteger::valueOf)};
        private final int limit;
        private final int limitBeforeMul;

        @SuppressWarnings("unchecked")
        public IntegerParser(NumberContext numberContext) {
            super(numberContext, () -> new LongParser(numberContext), postfixes);
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

    static class LongParser extends Parser<Long, BigInteger> {
        private static final Postfix[] postfixes = new Postfix[]{
                new Postfix.OverflowPostfix<>("y"),
                new Postfix.OverflowPostfix<>("s"),
                new Postfix<>("l", Long.MAX_VALUE, Long.MIN_VALUE, Long::longValue),
                new Postfix<>("bi", Long.MAX_VALUE, Long.MIN_VALUE, BigInteger::valueOf)};
        private final long limit;
        private final long limitBeforeMul;

        @SuppressWarnings("unchecked")
        public LongParser(NumberContext numberContext) {
            super(numberContext, () -> new BigIntegerParser(numberContext), postfixes);
            limit = numberContext.getSign() == 1 ? -Long.MAX_VALUE : Long.MIN_VALUE;
            limitBeforeMul = limit / numberContext.getRadix();
        }

        @Override
        public Long combineSignAndResult() {
            return -numberContext.getSign() * number;
        }

        @Override
        public BigInteger appendOverflowDigit(int digit) {
            return valueOf(number).multiply(valueOf(numberContext.getRadix())).subtract(valueOf(digit));
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
}

class Postfix<N extends Number & Comparable<N>> {
    private final String postfix;
    private final N maxValue;
    private final N minValue;
    private final Function<N, Number> convertor;
    private final String postfixUpperCase;

    public Postfix(String postfix, N maxValue, N minValue, Function<N, Number> convertor) {
        this.postfix = postfix.toLowerCase();
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.convertor = convertor;
        postfixUpperCase = this.postfix.toUpperCase();
    }

    public Number transform(N value, String content) {
        if (isOverflow(value))
            throw new NumberOverflowException(content);
        return convertor.apply(value);
    }

    protected boolean isOverflow(N value) {
        return value.compareTo(maxValue) > 0 || value.compareTo(minValue) < 0;
    }

    public boolean matches(Parser<N, ?> parser) {
        return parser.numberContext.endsWith(postfix) || parser.numberContext.endsWith(postfixUpperCase);
    }

    static class OverflowPostfix<N extends Number & Comparable<N>> extends Postfix<N> {
        public OverflowPostfix(String postfix) {
            super(postfix, null, null, null);
        }

        @Override
        protected boolean isOverflow(N value) {
            return true;
        }
    }
}
