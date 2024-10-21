package ong.aldenw.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import java.util.UUID;

import static ong.aldenw.SimpleGroups.INITIAL_SYNC;

public record SyncPayload(String playerUuid, String groupName) implements CustomPayload {
    public static final Id<SyncPayload> ID = new Id<>(INITIAL_SYNC);
    public static final PacketCodec<RegistryByteBuf, SyncPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, SyncPayload::playerUuid, PacketCodecs.STRING, SyncPayload::groupName, SyncPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}