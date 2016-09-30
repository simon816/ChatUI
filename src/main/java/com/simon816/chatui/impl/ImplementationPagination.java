package com.simon816.chatui.impl;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.common.service.pagination.SpongePaginationAccessor;
import org.spongepowered.common.service.pagination.SpongePaginationService;

public class ImplementationPagination {

    public static void modify(PaginationService service, CommandSource oldSource, CommandSource newSource) {
        if (Sponge.getPlatform().getImplementation().getId().equals("sponge")) {
            if (service instanceof SpongePaginationService) {
                SpongePaginationAccessor.replaceActivePagination((SpongePaginationService) service, oldSource, newSource);
            }
        }
    }

}
