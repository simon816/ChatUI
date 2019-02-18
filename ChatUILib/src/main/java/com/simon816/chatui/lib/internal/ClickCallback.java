package com.simon816.chatui.lib.internal;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simon816.chatui.lib.ChatUILib;
import com.simon816.chatui.lib.PlayerChatView;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ClickCallback {

    private static final Cache<UUID, Consumer<PlayerChatView>> callbackCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();

    public static CommandSpec createCommand() {
        return CommandSpec.builder()
                .arguments(GenericArguments.string(Text.of("uuid")))
                .executor((src, args) -> {
                    UUID uuid = UUID.fromString(args.<String>getOne("uuid").get());
                    Consumer<PlayerChatView> consumer = callbackCache.getIfPresent(uuid);
                    if (consumer == null) {
                        throw new CommandException(Text.of("Callback expired"));
                    }
                    consumer.accept(ChatUILib.getView(src));
                    return CommandResult.success();
                }).build();
    }

    public static String generateCommand(Consumer<PlayerChatView> handler) {
        UUID uuid = UUID.randomUUID();
        callbackCache.put(uuid, handler);
        return "/chatui exec " + uuid;
    }

}
