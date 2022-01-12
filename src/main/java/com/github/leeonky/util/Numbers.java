package com.github.leeonky.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.math.BigInteger.valueOf;

public class Numbers {
    public static Number parseNumber(String content) {
        if (content == null || content.length() == 0)
            return null;
        return parseFromInteger(content);
//        NumberContext numberContext = new NumberContext(content);
//        if (numberContext.atTheEnd())
//            return null;
//        return new Parser.IntegerParser(numberContext).parse(0);
    }

    private static Number parseFromInteger(String content) {
        int index = 0;
        int length = content.length();
        int number = 0;
        int radix = 10;
        int sign = 1;
        if (content.charAt(index) == '+') {
            if (++index == length)
                return null;
        }
        if (content.charAt(index) == '-') {
            if (++index == length)
                return null;
            sign = -1;
        }
        if (content.startsWith("0x", index) || content.startsWith("0X", index)) {
            if ((index += 2) == length)
                return null;
            radix = 16;
        }
        int limit = sign == 1 ? -Integer.MAX_VALUE : Integer.MIN_VALUE;
        int limitBeforeMul = limit / radix;

        while (index < length) {
            char c = content.charAt(index++);
            if (c == '_' && index != length)
                continue;
            int digit = getDigit(radix, c);
            if (digit < 0)
                return null;

            if (isOverflow(digit, number, limit, limitBeforeMul, radix)) {
                return continueParseLong(sign, radix, number, digit, index, content);
            }

            number = number * radix - digit;
        }
        return -number * sign;
    }

    private static Number continueParseLong(int sign, int radix, long number, int digit, int index, String content) {
        number = number * radix - digit;
        int length = content.length();
        long limit = sign == 1 ? -Long.MAX_VALUE : Long.MIN_VALUE;
        long limitBeforeMul = limit / radix;
        while (index < length) {
            char c = content.charAt(index++);
            if (c == '_' && index != length)
                continue;
            digit = getDigit(radix, c);
            if (digit < 0)
                return null;

//            if (isOverflow(digit, number, limit, limitBeforeMul, radix)) {
//                return continueParseLong(sign, radix, number, digit, index, content);
//            }

            number = number * radix - digit;
        }

        return -number * sign;
    }

    private static boolean isOverflow(int digit, int number, int limit, int limitBeforeMul, int radix) {
        return number < limitBeforeMul || number * radix < limit + digit;
    }

    private static int getDigit(int radix, char c) {
        int value;
        if (radix > 10) {
            if (c > 'a')
                value = c - 'a' + 10;
            else if (c > 'A')
                value = c - 'A' + 10;
            else
                value = c - '0';
        } else
            value = c - '0';
        if (value >= 0 && value < radix)
            return value;
        return -1;
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
        Postfix<T> postfix = fetchPostfix(postfixes, numberContext);
        number = base;
        Number overflowNumber = overflowPostfix(postfix);
        if (overflowNumber != null)
            return overflowNumber;
        for (char c : numberContext.leftChars()) {
            Number doubleDecimal = numberContext.tryParseDoubleOrDecimal(number, c, postfix);
            if (doubleDecimal != null)
                return doubleDecimal;
            int digit = Character.digit(c, numberContext.getRadix());
            if (digit < 0)
                return null;
            if (isOverflow(digit))
                return overflowParser.get().parse(appendOverflowDigit(digit));
            appendDigit(digit);
            if (numberContext.isPostfixPosition(postfix))
                return postfix.transform(combineSignAndResult(), numberContext);
        }
        return combineSignAndResult();
    }

    protected Number overflowPostfix(Postfix<T> postfix) {
        if (numberContext.isPostfixPosition(postfix))
            return postfix.transform(combineSignAndResult(), numberContext);
        return null;
    }

