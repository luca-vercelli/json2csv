package it.json2csv;

import java.text.NumberFormat;

/**
 * Immutable Number wrapper endowed with a NumberFormat that influences the
 * behaviour of toString()) method.
 */
public class NumberWrapper extends Number {

    /**
     * Wrapped Number
     */
    private Number x;

    /**
     * NumberFormat used to format this Number. Can be null.
     */
    private NumberFormat nf;

    /**
     * Constructor
     */
    public NumberWrapper(Number x, NumberFormat nf) {
        if (x == null)
            throw new NullPointerException("given null Number");
        this.x = x;
        this.nf = nf; // can be null
    }

    @Override
    public byte byteValue() {
        return x.byteValue();
    }

    @Override
    public int intValue() {
        return x.intValue();
    }

    @Override
    public long longValue() {
        return x.longValue();
    }

    @Override
    public float floatValue() {
        return x.floatValue();
    }

    @Override
    public double doubleValue() {
        return x.doubleValue();
    }

    @Override
    public short shortValue() {
        return x.shortValue();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Number))
            return false;
        return doubleValue() == ((Number) other).doubleValue();
    }

    /**
     * Format this number according to internal NumberFormat
     */
    @Override
    public String toString() {
        return nf != null ? nf.format(x) : x.toString();
    }
}
