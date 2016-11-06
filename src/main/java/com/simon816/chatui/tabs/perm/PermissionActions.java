package com.simon816.chatui.tabs.perm;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.util.Tristate;

import java.util.Set;

interface PermissionActions {

    Subject addSubjectToCollection(Player player, SubjectCollection collection, String subjIdentifier);

    boolean setPermission(Player player, Subject subject, Set<Context> contexts, String permission, Tristate value);

    void setDefault(Player player, Subject subject, Set<Context> contexts, Tristate value);

    boolean removeSubjectFromCollection(Player player, SubjectCollection collection, Subject subject);

    Tristate getDefault(Subject subject, Set<Context> contexts);

    boolean setOption(Player player, Subject subject, Set<Context> contexts, String key, String value);

    void addParent(Player player, Subject subject, Set<Context> contexts, String parentIdentifier);

    void removeParent(Player player, Subject subject, Set<Context> contexts, Subject parent);

}
