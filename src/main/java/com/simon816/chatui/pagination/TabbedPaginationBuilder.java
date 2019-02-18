package com.simon816.chatui.pagination;

import com.simon816.chatui.lib.config.PlayerSettings;
import org.spongepowered.api.service.pagination.PaginationList;
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
        this.builder.linesPerPage(PlayerSettings.DEFAULT_BUFFER_HEIGHT_LINES - this.removedHeight);
    }

    @Override
    public PaginationList.Builder contents(Iterable<Text> contents) {
        this.builder.contents(contents);
        return this;
    }

    @Override
    public PaginationList.Builder contents(Text... contents) {
        this.builder.contents(contents);
        return this;
    }

    @Override
    public PaginationList.Builder title(Text title) {
        this.builder.title(title);
        return this;
    }

    @Override
    public PaginationList.Builder header(Text header) {
        this.builder.header(header);
        return this;
    }

    @Override
    public PaginationList.Builder footer(Text footer) {
        this.builder.footer(footer);
        return this;
    }

    @Override
    public PaginationList.Builder from(PaginationList value) {
        this.builder.from(value);
        return this;
    }

    @Override
    public PaginationList.Builder reset() {
        this.builder.reset();
        return this;
    }

    @Override
    public PaginationList.Builder padding(Text padding) {
         this.builder.padding(padding);
        return this;
    }

    @Override
    public PaginationList.Builder linesPerPage(int linesPerPage) {
        this.builder.linesPerPage(linesPerPage - this.removedHeight);
        return this;
    }

    @Override
    public PaginationList build() {
        return new TabbedPaginationList(this.builder.build(), this.service);
    }

}
