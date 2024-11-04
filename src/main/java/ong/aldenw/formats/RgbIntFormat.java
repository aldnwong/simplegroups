package ong.aldenw.formats;

public class RgbIntFormat {
    public static final int RGB_INT_MAX = 16777215;
    public static final int RGB_INT_MIN = 0;

    public static int fromThree(int r, int g, int b) {
        return boundInt((((r << 8) + g) << 8) + b);
    }

    public static int fromHex(String hexCode) {
        if (hexCode.charAt(0) == '#') hexCode = hexCode.substring(1);
        if (hexCode.length() != 6) throw new IllegalArgumentException("Invalid hex code");
        String rHex = hexCode.substring(0, 2);
        String gHex = hexCode.substring(2, 4);
        String bHex = hexCode.substring(4, 6);

        return boundInt((((Integer.parseInt(rHex, 16) << 8) + (Integer.parseInt(gHex, 16)) << 8) + (Integer.parseInt(bHex, 16))));
    }

    public static int boundInt(int color) {
        if (color > RGB_INT_MAX)
            color = RGB_INT_MAX;

        if (color < RGB_INT_MIN)
            color = RGB_INT_MIN;

        return color;
    }
}
