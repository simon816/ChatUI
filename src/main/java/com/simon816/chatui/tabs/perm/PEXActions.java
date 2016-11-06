package com.simon816.chatui.tabs.perm;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.simon816.chatui.util.ForwardingSource;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PEXActions implements PermissionActions {

    @Override
    public Subject addSubjectToCollection(Player player, SubjectCollection collection, String subjIdentifier) {
        CommandResult res = command(player, new StringBuilder("pex ")
                .append(collection.getIdentifier()).append(' ')
                .append(subjIdentifier).append(" info").toString());
        if (res.getSuccessCount().isPresent() && res.getSuccessCount().get() > 0) {
            return collection.get(subjIdentifier);
        }
        return null;
    }

    @Override
    public boolean removeSubjectFromCollection(Player player, SubjectCollection collection, Subject subject) {
        CommandResult res = command(player, new StringBuilder("pex ")
                .append(subject.getContainingCollection().getIdentifier()).append(' ')
                .append(subject.getIdentifier()).append(" delete")
                .toString());
        return res.getSuccessCount().isPresent() && res.getSuccessCount().get() > 0;
    }

    @Override
    public boolean setPermission(Player player, Subject subject, Set<Context> contexts, String permission, Tristate value) {
        CommandResult res = command(player, subjContext(subject, contexts)
                .append("permission ").append(permission).append(' ').append(tempAsStr(asInt(value))).toString());
        return res.getSuccessCount().isPresent() && res.getSuccessCount().get() > 0;
    }

    @Override
    public Tristate getDefault(Subject subject, Set<Context> contexts) {
        Tristate[] val = new Tristate[] {Tristate.UNDEFINED};
        command(new ForwardingSource(Sponge.getServer().getConsole()) {

            private final Pattern defRegex = Pattern.compile("\\s*Default permission:\\s*(\\-?\\d)");
            private boolean capturePermissions;
            private Set<Map.Entry<String, String>> capturedContext;

            @Override
            public void sendMessage(Text message) {
                String text = message.toPlain();
                if (!this.capturePermissions) {
                    if (text.startsWith("Permissions:")) {
                        this.capturePermissions = true;
                    }
                    return;
                }
                if (!text.startsWith("  ")) {
                    this.capturePermissions = false;
                    return;
                }
                if (text.equals("  Global:")) {
                    this.capturedContext = Collections.emptySet();
                    return;
                }
                if (text.startsWith("  [") && text.endsWith("]:")) {
                    this.capturedContext = Sets.newHashSet();
                    int start = text.indexOf('[') + 1;
                    int end = text.indexOf(']');
                    String[] split = text.substring(start, end).split(",\\s*");
                    for (String pair : split) {
                        String[] keyVal = pair.split("=", 2);
                        this.capturedContext.add(Maps.immutableEntry(keyVal[0], keyVal[1]));
                    }
                    return;
                }
                if (!contexts.equals(this.capturedContext)) {
                    return;
                }
                Matcher matcher = this.defRegex.matcher(text);
                if (matcher.matches()) {
                    int v = Integer.parseInt(matcher.group(1));
                    val[0] = v == 0 ? Tristate.UNDEFINED : v > 0 ? Tristate.TRUE : Tristate.FALSE;
                    this.capturePermissions = false;
                }
            }
        }, subjContext(subject, contexts).append("info").toString());
        return val[0];
    }

    @Override
    public void setDefault(Player player, Subject subject, Set<Context> contexts, Tristate value) {
        command(player, subjContext(subject, contexts).append("default ").append(tempAsStr(asInt(value))).toString());
    }

    @Override
    public void addParent(Player player, Subject subject, Set<Context> contexts, String parentIdentifier) {
        command(player, subjContext(subject, contexts)
                .append("parent add ").append(parentIdentifier).toString());
    }

    @Override
    public void removeParent(Player player, Subject subject, Set<Context> contexts, Subject parent) {
        command(player, subjContext(subject, contexts)
                .append("parent remove ").append(parent.getContainingCollection().getIdentifier()).append(' ')
                .append(parent.getIdentifier())
                .toString());
    }

    @Override
    public boolean setOption(Player player, Subject subject, Set<Context> contexts, String key, String value) {
        CommandResult res = command(player, subjContext(subject, contexts)
                .append("options ").append(key).append(' ')
                .append(value == null ? "" : value).toString());
        return res.getSuccessCount().isPresent() && res.getSuccessCount().get() > 0;
    }

    private static int asInt(Tristate tristate) {
        return tristate == Tristate.TRUE ? 1 : tristate == Tristate.FALSE ? -1 : 0;
    }

    // Because -1 doesn't want to work
    private static String tempAsStr(int v) {
        return v == -1 ? "false" : String.valueOf(v);
    }

    private static StringBuilder subjContext(Subject subject, Set<Context> contexts) {
        StringBuilder b = new StringBuilder("pex ");
        for (Context context : contexts) {
            b.append("--contexts ").append(context.getKey()).append('=').append(context.getValue()).append(' ');
        }
        return b.append(subject.getContainingCollection().getIdentifier())
                .append(' ')
                .append(subject.getIdentifier())
                .append(' ');
    }

    private CommandResult command(CommandSource src, String cmd) {
        return Sponge.getCommandManager().process(src, cmd);
    }

}
