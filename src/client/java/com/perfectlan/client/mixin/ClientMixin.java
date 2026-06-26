package com.perfectlan.client.mixin;

import com.perfectlan.client.server.ServerProcessManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class ClientMixin {
    @Shadow public Gui gui;
    @Shadow private boolean pause;

    @Inject(method = "runTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;pause:Z", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void onRunTickPause(boolean renderLevel, CallbackInfo ci) {
        if (ServerProcessManager.isServerRunning() && this.gui.isPausing()) {
            this.pause = true;
        }
    }
}