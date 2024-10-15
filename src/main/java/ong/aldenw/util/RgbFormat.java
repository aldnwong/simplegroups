package ong.aldenw.util;

public class RgbFormat {
    public static int fromThree(int red, int green, int blue) {
        int rgb = red;
        rgb = (rgb << 8) + green;
        rgb = (rgb << 8) + blue;
        return rgb;
    }
}
