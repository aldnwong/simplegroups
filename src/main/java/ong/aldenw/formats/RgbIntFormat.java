package ong.aldenw.formats;

public class RgbIntFormat {
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
