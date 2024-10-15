package ong.aldenw.data;

import java.util.ArrayList;
import java.util.UUID;

public class GroupData {
    public String name;
    public String prefix;
    public int color;
    public boolean listed = false;
    public boolean open = false;
    public UUID leader;
    public ArrayList<UUID> players = new ArrayList<UUID>();
}
