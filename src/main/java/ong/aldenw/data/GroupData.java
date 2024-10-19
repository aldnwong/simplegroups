package ong.aldenw.data;

import ong.aldenw.util.RgbFormat;

import java.util.ArrayList;
import java.util.UUID;

public class GroupData {
    public String displayName = "";
    public String prefix = "[YUP] ";
    public int color = RgbFormat.fromThree(0, 255, 155);
    public boolean listed = false;
    public boolean open = false;
    public UUID leader;
    public ArrayList<UUID> players = new ArrayList<UUID>();
}
