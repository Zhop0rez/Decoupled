package com.perfectlan.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SimulationDistancePayload(int distance) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("perfectlan", "simulation_distance");
    public static final Type<SimulationDistancePayload> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SimulationDistancePayload> CODEC = CustomPacketPayload.codec(
        SimulationDistancePayload::write, SimulationDistancePayload::new
    );

    public SimulationDistancePayload(RegistryFriendlyByteBuf buf) {
        this(buf.readInt());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(distance);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
