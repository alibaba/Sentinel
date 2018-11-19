package com.alibaba.csp.sentinel.util.function;

/**
 * A tuple of 2 elements.
 */
public class Tuple2<R1, R2> {

    public final R1 r1;
    public final R2 r2;

    public Tuple2(R1 r1, R2 r2) {
        this.r1 = r1;
        this.r2 = r2;
    }

    /**
     * Factory method for creating a Tuple.
     *
     * @return new Tuple
     */
    public static <C1, C2> Tuple2<C1, C2> of(C1 c1, C2 c2) {
        return new Tuple2<C1, C2>(c1, c2);
    }

    /**
     * Swaps the element of this Tuple.
     *
     * @return a new Tuple where the first element is the second element of this Tuple and the second element is the first element of this Tuple.
     */
    public Tuple2<R2, R1> swap() {
        return new Tuple2<R2, R1>(this.r2, this.r1);
    }

    @Override
    public String toString() {
        return "Tuple2{" +
            "r1=" + r1 +
            ", r2=" + r2 +
            '}';
    }
}