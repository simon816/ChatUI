package com.simon816.chatui.lib.lang;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.simon816.chatui.lib.ChatUILib;
import com.simon816.chatui.lib.config.LibConfig;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.text.translation.locale.Locales;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LanguagePackManager {

    private static final Pattern NUMERIC_VARIABLE_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");

    private final Path langZipFile;
    private final Map<Locale, Integer> localeRefCount = Maps.newConcurrentMap();
    private final LoadingCache<Locale, Map<String, String>> translationCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<Locale, Map<String, String>>() {

                @Override
                public Map<String, String> load(Locale key) throws Exception {
                    return loadTranslations(key);
                }
            });

    public LanguagePackManager(Path confDir, Logger logger) {
        this.langZipFile = confDir.resolve("lang").resolve(getMcVersion() + "-languagepack.zip");
        try {
            Files.createDirectories(this.langZipFile.getParent());
        } catch (IOException e) {
            logger.error("Failed to create language pack directory {}", this.langZipFile.getParent(), e);
        }
    }

    public void fetch(Logger logger) {
        Sponge.getScheduler().createTaskBuilder()
                .async()
                .execute(new LanguagePackFetchTask(logger, this.langZipFile, getMcVersion()))
                .name("Language pack fetcher")
                .submit(ChatUILib.getInstance());
    }

    private String getMcVersion() {
        return Sponge.getPlatform().getMinecraftVersion().getName();
    }

    public Translation forTranslation(Translation translation) {
        if (!LibConfig.useLanguagePack()) {
            return translation;
        }
        return new PackTranslation(translation.getId());
    }

    public String translate(Locale locale, String id) {
        return this.translationCache.getUnchecked(locale).getOrDefault(id, id);
    }

    public void incrementLocale(Locale locale) {
        if (isDefault(locale)) {
            return;
        }
        this.localeRefCount.put(locale, this.localeRefCount.getOrDefault(locale, 0) + 1);
    }

    public void decrementLocale(Locale locale) {
        if (isDefault(locale)) {
            return;
        }
        Integer val = this.localeRefCount.get(locale);
        if (val == null || val <= 1) {
            this.localeRefCount.remove(locale);
            this.translationCache.invalidate(locale);
        } else {
            this.localeRefCount.put(locale, val - 1);
        }
    }

    public boolean isDefault(Locale locale) {
        return locale.equals(Locales.EN_US) || locale.equals(Locales.DEFAULT);
    }

    Map<String, String> loadTranslations(Locale locale) {
        if (!Files.exists(this.langZipFile)) {
            return Collections.emptyMap();
        }
        Map<String, String> translations = Maps.newHashMap();
        try (ZipFile zipFile = new ZipFile(this.langZipFile.toFile())) {
            ZipEntry entry = zipFile.getEntry(locale.toString().toLowerCase() + ".lang");
            if (entry == null) {
                return translations;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isEmpty() && line.charAt(0) != '#') {
                        int eqPos = line.indexOf('=');
                        if (eqPos != -1) {
                            String key = line.substring(0, eqPos);
                            String value = eqPos == line.length() - 1 ? "" : line.substring(eqPos + 1);
                            value = NUMERIC_VARIABLE_PATTERN.matcher(value).replaceAll("%$1s");
                            translations.put(key, value);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return translations;
    }

    private class PackTranslation implements Translation {

        private final String id;

        public PackTranslation(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public String get(Locale locale) {
            return translate(locale, this.id);
        }

        @Override
        public String get(Locale locale, Object... args) {
            // see net.minecraft.util.text.translation.LanguageMap
            String unformatted = get(locale);
            try {
                return String.format(unformatted, args);
            } catch (IllegalFormatException var5) {
                return "Format error: " + unformatted;
            }
        }

    }

}
