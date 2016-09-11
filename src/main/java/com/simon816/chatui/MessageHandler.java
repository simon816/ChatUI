package com.simon816.chatui;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

public interface MessageHandler {

    public boolean process(Text message, CommandSource sender);

}
