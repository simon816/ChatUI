package com.simon816.minecraft.tabchat.util;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ProxySource;

public class Utils {

    public static CommandSource getRealSource(CommandSource source) {
        while (source instanceof ProxySource) {
            source = ((ProxySource) source).getOriginalSource();
        }
        return source;
    }

}
