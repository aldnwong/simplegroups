package ong.aldenw.formats;

import ong.aldenw.managers.DataManager;

import java.util.ArrayList;

public class GroupFormat {
    public static int MAX_GROUP_NAME_LENGTH = 30;
    public static int MIN_GROUP_NAME_LENGTH = 1;
    public static int MAX_PREFIX_NAME_LENGTH = 20;
    public static String ALLOWED_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static boolean isPrefixValid(String prefix, DataManager state) {
        if (prefix.length() > MAX_PREFIX_NAME_LENGTH || isPrefixInUse(prefix, state))
            return false;

        return true;
    }

    public static boolean isPrefixInUse(String prefix, DataManager state) {
        ArrayList<String> prefixCache = new ArrayList<>();
        state.groupList.forEach((name, groupData) -> prefixCache.add(groupData.getPrefix()));
        return prefixCache.contains(prefix);
    }

    public static boolean isNameValid(String groupName, DataManager state) {
        boolean allowedChars = true;
        for (char nameChar : groupName.toCharArray()) {
            if (ALLOWED_CHARACTERS.indexOf(nameChar) == -1) {
                allowedChars = false;
                break;
            }
        }
        return groupName.length() > MIN_GROUP_NAME_LENGTH && groupName.length() <= MAX_GROUP_NAME_LENGTH && !groupName.isEmpty() && !state.groupList.containsKey(groupName) && allowedChars;
    }
}
