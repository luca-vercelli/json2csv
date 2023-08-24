package it.json2csv;

import java.text.NumberFormat;

/**
 * Immutable double wrapper endowed with a NumberFormat that influences the behaviour of toString()) method.
 */
public class NumberWrapper extends Number {
    Double d;
    NumberFormat nf;

    public NumberWrapper(Number x) {
        if (x == null) throw new NullPointerException("given null Number");
        d = x.doubleValue();
    }

    public NumberWrapper(Number x, NumberFormat nf) {
        this(x);
        this.nf = nf; // can be null
    }


	@Override
	public int intValue() {
		return d.intValue();
	}


	@Override
	public long longValue() {
		return d.longValue();
	}


	@Override
	public float floatValue() {
		return d.floatValue();
	}


	@Override
	public double doubleValue() {
		return d;
	}

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Number)) return false;
        return d.equals(((Number)other).doubleValue());
    }

    /**
     * Format this number according to internal NumberFormat
     */
    @Override
    public String toString() {
        return (nf == null) ? d.toString() : nf.format(this);
    }
}
