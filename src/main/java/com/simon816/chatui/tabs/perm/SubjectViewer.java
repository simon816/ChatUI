package com.simon816.chatui.tabs.perm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.simon816.chatui.lib.PlayerChatView;
import com.simon816.chatui.lib.PlayerContext;
import com.simon816.chatui.ui.AnchorPaneUI;
import com.simon816.chatui.ui.LineFactory;
import com.simon816.chatui.ui.UIComponent;
import com.simon816.chatui.ui.table.DefaultColumnRenderer;
import com.simon816.chatui.ui.table.DefaultTableRenderer;
import com.simon816.chatui.ui.table.TableColumnRenderer;
import com.simon816.chatui.ui.table.TableModel;
import com.simon816.chatui.ui.table.TableScrollHelper;
import com.simon816.chatui.ui.table.TableUI;
import com.simon816.chatui.util.ExtraUtils;
import com.simon816.chatui.util.TextUtils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.Tristate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.SwingConstants;

class SubjectViewer extends AnchorPaneUI {

    final PermissionsTab tab;

    Subject activeSubj;
    Set<Context> activeContext = Collections.emptySet();

    private List<Entry<String, Boolean>> permList;

    TableScrollHelper tableScroll;

    boolean addMode;
    boolean addParentMode;

    public SubjectViewer(PermissionsTab tab) {
        this.tab = tab;
        addWithConstraint(createTopBar(), ANCHOR_TOP);
        addChildren(createTable(), createEmptyMessage());
        addWithConstraint(createBottomBar(), ANCHOR_BOTTOM);
    }

    public List<Entry<String, Boolean>> getPerms() {
        if (this.permList == null) {
            Map<String, Boolean> map = this.activeSubj.getSubjectData().getPermissions(this.activeContext);
            this.permList = Lists.newArrayList();
            for (Entry<String, Boolean> entry : map.entrySet()) {
                this.permList.add(entry);
            }
        }
        this.permList.sort((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()));
        return this.permList;
    }

    private TableModel createTableModel() {
        return new TableModel() {

            @Override
            public int getRowCount() {
                return getPerms().size();
            }

            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public Object getCellValue(int row, int column) {
                Entry<String, Boolean> entry = getPerms().get(row);
                if (column == 0) {
                    return entry.getKey();
                } else if (column == 1) {
                    return entry.getValue();
                }
                return null;
            }
        };
    }

    private TableUI createTable() {
        TableModel model = createTableModel();
        this.tableScroll = new TableScrollHelper(model);
        return new TableUI(model, new DefaultTableRenderer() {

            private final TableViewport viewport = SubjectViewer.this.tableScroll.createViewport();

            @Override
            public TableViewport getViewport() {
                return this.viewport;
            }

            @Override
            protected int getCellAlignment(int row, int column) {
                if (column == 2) {
                    return SwingConstants.CENTER;
                }
                return super.getCellAlignment(row, column);
            }

            @Override
            public TableColumnRenderer createColumnRenderer(int columnIndex) {
                if (columnIndex == 0) {
                    return new DefaultColumnRenderer() {

                        @Override
                        public List<Text> renderCell(Object value, int row, int tableWidth, boolean forceUnicode) {
                            return TextUtils.splitLines(Text.of(ExtraUtils.clickAction(view -> {
                                toggle(view.getPlayer(), row);
                            }, SubjectViewer.this.tab), value), tableWidth - getPrefWidth(), forceUnicode);
                        }
                    };
                }
                if (columnIndex == 1) {
                    return new DefaultColumnRenderer() {

                        @Override
                        public List<Text> renderCell(Object value, int row, int tableWidth, boolean forceUnicode) {
                            boolean v = (Boolean) value;
                            TextColor color = v ? TextColors.GREEN : TextColors.RED;
                            return TextUtils.splitLines(Text.of(color, ExtraUtils.clickAction(view -> {
                                toggle(view.getPlayer(), row);
                            }, SubjectViewer.this.tab), value), getPrefWidth(), forceUnicode);
                        }
                    };
                }
                if (columnIndex == 2) {
                    return new DefaultColumnRenderer() {

                        @Override
                        public List<Text> renderCell(Object value, int row, int tableWidth, boolean forceUnicode) {
                            return Collections.singletonList(Text.of(TextColors.RED, ExtraUtils.clickAction(view -> {
                                delete(view.getPlayer(), row);
                            }, SubjectViewer.this.tab), "X"));
                        }
                    };
                }
                return super.createColumnRenderer(columnIndex);
            }
        });
    }

