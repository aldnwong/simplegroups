package ong.aldenw.data;

import ong.aldenw.formats.RgbIntFormat;

import java.util.ArrayList;
import java.util.UUID;

public class GroupData {
    public String name = "";
    public String prefix = "";
    public int color = RgbIntFormat.fromThree(255, 255, 255);
    public boolean listed = false;
    public boolean open = false;
    public UUID leader;
    public ArrayList<UUID> players = new ArrayList<>();
    public ArrayList<UUID> requests = new ArrayList<>();
}
