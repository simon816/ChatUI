package com.simon816.chatui.tabs.perm;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.Tristate;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class FallbackPermActions implements PermissionActions {

    @Override
    public SubjectReference addSubjectToCollection(Player player, SubjectCollection collection, String subjIdentifier) {
        return collection.newSubjectReference(subjIdentifier);
    }

    @Override
    public boolean removeSubjectFromCollection(Player player, SubjectCollection collection, Subject subject) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public CompletableFuture<Boolean> setPermission(Player player, Subject subject, Set<Context> contexts, String permission, Tristate value) {
        Boolean perm = subject.getSubjectData().getPermissions(contexts).get(permission);
        if ((perm == null && value == Tristate.UNDEFINED)
                || (perm == Boolean.TRUE && value == Tristate.TRUE)
                || (perm == Boolean.FALSE && value == Tristate.FALSE)) {
            return CompletableFuture.completedFuture(true);
        }
        return subject.getSubjectData().setPermission(contexts, permission, value);
    }

    @Override
    public Tristate getDefault(Subject subject, Set<Context> contexts) {
        return Tristate.UNDEFINED;
    }

    @Override
    public void setDefault(Player player, Subject subject, Set<Context> contexts, Tristate value) {
    }

    @Override
    public void addParent(Player player, Subject subject, Set<Context> contexts, String parentIdentifier) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeParent(Player player, Subject subject, Set<Context> contexts, SubjectReference parent) {
        subject.getSubjectData().removeParent(contexts, parent);
    }

    @Override
    public CompletableFuture<Boolean> setOption(Player player, Subject subject, Set<Context> contexts, String key, String value) {
        return subject.getSubjectData().setOption(contexts, key, value);
    }
}
