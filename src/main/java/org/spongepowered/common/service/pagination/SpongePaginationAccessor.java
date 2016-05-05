package org.spongepowered.common.service.pagination;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.common.service.pagination.SpongePaginationService.SourcePaginations;

public class SpongePaginationAccessor {

    public static void replaceActivePagination(SpongePaginationService service, CommandSource oldSource, CommandSource newSource) {
        SourcePaginations state = service.getPaginationState(oldSource, false);
        ActivePagination active = state.get(state.getLastUuid());
        state.keys().remove(state.getLastUuid());
        service.getPaginationState(newSource, true).put(active);
    }

}
