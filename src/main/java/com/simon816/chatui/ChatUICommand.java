package com.simon816.chatui;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandNotFoundException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public class ChatUICommand implements CommandCallable {

    private final Text desc = Text.of("Internal TabbedChat commands");

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        if (!(source instanceof Player)) {
            throw new CommandException(Text.of("Source must be player"));
        }
        if (ChatUI.getView(source).handleCommand(arguments.split("\\s+"))) {
            return CommandResult.success();
        }
        throw new CommandNotFoundException(arguments);
    }

    // Forward-compatible API 5
    public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) throws CommandException {
        return getSuggestions(source, arguments);
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        return Collections.emptyList();
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return source instanceof Player;
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.of(this.desc);
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        return Optional.of(this.desc);
    }

    @Override
    public Text getUsage(CommandSource source) {
        return this.desc;
    }
}
