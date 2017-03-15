package com.simon816.chatui;

import com.simon816.chatui.tabs.NewTab;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

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
    private final Player player;
    private final PlayerList stubPlayerList;
    private final Window stubWindow;
    private final NewTab stubNewTab;

    DisabledChatView(Player player) {
        this.player = player;
        this.stubPlayerList = new PlayerList(player);
        this.stubWindow = new Window();
        this.stubNewTab = new NewTab();
        player.sendMessage(DISABLED_MESSAGE);
    }

    @Override
    public PlayerList getPlayerList() {
        return this.stubPlayerList;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public Window getWindow() {
        return this.stubWindow;
    }

    @Override
    public NewTab getNewTab() {
        return this.stubNewTab;
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
            Config.playerConfig(this.player.getUniqueId()).getNode("enabled").setValue(true);
            Config.saveConfig();
            ChatUI.instance().initialize(this.player);
            return true;
        }
        return false;
    }

}
