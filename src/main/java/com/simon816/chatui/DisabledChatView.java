package com.simon816.chatui;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.UUID;

public class DisabledChatView implements PlayerChatView {

    private static final Text DISABLED_MESSAGE;
    static {
        Text.Builder builder = Text.builder("ChatUI is disabled, type ");
        builder.append(Text.builder("/chatui enable")
                .color(TextColors.GREEN)
                .onClick(TextActions.suggestCommand("/chatui enable"))
                .build(), Text.of(" to re-enable"));
        DISABLED_MESSAGE = builder.build();
    }
    private final UUID playerUuid;

    DisabledChatView(Player player) {
        this.playerUuid = player.getUniqueId();
        player.sendMessage(DISABLED_MESSAGE);
    }

    @Override
    public Player getPlayer() {
        return Sponge.getServer().getPlayer(this.playerUuid).get();
    }

    @Override
    public TopWindow getWindow() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void update() {
    }

    @Override
    public boolean handleIncoming(Text message) {
        return false;
    }

    @Override
    public Optional<Text> transformOutgoing(CommandSource sender, Text originalOutgoing, ChatType type) {
        return Optional.of(originalOutgoing);
    }

    @Override
    public boolean handleCommand(String[] args) {
        if (args[0].equals("enable")) {
            Config.playerConfig(this.playerUuid).getNode("enabled").setValue(true);
            Config.saveConfig();
            ChatUI.instance().initialize(this.getPlayer());
            return true;
        }
        return false;
    }

    @Override
    public void onRemove() {
    }

}
