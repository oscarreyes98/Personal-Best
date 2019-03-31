package edu.personalbest.ucsd.personalbest;

import java.util.HashMap;
import java.util.Map;

// Package-private
class Utility {

    public static final String ARG_KEY_ACTION = "action";
    public static final String ARG_KEY_TARGET = "target";

    public static Map<String, String> makeArgs(String action, String target) {
        Map<String, String> args = new HashMap<>();
        args.put(ARG_KEY_ACTION, action);
        args.put(ARG_KEY_TARGET, target);
        return args;
    }
    public static String extractIDFromIDString(String str) {
        int idx = str.indexOf("@");
        if (idx == -1)
            return str;
        return str.substring(idx + 1);
    }
}
