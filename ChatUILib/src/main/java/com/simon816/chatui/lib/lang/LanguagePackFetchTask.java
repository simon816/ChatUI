package com.simon816.chatui.lib.lang;

import com.google.common.io.ByteStreams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class LanguagePackFetchTask implements Runnable {

    private final Logger logger;
    private final Path zipFile;
    private final String mcVersion;
    private final ExecutorService executor;

    public LanguagePackFetchTask(Logger logger, Path zipFile, String mcVersion) {
        this.logger = logger;
        this.zipFile = zipFile;
        this.mcVersion = mcVersion;
        this.executor = Executors.newFixedThreadPool(8);
    }

    @Override
    public void run() {
        this.logger.info("Fetching language pack");
        if (Files.exists(this.zipFile)) {
            this.logger.info("Language pack found, nothing to fetch");
            return;
        }
        URL versionManifestUrl;
        try {
            versionManifestUrl = new URL("https://launchermeta.mojang.com/mc/game/version_manifest.json");
        } catch (MalformedURLException e) {
            this.logger.error("Failed to create manifest URL", e);
            return;
        }
        JsonElement manifestJson;
        try (InputStream manifestJsonStream = versionManifestUrl.openConnection().getInputStream()) {
            manifestJson = new JsonParser().parse(new InputStreamReader(manifestJsonStream));
        } catch (IOException e) {
            this.logger.error("Failed to read manifest JSON", e);
            return;
        }
        JsonArray versionArray = manifestJson.getAsJsonObject().get("versions").getAsJsonArray();
        String versionUrlString = null;
        for (JsonElement versionElem : versionArray) {
            JsonObject version = versionElem.getAsJsonObject();
            if (version.get("id").getAsString().equals(this.mcVersion)) {
                versionUrlString = version.get("url").getAsString();
                break;
            }
        }
        if (versionUrlString == null) {
            this.logger.error("Could not find version %s from manifest", this.mcVersion);
            return;
        }
        URL versionJsonUrl;
        try {
            versionJsonUrl = new URL(versionUrlString);
        } catch (MalformedURLException e) {
            this.logger.error("Failed to create version URL", e);
            return;
        }
        JsonElement versionJson;
        try (InputStream versionJsonStream = versionJsonUrl.openConnection().getInputStream()) {
            versionJson = new JsonParser().parse(new InputStreamReader(versionJsonStream));
        } catch (IOException e) {
            this.logger.error("Failed to read version JSON", e);
            return;
        }
        URL assetJsonUrl;
        try {
            assetJsonUrl = new URL(versionJson.getAsJsonObject().get("assetIndex").getAsJsonObject().get("url").getAsString());
        } catch (MalformedURLException e) {
            this.logger.error("Failed to create asset JSON URL", e);
            return;
        }
        JsonElement assetJson;
        try (InputStream assetJsonStream = assetJsonUrl.openConnection().getInputStream()) {
            assetJson = new JsonParser().parse(new InputStreamReader(assetJsonStream));
        } catch (IOException e) {
            this.logger.error("Failed to read asset JSON", e);
            return;
        }
        JsonObject assetObjects = assetJson.getAsJsonObject().get("objects").getAsJsonObject();
        ZipOutputStream zip;
        try {
            zip = new ZipOutputStream(Files.newOutputStream(this.zipFile));
        } catch (IOException e) {
            this.logger.error("Failed to create language pack zip file", e);
            return;
        }
        for (Entry<String, JsonElement> entry : assetObjects.entrySet()) {
            if (entry.getKey().startsWith("minecraft/lang/") && entry.getKey().endsWith(".lang")) {
                String hash = entry.getValue().getAsJsonObject().get("hash").getAsString();
                String filename = Paths.get(entry.getKey()).getFileName().toString();
                this.executor.execute(() -> fetchAsset(hash, filename, zip));
            }
        }
        this.executor.shutdown();
        while (!this.executor.isTerminated()) {
            try {
                this.executor.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
            }
        }
        try {
            zip.close();
        } catch (IOException e) {
            return;
        }
    }

    private void fetchAsset(String hash, String filename, ZipOutputStream zip) {
        URL assetUrl;
        try {
            assetUrl = new URL("https://resources.download.minecraft.net/" + hash.substring(0, 2) + "/" + hash);
        } catch (MalformedURLException e) {
            this.logger.error("Failed to create asset file URL", e);
            return;
        }
        try (InputStream assetStream = assetUrl.openConnection().getInputStream()) {
            ZipEntry entry = new ZipEntry(filename);
            entry.setMethod(ZipEntry.DEFLATED);
            synchronized (zip) {
                zip.putNextEntry(entry);
                ByteStreams.copy(assetStream, zip);
            }
        } catch (IOException e) {
            this.logger.error("Failed to copy asset to zip", e);
            return;
        }
    }

}
