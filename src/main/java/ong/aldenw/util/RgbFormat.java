package ong.aldenw.util;

public class RgbFormat {
    public static final int BLACK = 0;
    public static final int DARK_BLUE = 170;
    public static final int DARK_GREEN = 43520;
    public static final int DARK_AQUA = 43690;
    public static final int DARK_RED = 11141120;
    public static final int DARK_PURPLE = 11141290;
    public static final int GOLD = 16755200;
    public static final int GRAY = 11184810;
    public static final int DARK_GRAY = 5592405;
    public static final int BLUE = 5592575;
    public static final int GREEN = 5635925;
    public static final int AQUA = 5636095;
    public static final int RED = 16733525;
    public static final int LIGHT_PURPLE = 16733695;
    public static final int YELLOW = 16777045;
    public static final int WHITE = 16777215;


    public static int fromThree(int r, int g, int b) {
        return (((r << 8) + g) << 8) + b;
    }

    public static int fromHex(String hexCode) {
        if (hexCode.charAt(0) == '#') hexCode = hexCode.substring(1);
        if (hexCode.length() != 6) throw new IllegalArgumentException("Invalid hex code");
        String rHex = hexCode.substring(0, 2);
        String gHex = hexCode.substring(2, 4);
        String bHex = hexCode.substring(4, 6);
        return (((Integer.parseInt(rHex, 16) << 8) + (Integer.parseInt(gHex, 16)) << 8) + (Integer.parseInt(bHex, 16)));
    }
}
