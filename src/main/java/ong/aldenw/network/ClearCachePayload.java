package ong.aldenw.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import static ong.aldenw.SimpleGroups.CLEAR_CACHE;
import static ong.aldenw.SimpleGroups.UPDATE_COLOR_CACHE;

public record ClearCachePayload(String playerUuid) implements CustomPayload {
    public static final Id<ClearCachePayload> ID = new Id<>(CLEAR_CACHE);
    public static final PacketCodec<RegistryByteBuf, ClearCachePayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, ClearCachePayload::playerUuid, ClearCachePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}