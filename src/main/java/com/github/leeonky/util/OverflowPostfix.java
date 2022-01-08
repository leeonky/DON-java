package com.github.leeonky.util;

class OverflowPostfix<N extends Number & Comparable<N>> extends Postfix<N> {
    public OverflowPostfix(String postfix) {
        super(postfix, null, null, null);
    }

    @Override
    protected boolean isOverflow(N value) {
        return true;
    }
}
