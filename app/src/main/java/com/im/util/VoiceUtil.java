package com.im.util;

/**
 * Created by Unicorn on 2016/4/15.
 */
public class VoiceUtil {
    private static VoiceUtil instance;

    static {
        instance = new VoiceUtil();
    }
    public static VoiceUtil getInstance() {
        if (instance != null) {
            return instance;
        }
        return null;
    }
}
