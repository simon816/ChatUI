package com.simon816.minecraft.tabchat.pagination;

import com.simon816.minecraft.tabchat.PlayerChatView;
import com.simon816.minecraft.tabchat.tabs.PaginationTab;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ProxySource;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Tristate;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PaginationSourceWrapper implements ProxySource {

    public final PlayerChatView view;
    private final CommandSource actualSource;
    private final PaginationTab tab;

    public PaginationSourceWrapper(PlayerChatView view, CommandSource actualSource, Text title) {
        this.view = view;
        this.actualSource = actualSource;
        this.tab = new PaginationTab(this, title == null ? Text.of("Pagination") : title);
        view.getWindow().addTab(this.tab, false);
//        MessageHandler pipelineHandler = (message, sender) -> true;
//        view.getPipeline().addHandler(pipelineHandler);
        view.update();
    }

    @Override
    public void sendMessage(Text message) {
        this.tab.appendMessage(message);
    }

    @Override
    public void sendMessages(Iterable<Text> messages) {
        this.tab.setPage(messages);
    }

    @Override
    public void sendMessages(Text... messages) {
        this.tab.setPage(messages);
    }

    @Override
    public String getName() {
        return this.actualSource.getName();
    }

    @Override
    public MessageChannel getMessageChannel() {
        return this.actualSource.getMessageChannel();
    }

    @Override
    public void setMessageChannel(MessageChannel channel) {
        this.actualSource.setMessageChannel(channel);
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        return this.actualSource.getCommandSource();
    }

    @Override
    public SubjectCollection getContainingCollection() {
        return this.actualSource.getContainingCollection();
    }

    @Override
    public SubjectData getSubjectData() {
        return this.actualSource.getSubjectData();
    }

    @Override
    public SubjectData getTransientSubjectData() {
        return this.actualSource.getTransientSubjectData();
    }

    @Override
    public boolean hasPermission(Set<Context> contexts, String permission) {
        return this.actualSource.hasPermission(contexts, permission);
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.actualSource.hasPermission(permission);
    }

    @Override
    public Tristate getPermissionValue(Set<Context> contexts, String permission) {
        return this.actualSource.getPermissionValue(contexts, permission);
    }

    @Override
    public boolean isChildOf(Subject parent) {
        return this.actualSource.isChildOf(parent);
    }

    @Override
    public boolean isChildOf(Set<Context> contexts, Subject parent) {
        return this.actualSource.isChildOf(contexts, parent);
    }

    @Override
    public List<Subject> getParents() {
        return this.actualSource.getParents();
    }

    @Override
    public List<Subject> getParents(Set<Context> contexts) {
        return this.actualSource.getParents(contexts);
    }

    @Override
    public String getIdentifier() {
        return this.actualSource.getIdentifier();
    }

    @Override
    public Set<Context> getActiveContexts() {
        return this.actualSource.getActiveContexts();
    }

    @Override
    public CommandSource getOriginalSource() {
        return this.actualSource;
    }

}