    void toggle(Player player, int index) {
        Entry<String, Boolean> entry = getPerms().get(index);
        if (set(player, index, Tristate.fromBoolean(!entry.getValue()))) {
            this.permList = null;
        }

    }

    private boolean set(Player player, int index, Tristate value) {
        Entry<String, Boolean> entry = getPerms().get(index);
        return this.tab.actions().setPermission(player, this.activeSubj, this.activeContext, entry.getKey(), value);
    }

    void delete(Player player, int index) {
        if (set(player, index, Tristate.UNDEFINED)) {
            getPerms().remove(index);
        }
    }

    private UIComponent createEmptyMessage() {
        return new UIComponent() {

            @Override
            public void draw(PlayerContext ctx, LineFactory lineFactory) {
                if (getPerms().size() == 0) {
                    lineFactory.appendNewLine(Text.of("No permissions set"), ctx.forceUnicode);
                }
            }
        };
    }

    Text hyperlink(String text, Runnable r) {
        return hyperlink(text, r, null);
    }

    Text hyperlink(String text, Runnable r, String hoverText) {
        Text.Builder b = Text.builder(text).color(TextColors.BLUE).style(TextStyles.UNDERLINE)
                .onClick(ExtraUtils.clickAction(r, this.tab));
        if (hoverText != null) {
            b.onHover(TextActions.showText(Text.of(hoverText)));
        }
        return b.build();
    }

    private UIComponent createTopBar() {
        return new UIComponent() {

            @Override
            public void draw(PlayerContext ctx, LineFactory lineFactory) {
                lineFactory.appendNewLine(Text.of(TextStyles.BOLD, TextColors.RED, SubjectViewer.this.activeSubj.getIdentifier()), ctx.forceUnicode);
                Text.Builder builder = Text.builder("Parents: ");
                if (SubjectViewer.this.activeSubj.getParents(SubjectViewer.this.activeContext).isEmpty()) {
                    builder.append(Text.of("None"));
                }
                for (Subject parent : SubjectViewer.this.activeSubj.getParents(SubjectViewer.this.activeContext)) {
                    builder.append(hyperlink(parent.getIdentifier(), () -> setActive(parent, false)));
                    builder.append(Text.builder("[x]").color(TextColors.RED)
                            .onClick(ExtraUtils.clickAction((Consumer<PlayerChatView>) view -> removeParent(view.getPlayer(), parent),
                                    SubjectViewer.this.tab))
                            .build());
                    builder.append(Text.of(", "));
                }
                lineFactory.appendNewLine(builder.build(), ctx.forceUnicode);
                builder = Text.builder("Current Context: ");
                if (SubjectViewer.this.activeContext.isEmpty()) {
                    builder.append(Text.of("global"));
                }
                for (Context context : SubjectViewer.this.activeContext) {
                    builder.append(Text.of(context.getType() + "[" + context.getName() + "], "));
                }
                lineFactory.appendNewLine(builder.build(), ctx.forceUnicode);
            }
        };
    }

