package com.simon816.minecraft.tabchat;

import com.google.common.collect.Lists;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.List;

public class MessagePipeline implements MessageHandler {

    private final List<MessageHandler> handlers = Lists.newArrayList();

    @Override
    public boolean process(Text message, CommandSource sender) {
        synchronized (this.handlers) {
            for (MessageHandler handler : this.handlers) {
                if (handler.process(message, sender)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addHandler(MessageHandler handler) {
        synchronized (this.handlers) {
            this.handlers.add(0, handler);
        }
    }

    public void removeHandler(MessageHandler handler) {
        synchronized (this.handlers) {
            this.handlers.remove(handler);
        }
    }

}
