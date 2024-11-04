package ong.aldenw.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import static ong.aldenw.SimpleGroups.UPDATE_COLOR_CACHE;

public record UpdateColorPayload(String playerUuid, int color) implements CustomPayload {
    public static final Id<UpdateColorPayload> ID = new Id<>(UPDATE_COLOR_CACHE);
    public static final PacketCodec<RegistryByteBuf, UpdateColorPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, UpdateColorPayload::playerUuid, PacketCodecs.INTEGER, UpdateColorPayload::color, UpdateColorPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}