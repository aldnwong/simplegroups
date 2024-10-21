package ong.aldenw.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import static ong.aldenw.SimpleGroups.INITIAL_SYNC;

public record SyncDisplayNamePayload(String playerUuid, String prefix, int color) implements CustomPayload {
    public static final Id<SyncDisplayNamePayload> ID = new Id<>(INITIAL_SYNC);
    public static final PacketCodec<RegistryByteBuf, SyncDisplayNamePayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, SyncDisplayNamePayload::playerUuid, PacketCodecs.STRING, SyncDisplayNamePayload::prefix, PacketCodecs.INTEGER, SyncDisplayNamePayload::color, SyncDisplayNamePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}