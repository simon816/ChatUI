package com.simon816.minecraft.tabchat.pagination;

import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;

public class TabbedPaginationService implements PaginationService {

    private PaginationService service;

    public TabbedPaginationService(PaginationService service) {
        this.service = service;
    }

    @Override
    public PaginationList.Builder builder() {
        return new TabbedPaginationBuilder(this.service);
    }

}
