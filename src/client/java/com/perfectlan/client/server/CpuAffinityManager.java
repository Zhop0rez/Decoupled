package com.perfectlan.client.server;

import com.perfectlan.client.config.PerfectLANConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CpuAffinityManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("PerfectLAN-Affinity");

    public static void applySmartAffinity(long serverPid) {
        try {
            int totalCores = Runtime.getRuntime().availableProcessors();
            long clientPid = ProcessHandle.current().pid();
            int configCores = PerfectLANConfig.get().serverCpuCores;

            if (totalCores <= 4 && configCores == 0) {
                LOGGER.info("CPU has {} logical cores. Smart affinity disabled (too few cores).", totalCores);
                return;
            }

            int serverCores;
            if (configCores > 0 && configCores < totalCores) {
                serverCores = configCores;
            } else {
                if (totalCores <= 8) {
                    serverCores = 2;
                } else if (totalCores <= 12) {
                    serverCores = 4;
                } else {
                    serverCores = 6;
                }
            }

            int clientCores = totalCores - serverCores;

            // Generate bitmasks
            long clientMask = (1L << clientCores) - 1; // First N cores
            long serverMask = ((1L << serverCores) - 1) << clientCores; // Remaining cores

            LOGGER.info("Applying CPU Affinity: Client gets first {} cores (mask {}), Server gets last {} cores (mask {}).", clientCores, clientMask, serverCores, serverMask);

            setAffinity(clientPid, clientMask);
            setAffinity(serverPid, serverMask);
        } catch (Exception e) {
            LOGGER.error("Failed to apply CPU affinity", e);
        }
    }

    public static void restoreClientAffinity() {
        try {
            int totalCores = Runtime.getRuntime().availableProcessors();
            long clientPid = ProcessHandle.current().pid();
            long allCoresMask = (1L << totalCores) - 1;
            LOGGER.info("Restoring Client CPU Affinity to all cores (mask {})", allCoresMask);
            setAffinity(clientPid, allCoresMask);
        } catch (Exception e) {
            LOGGER.error("Failed to restore CPU affinity", e);
        }
    }

    private static void setAffinity(long pid, long mask) throws Exception {
        Process p = Runtime.getRuntime().exec(new String[] {
            "powershell", "-Command",
            "$Process = Get-Process -Id " + pid + "; $Process.ProcessorAffinity = " + mask
        });
        p.waitFor();
    }
}
