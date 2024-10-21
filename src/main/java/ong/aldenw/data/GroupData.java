package ong.aldenw.data;

import ong.aldenw.formats.RgbFormat;

import java.util.ArrayList;
import java.util.UUID;

public class GroupData {
    public String displayName = "";
    public String prefix = "";
    public int color = RgbFormat.fromThree(255, 255, 255);
    public boolean listed = false;
    public boolean open = false;
    public UUID leader;
    public ArrayList<UUID> players = new ArrayList<>();
}
