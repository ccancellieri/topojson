package org.mibcxb.topojson;

import java.util.Arrays;

public class Transform {
    private double[] scale;
    private double[] translate;

    public double[] getScale() {
        return scale;
    }

    public void setScale(double[] scale) {
        this.scale = scale;
    }

    public double[] getTranslate() {
        return translate;
    }

    public void setTranslate(double[] translate) {
        this.translate = translate;
    }

    @Override
    public String toString() {
        return "McTransform [scale=" + Arrays.toString(scale) + ", translate="
                + Arrays.toString(translate) + "]";
    }
}
