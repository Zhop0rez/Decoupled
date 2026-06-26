package com.perfectlan.client.mixin;

import com.perfectlan.network.PausePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public class PauseScreenInitMixin {

    @Inject(method = "init()V", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (Minecraft.getInstance().player != null && ClientPlayNetworking.canSend(PausePayload.TYPE)) {
            ClientPlayNetworking.send(new PausePayload(true));
        }
    }
}
