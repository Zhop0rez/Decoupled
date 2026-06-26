package com.perfectlan.client.gui;

import com.perfectlan.client.config.PerfectLANConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class PerfectLANConfigScreen extends Screen {
    private final Screen parent;
    private final PerfectLANConfig config;

    public PerfectLANConfigScreen(Screen parent) {
        super(Component.literal("Perfect LAN Settings"));
        this.parent = parent;
        this.config = PerfectLANConfig.get();
    }

    @Override
    protected void init() {
        int y = this.height / 4;
        int center = this.width / 2;

        this.addRenderableWidget(Button.builder(
            Component.literal("Perfect LAN: " + (config.enablePerfectLAN ? "ON" : "OFF")),
            button -> {
                config.enablePerfectLAN = !config.enablePerfectLAN;
                button.setMessage(Component.literal("Perfect LAN: " + (config.enablePerfectLAN ? "ON" : "OFF")));
                config.save();
            }
        ).bounds(center - 100, y, 200, 20).build());

        y += 24;

        this.addRenderableWidget(Button.builder(
            Component.literal("Server RAM: " + config.serverRamMb + " MB"),
            button -> {
                config.serverRamMb += 512;
                if (config.serverRamMb > 4096) config.serverRamMb = 512;
                button.setMessage(Component.literal("Server RAM: " + config.serverRamMb + " MB"));
                config.save();
            }
        ).bounds(center - 100, y, 200, 20).build());

        y += 24;

        this.addRenderableWidget(Button.builder(
            Component.literal("Server CPU Cores: " + (config.serverCpuCores == 0 ? "Smart Auto" : config.serverCpuCores)),
            button -> {
                config.serverCpuCores += 1;
                if (config.serverCpuCores > Runtime.getRuntime().availableProcessors() - 1) {
                    config.serverCpuCores = 0;
                }
                button.setMessage(Component.literal("Server CPU Cores: " + (config.serverCpuCores == 0 ? "Smart Auto" : config.serverCpuCores)));
                config.save();
            }
        ).bounds(center - 100, y, 200, 20).build());

        y += 24;

        this.addRenderableWidget(Button.builder(
            Component.literal("Online Mode: " + (config.onlineMode ? "ON (Premium Only)" : "OFF (Cracked Allowed)")),
            button -> {
                config.onlineMode = !config.onlineMode;
                button.setMessage(Component.literal("Online Mode: " + (config.onlineMode ? "ON (Premium Only)" : "OFF (Cracked Allowed)")));
                config.save();
            }
        ).bounds(center - 100, y, 200, 20).build());

        y += 36;

        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.done"),
            button -> this.minecraft.setScreenAndShow(this.parent)
        ).bounds(center - 100, y, 200, 20).build());
    }
}
