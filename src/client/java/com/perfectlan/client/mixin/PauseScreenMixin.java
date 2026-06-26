package com.perfectlan.client.mixin;

import com.perfectlan.network.PausePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class PauseScreenMixin {

    @Inject(method = "removed", at = @At("HEAD"))
    private void onRemoved(CallbackInfo ci) {
        if ((Object) this instanceof PauseScreen) {
            if (Minecraft.getInstance().player != null && ClientPlayNetworking.canSend(PausePayload.TYPE)) {
                ClientPlayNetworking.send(new PausePayload(false));
            }
        }
    }
}
