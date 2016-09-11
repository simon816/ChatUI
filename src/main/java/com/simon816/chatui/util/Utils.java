package com.simon816.chatui.util;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ProxySource;

public class Utils {

    public static CommandSource getRealSource(CommandSource source) {
        while (source instanceof ProxySource) {
            source = ((ProxySource) source).getOriginalSource();
        }
        return source;
    }

    public static int ensureMultiple(int val, int of) {
        while (val % of != 0) {
            val++;
        }
        return val;
    }
}
