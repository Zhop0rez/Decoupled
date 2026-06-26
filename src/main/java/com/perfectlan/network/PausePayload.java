package com.perfectlan.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PausePayload(boolean paused) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("perfectlan", "pause");
    public static final Type<PausePayload> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PausePayload> CODEC = CustomPacketPayload.codec(
        PausePayload::write, PausePayload::new
    );

    public PausePayload(RegistryFriendlyByteBuf buf) {
        this(buf.readBoolean());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(paused);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
