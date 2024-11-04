package ong.aldenw.formats;

public class RgbIntFormat {
    public static final int RGB_INT_MAX = 16777215;
    public static final int RGB_INT_MIN = 0;

    public static int fromThree(int r, int g, int b) {
        int color = (((r << 8) + g) << 8) + b;
        if (color > RGB_INT_MAX)
            color = RGB_INT_MAX;
        if (color < RGB_INT_MIN)
            color = RGB_INT_MIN;
        return color;
    }

    public static int boundInt(int color) {
        if (color > RGB_INT_MAX)
            color = RGB_INT_MAX;
        if (color < RGB_INT_MIN)
            color = RGB_INT_MIN;
        return color;
    }
}