    private Postfix<T> fetchPostfix(Postfix<T>[] postfixes, NumberContext numberContext) {
        for (Postfix<T> postfix : postfixes) {
            if (postfix.matches(numberContext))
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
                new Postfix.NonOverflowPostfix<BigInteger>("bi", v -> v),
                new Postfix.DecimalPostfix<BigInteger>("bd", BigDecimal::new, BigDecimal.class),
                new Postfix.DecimalPostfix<BigInteger>("f", Number::floatValue, Float.class),
                new Postfix.DecimalPostfix<BigInteger>("d", Number::doubleValue, Double.class)
        };
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
                new Postfix<Integer>("bi", Integer.MAX_VALUE, Integer.MIN_VALUE, BigInteger::valueOf),
                new Postfix.DecimalPostfix<Integer>("bd", BigDecimal::valueOf, BigDecimal.class),
                new Postfix.DecimalPostfix<Integer>("f", v -> (float) v, Float.class),
                new Postfix.DecimalPostfix<Integer>("d", v -> (double) v, Double.class)
        };
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

        @Override
        protected Number overflowPostfix(Postfix<Integer> postfix) {
            return null;
        }
    }

    static class LongParser extends Parser<Long, BigInteger> {
        private static final Postfix[] postfixes = new Postfix[]{
                new Postfix.OverflowPostfix<>("y"),
                new Postfix.OverflowPostfix<>("s"),
                new Postfix<>("l", Long.MAX_VALUE, Long.MIN_VALUE, Long::longValue),
                new Postfix<>("bi", Long.MAX_VALUE, Long.MIN_VALUE, BigInteger::valueOf),
                new Postfix.DecimalPostfix<Long>("bd", BigDecimal::valueOf, BigDecimal.class),
                new Postfix.DecimalPostfix<Long>("f", v -> (float) v, Float.class),
                new Postfix.DecimalPostfix<Long>("d", v -> (double) v, Double.class)};
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
    private final int length;

    public Postfix(String postfix, N maxValue, N minValue, Function<N, Number> convertor) {
        this.postfix = postfix.toLowerCase();
        postfixUpperCase = this.postfix.toUpperCase();
        length = postfix.length();
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.convertor = convertor;
    }

    public Number transform(N value, NumberContext numberContext) {
        if (isOverflow(value))
            throw new NumberOverflowException(numberContext.getContent());
        return convertor.apply(value);
    }

    protected boolean isOverflow(N value) {
        return value.compareTo(maxValue) > 0 || value.compareTo(minValue) < 0;
    }

    public boolean matches(NumberContext numberContext) {
        return numberContext.getContent().endsWith(postfix)
                || numberContext.getContent().endsWith(postfixUpperCase);
    }

    public int getPostfixLength() {
        return length;
    }

    public Number transformFloat(int sign, String stringBuilder, NumberContext numberContext) {
        throw new NumberOverflowException(numberContext.getContent());
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

    static class DecimalPostfix<N extends Number & Comparable<N>> extends NonOverflowPostfix<N> {
        private final Class<?> type;

        public DecimalPostfix(String postfix, Function<N, Number> convertor, Class<?> type) {
            super(postfix, convertor);
            this.type = type;
        }

        @Override
        public boolean matches(NumberContext numberContext) {
            return numberContext.getRadix() == 10 && super.matches(numberContext);
        }

        @Override
        public Number transformFloat(int sign, String content, NumberContext numberContext) {
            if (BigDecimal.class.equals(type))
                return sign == 1 ? new BigDecimal(content).negate() : new BigDecimal(content);
            else if (Double.class.equals(type)) {
                double d = sign == 1 ? -Double.parseDouble(content) : Double.parseDouble(content);
                if (Double.isInfinite(d))
                    throw new NumberOverflowException(numberContext.getContent());
                return d;
            } else if (Float.class.equals(type)) {
                float f = sign == 1 ? -Float.parseFloat(content) : Float.parseFloat(content);
                if (Float.isInfinite(f))
                    throw new NumberOverflowException(numberContext.getContent());
                return f;
            }
            return super.transformFloat(sign, content, numberContext);
        }
    }

    static class NonOverflowPostfix<N extends Number & Comparable<N>> extends Postfix<N> {

        public NonOverflowPostfix(String postfix, Function<N, Number> convertor) {
            super(postfix, null, null, convertor);
        }

        @Override
        protected boolean isOverflow(N value) {
            return false;
        }
    }
}

