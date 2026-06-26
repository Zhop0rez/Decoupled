package com.perfectlan.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PerfectLANConfig {
    public static final Logger LOGGER = LoggerFactory.getLogger("PerfectLAN");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "perfectlan.json");

    public int serverRamMb = 4096; // Default 4GB
    public boolean checkAvailableRam = true;
    public boolean enablePerfectLAN = true;
    public boolean onlineMode = false;
    public int serverCpuCores = 0; // 0 = Auto Smart Split

    private static PerfectLANConfig instance;

    public static PerfectLANConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                instance = GSON.fromJson(reader, PerfectLANConfig.class);
            } catch (Exception e) {
                LOGGER.error("Failed to load Perfect LAN config", e);
                instance = new PerfectLANConfig();
            }
        } else {
            instance = new PerfectLANConfig();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save Perfect LAN config", e);
        }
    }
}
