package com.perfectlan.mixin;

import com.perfectlan.PerfectLAN;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class ServerMixin {
    
    @Shadow
    public abstract net.minecraft.server.network.ServerConnectionListener getConnection();

    @Inject(method = "tickChildren", at = @At("HEAD"), cancellable = true)
    private void onTick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        if (PerfectLAN.isOopPaused) {
            // Skip the world simulation, entities, etc
            ci.cancel();
        }
    }
}