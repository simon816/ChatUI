package com.simon816.chatui.impl;

import com.simon816.chatui.tabs.config.ConfigEditTab;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.type.GlobalConfig;

import java.io.IOException;
import java.lang.reflect.Field;

public class ImplementationConfig {

    private enum SupportedType {
        SPONGE("Sponge") {

            @Override
            protected ConfigEditTab.ActionHandler createHandler() {
                return new ConfigEditTab.ActionHandler() {

                    private void save(ConfigEditTab tab) {
                        SpongeConfig<GlobalConfig> conf = SpongeImpl.getGlobalConfig();
                        try {
                            Field loaderField = SpongeConfig.class.getDeclaredField("loader");
                            loaderField.setAccessible(true);
                            ((ConfigurationLoader<?>) loaderField.get(conf)).save(conf.getRootNode().getParent());
                            conf.reload();
                            tab.reloadRootNode(conf.getRootNode().getParent());
                        } catch (ReflectiveOperationException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNodeAdded(ConfigEditTab tab, ConfigurationNode node) {
                        save(tab);
                    }

                    @Override
                    public void onNodeChanged(ConfigEditTab tab, ConfigurationNode node) {
                        save(tab);
                    }

                    @Override
                    public void onNodeRemoved(ConfigEditTab tab, ConfigurationNode parent, Object key) {
                        save(tab);
                    }
                };
            }

            @Override
            public ConfigurationNode rootNode() {
                return SpongeImpl.getGlobalConfig().getRootNode();
            }

            @Override
            public Text getTitle() {
                return Text.of("Sponge Config");
            }
        };

        final String implId;
        final ConfigEditTab.ActionHandler handler;

        private SupportedType(String id) {
            this.implId = id;
            this.handler = createHandler();
        }

        protected abstract ConfigEditTab.ActionHandler createHandler();

        public abstract ConfigurationNode rootNode();

        public abstract Text getTitle();

    }

    static {
        SupportedType type = null;
        for (SupportedType testType : SupportedType.values()) {
            if (Sponge.getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getId().equals(testType.implId)
                    // also check CommonName for spongecommon-based
                    || testType.implId.equals(Sponge.getPlatform().asMap().get("CommonName"))) {
                type = testType;
                break;
            }
        }
        ImplementationConfig.type = type;
    }

    private static SupportedType type;

    public static boolean isSupported() {
        return type != null;
    }

    public static ConfigurationNode getRootNode() {
        return type != null ? type.rootNode() : null;
    }

    public static ConfigEditTab.ActionHandler getHandler() {
        return type != null ? type.handler : null;
    }

    public static Text getTitle() {
        return type != null ? type.getTitle() : null;
    }

}
