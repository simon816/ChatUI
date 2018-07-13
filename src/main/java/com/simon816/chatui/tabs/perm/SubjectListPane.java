package com.simon816.chatui.tabs.perm;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

class SubjectListPane extends AnchorPaneUI {

    protected final List<Subject> subjectList = Lists.newArrayList();

    final PermissionsTab tab;

    TableScrollHelper tableScroll;
    boolean addMode;

    private SubjectCollection collection;

    public SubjectListPane(PermissionsTab tab) {
        this.tab = tab;
        addWithConstraint(createTable(), ANCHOR_TOP);
        addWithConstraint(createBottomBar(), ANCHOR_BOTTOM);
    }

    private UIComponent createBottomBar() {
        return new UIComponent() {

            @Override
            public void draw(PlayerContext ctx, LineFactory lineFactory) {
                lineFactory.appendNewLine(Text.builder()
                        .append(
                                Text.builder("[Return]").color(TextColors.BLUE).onClick(ExtraUtils.clickAction(() -> {
                                    SubjectListPane.this.tab.setRoot(SubjectListPane.this.tab.getDashboard());
                                }, SubjectListPane.this.tab)).build(),
                                Text.builder(SubjectListPane.this.addMode ? " [Cancel]" : " [Add]")
                                        .color(SubjectListPane.this.addMode ? TextColors.RED : TextColors.GREEN)
                                        .onClick(ExtraUtils.clickAction(() -> SubjectListPane.this.addMode = !SubjectListPane.this.addMode,
                                                SubjectListPane.this.tab))
                                        .build(),
                                Text.builder(" [Scroll Up]")
                                        .color(SubjectListPane.this.tableScroll.canScrollUp() ? TextColors.WHITE : TextColors.GRAY)
                                        .onClick(ExtraUtils.clickAction(SubjectListPane.this.tableScroll::scrollUp, SubjectListPane.this.tab))
                                        .build(),
                                Text.builder(" [Scroll Down]")
                                        .color(SubjectListPane.this.tableScroll.canScrollDown() ? TextColors.WHITE : TextColors.GRAY)
                                        .onClick(ExtraUtils.clickAction(SubjectListPane.this.tableScroll::scrollDown, SubjectListPane.this.tab))
                                        .build())
                        .build(), ctx);
            }
        };
    }

    private TableModel createTableModel() {
        return new TableModel() {

            @Override
            public int getRowCount() {
                return SubjectListPane.this.subjectList.size();
            }

            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public Object getCellValue(int row, int column) {
                if (column == 0) {
                    return SubjectListPane.this.subjectList.get(row);
                }
                return null;
            }
        };
    }

    Text renderSubject(Subject subject) {
        HoverAction<?> hover = null;
        if (this.collection.getIdentifier().equals(PermissionService.SUBJECTS_USER)) {
            try {
                UUID uuid = UUID.fromString(subject.getIdentifier());
                Optional<User> user = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(uuid);
                if (user.isPresent()) {
                    hover = TextActions.showText(Text.of(user.get().getName()));
                }
            } catch (Exception e) {
            }
        }
        return Text.builder(subject.getIdentifier())
                .onHover(hover)
                .onClick(ExtraUtils.clickAction(() -> {
                    this.tab.getSubjViewer().setActive(subject, true);
                    this.tab.setRoot(this.tab.getSubjViewer());
                }, SubjectListPane.this.tab)).build();
    }

    private TableUI createTable() {
        TableModel model = createTableModel();
        this.tableScroll = new TableScrollHelper(model);
        return new TableUI(model, new DefaultTableRenderer() {

            private final TableViewport viewport = SubjectListPane.this.tableScroll.createViewport();

            @Override
            public TableViewport getViewport() {
                return this.viewport;
            }

            @Override
            public TableColumnRenderer createColumnRenderer(int columnIndex) {
                if (columnIndex == 0) {
                    return new DefaultColumnRenderer() {

                        @Override
                        public List<Text> renderCell(Object value, int row, int tableWidth, PlayerContext ctx) {
                            return ctx.utils().splitLines(renderSubject((Subject) value), tableWidth);
                        }

                    };
                }
                if (columnIndex == 1) {
                    return new DefaultColumnRenderer() {

                        @Override
                        public List<Text> renderCell(Object value, int row, int tableWidth, PlayerContext ctx) {
                            return Collections.singletonList(Text.of(TextColors.RED, ExtraUtils.clickAction(view -> {
                                delete(view.getPlayer(), row);
                            }, SubjectListPane.this.tab), "X"));
                        }
                    };
                }
                return super.createColumnRenderer(columnIndex);
            }
        });
    }

    void delete(Player player, int index) {
        if (this.tab.actions().removeSubjectFromCollection(player, this.collection, this.subjectList.get(index))) {
            this.subjectList.remove(index);
        }
    }

    public void setSubjectList(SubjectCollection subjects) {
        this.subjectList.clear();
        Iterables.addAll(this.subjectList, subjects.getLoadedSubjects());
        this.collection = subjects;
        this.tableScroll.reset();
    }

    public void onTextEntered(PlayerChatView view, Text input) {
        if (!this.addMode) {
            return;
        }
        this.addMode = false;
        if (add(view.getPlayer(), input.toPlain())) {
            view.update();
        }
    }

    private boolean add(Player player, String identifier) {
        Subject subject = this.tab.actions().addSubjectToCollection(player, this.collection, identifier);
        if (subject != null) {
            this.subjectList.add(subject);
        }
        return subject != null;
    }

}