    private UIComponent createBottomBar() {
        PermissionsTab tab = this.tab;
        return new UIComponent() {

            @Override
            public void draw(PlayerContext ctx, LineFactory lineFactory) {
                lineFactory.appendNewLine(Text.builder()
                        .append(
                                simpleLink(SubjectViewer.this.addMode ? TextColors.RED : TextColors.GREEN,
                                        SubjectViewer.this.addMode ? "[Cancel]" : "[Add]", () -> {
                                            SubjectViewer.this.addMode = !SubjectViewer.this.addMode;
                                        }),
                                simpleLink(SubjectViewer.this.addParentMode ? TextColors.RED : TextColors.DARK_GREEN,
                                        SubjectViewer.this.addParentMode ? " [Cancel]" : " [Add Parent]", () -> {
                                            SubjectViewer.this.addParentMode = !SubjectViewer.this.addParentMode;
                                        }),
                                simpleLink(SubjectViewer.this.tableScroll.canScrollUp() ? TextColors.WHITE : TextColors.GRAY, " [Scroll Up]", () -> {
                                    SubjectViewer.this.tableScroll.scrollUp();
                                }),
                                simpleLink(SubjectViewer.this.tableScroll.canScrollDown() ? TextColors.WHITE : TextColors.GRAY, " [Scroll Down]",
                                        () -> {
                                            SubjectViewer.this.tableScroll.scrollDown();
                                        }))
                        .build(), ctx.forceUnicode);
                lineFactory.appendNewLine(Text.builder()
                        .append(
                                simpleLink(TextColors.BLUE, "[Return]", () -> {
                                    goBack();
                                }),
                                simpleLink(TextColors.GOLD, " [Options]", view -> {
                                    tab.getEntryDisplayer()
                                            .setData(SubjectViewer.this.activeSubj.getSubjectData()
                                                    .getOptions(SubjectViewer.this.activeContext),
                                                    (key, value) -> createOption(view.getPlayer(), key, value),
                                                    key -> removeOption(view.getPlayer(), key),
                                                    () -> tab.setRoot(SubjectViewer.this));

                                    tab.setRoot(tab.getEntryDisplayer());
                                }),
                                simpleLink(TextColors.DARK_AQUA, " Default: " + getDefault().toString(), view -> {
                                    cycleDefault(view.getPlayer());
                                }))
                        .build(), ctx.forceUnicode);
            }

            private Text simpleLink(TextColor color, String text, Runnable r) {
                return Text.builder(text).color(color).onClick(ExtraUtils.clickAction(r, tab)).build();
            }

            private Text simpleLink(TextColor color, String text, Consumer<PlayerChatView> c) {
                return Text.builder(text).color(color).onClick(ExtraUtils.clickAction(c, tab)).build();
            }

        };
    }

    Map.Entry<String, String> createOption(Player player, String key, String value) {
        if (this.tab.actions().setOption(player, this.activeSubj, this.activeContext, key, value)) {
            return Maps.immutableEntry(key, value);
        }
        return null;
    }

    boolean removeOption(Player player, String option) {
        return this.tab.actions().setOption(player, this.activeSubj, this.activeContext, option, null);
    }

    void cycleDefault(Player player) {
        Tristate[] vals = Tristate.values();
        Tristate val = vals[(getDefault().ordinal() + 1) % vals.length];
        this.tab.actions().setDefault(player, this.activeSubj, this.activeContext, val);
    }

    Tristate getDefault() {
        return this.tab.actions().getDefault(this.activeSubj, this.activeContext);
    }

    private final List<Subject> history = Lists.newArrayList();

    void goBack() {
        if (!this.history.isEmpty()) {
            this.activeSubj = null;
            setActive(this.history.remove(this.history.size() - 1), false);
        } else {
            this.tab.getSubjListPane().setSubjectList(SubjectViewer.this.activeSubj.getContainingCollection());
            this.tab.setRoot(this.tab.getSubjListPane());
        }
    }

    public void setActive(Subject subject, boolean clearHistory) {
        if (clearHistory) {
            this.history.clear();
        } else if (this.activeSubj != null) {
            this.history.add(this.activeSubj);
        }
        this.permList = null;
        this.activeSubj = subject;
        this.tableScroll.reset();
    }

    public void onTextEntered(PlayerChatView view, Text input) {
        if (this.addMode) {
            add(view.getPlayer(), input.toPlain());
            this.addMode = false;
        } else if (this.addParentMode) {
            addParent(view.getPlayer(), input.toPlain());
            this.addParentMode = false;
        } else {
            return;
        }
        view.update();
    }

    private void addParent(Player player, String parentIdentifier) {
        this.tab.actions().addParent(player, this.activeSubj, this.activeContext, parentIdentifier);
    }

    void removeParent(Player player, Subject parent) {
        this.tab.actions().removeParent(player, this.activeSubj, this.activeContext, parent);
    }

    private boolean add(Player player, String permission) {
        if (this.tab.actions().setPermission(player, this.activeSubj, this.activeContext, permission, Tristate.TRUE)) {
            this.permList = null;
            return true;
        }
        return false;
    }

}
