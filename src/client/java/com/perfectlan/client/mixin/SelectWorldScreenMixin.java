package com.perfectlan.client.mixin;

import com.perfectlan.client.gui.PerfectLANConfigScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {

    protected SelectWorldScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.addRenderableWidget(Button.builder(
            Component.literal("Perfect LAN"),
            button -> this.minecraft.setScreenAndShow(new PerfectLANConfigScreen(this))
        ).bounds(this.width - 100, 5, 90, 20).build());
    }
}
