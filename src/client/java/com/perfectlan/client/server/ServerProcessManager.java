package com.perfectlan.client.server;

import com.perfectlan.client.config.PerfectLANConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.management.OperatingSystemMXBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ServerProcessManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("PerfectLAN-ServerManager");
    private static Process serverProcess;
    private static boolean isServerReady = false;

    public static boolean isServerRunning() {
        return serverProcess != null && serverProcess.isAlive();
    }

    public static boolean checkMemoryAvailability() {
        if (!PerfectLANConfig.get().checkAvailableRam) {
            return true;
        }

        try {
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            long freeMemoryBytes = osBean.getFreeMemorySize();
            long freeMemoryMb = freeMemoryBytes / (1024 * 1024);
            int requestedMb = PerfectLANConfig.get().serverRamMb;

            LOGGER.info("Available RAM: {} MB, Requested for server: {} MB", freeMemoryMb, requestedMb);

            // Require at least requested + 1024MB buffer for OS stability
            if (freeMemoryMb < (requestedMb + 1024)) {
                LOGGER.warn("Not enough free physical memory to safely launch background server!");
                return false;
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to check OS memory", e);
            return true; // Fallback to allowing if we can't check
        }
    }

    public static void startServer(Path worldDir, int port, Runnable onReady) {
        if (serverProcess != null && serverProcess.isAlive()) {
            LOGGER.warn("Background server is already running!");
            if (isServerReady) {
                onReady.run(); // Connect immediately if it's fully started
            }
            return;
        }
        isServerReady = false;

        if (!checkMemoryAvailability()) {
            // TODO: Display in-game warning to the user
            return;
        }

        try {
            Path gameDir = Paths.get(System.getProperty("user.dir"));
            File serverDir = gameDir.toFile();
            File eula = new File(serverDir, "eula.txt");
            try (java.io.FileWriter writer = new java.io.FileWriter(eula)) {
                writer.write("eula=true\n");
            }
            
            // Auto-configure server properties for optimal localhost performance and offline mode
            File serverProps = new File(serverDir, "server.properties");
            try (java.io.FileWriter writer = new java.io.FileWriter(serverProps)) {
                writer.write("online-mode=" + PerfectLANConfig.get().onlineMode + "\n");
                writer.write("sync-chunk-writes=false\n"); // Less disk I/O blocking
                writer.write("network-compression-threshold=-1\n"); // Disable compression for localhost to save CPU
                writer.write("server-port=" + port + "\n");
            }

            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
            String classPath = System.getProperty("java.class.path");

            List<String> command = new ArrayList<>();
            command.add(javaBin);
            command.add("-Xmx" + PerfectLANConfig.get().serverRamMb + "M");
            
            // Forward Fabric development properties so the mod loads in IDE
            for (String key : System.getProperties().stringPropertyNames()) {
                if (key.startsWith("fabric.") || key.startsWith("mixin.") || key.startsWith("log4j")) {
                    command.add("-D" + key + "=" + System.getProperty(key));
                }
            }

            // Add classpath
            command.add("-cp");
            command.add(classPath);
            
            command.add("-Dlog.file=logs/perfectlan_server.log");

            // Fabric knot server entrypoint
            command.add("net.fabricmc.loader.impl.launch.knot.KnotServer");

            // Server arguments
            command.add("--port");
            command.add(String.valueOf(port));
            command.add("--nogui");
            command.add("--universe");
            command.add(worldDir.getParent().toAbsolutePath().toString());
            command.add("--world");
            command.add(worldDir.getFileName().toString());

            LOGGER.info("Starting background server: {}", String.join(" ", command));

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.directory(gameDir.toFile());
            serverProcess = builder.start();

            // Apply CPU affinity right after starting
            CpuAffinityManager.applySmartAffinity(serverProcess.pid());

            // Read output in a background thread to prevent blocking
            Thread outputReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        LOGGER.info("[BG-SERVER] {}", line);
                        if (!isServerReady && line.contains("Done (")) {
                            isServerReady = true;
                            onReady.run();
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Error reading server output", e);
                }
            }, "OOP-Server-Output-Reader");
            outputReader.setDaemon(true);
            outputReader.start();

        } catch (Exception e) {
            LOGGER.error("Failed to start background server process", e);
        }
    }

    public static void stopServer() {
        final Process p = serverProcess;
        if (p != null && p.isAlive()) {
            LOGGER.info("Client disconnected. Background server should shut down automatically.");
            serverProcess = null;
            isServerReady = false;
            
            CpuAffinityManager.restoreClientAffinity();

            new Thread(() -> {
                try {
                    // Give the server time to save and exit gracefully
                    if (p.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                        LOGGER.info("Background server stopped gracefully.");
                        return;
                    }
                } catch (Exception e) {}

                if (p.isAlive()) {
                    LOGGER.warn("Background server did not stop gracefully in time. Forcing kill...");
                    p.destroyForcibly();
                }
            }, "PerfectLAN-Stopper").start();
        }
    }
}
