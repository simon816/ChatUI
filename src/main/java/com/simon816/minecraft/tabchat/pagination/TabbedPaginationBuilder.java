package com.simon816.minecraft.tabchat.pagination;

import com.simon816.minecraft.tabchat.PlayerChatView;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationList.Builder;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;

public class TabbedPaginationBuilder implements PaginationList.Builder {

    private final PaginationList.Builder builder;
    private final PaginationService service;
    private final int removedHeight;

    public TabbedPaginationBuilder(PaginationService service) {
        this.builder = service.builder();
        this.service = service;
        // This cannot be obtained without a Window object
        this.removedHeight = 2;
        this.builder.linesPerPage(PlayerChatView.DEFAULT_BUFFER_HEIGHT - this.removedHeight);
    }

    @Override
    public PaginationList.Builder contents(Iterable<Text> contents) {
        return this.builder.contents(contents);
    }

    @Override
    public PaginationList.Builder contents(Text... contents) {
        return this.builder.contents(contents);
    }

    @Override
    public PaginationList.Builder title(Text title) {
        return this.builder.title(title);
    }

    @Override
    public PaginationList.Builder header(Text header) {
        return this.builder.header(header);
    }

    @Override
    public PaginationList.Builder footer(Text footer) {
        return this.builder.footer(footer);
    }

    @Override
    public Builder from(PaginationList value) {
        return this.builder.from(value);
    }

    @Override
    public Builder reset() {
        return this.builder.reset();
    }

    @Override
    public Builder padding(Text padding) {
        return this.builder.padding(padding);
    }

    @Override
    public Builder linesPerPage(int linesPerPage) {
        return this.builder.linesPerPage(linesPerPage - this.removedHeight);
    }

    @Override
    public PaginationList build() {
        return new TabbedPaginationList(this.builder.build(), this.service);
    }

}
