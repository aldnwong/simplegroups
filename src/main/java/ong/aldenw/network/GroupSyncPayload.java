package ong.aldenw.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import static ong.aldenw.SimpleGroups.INITIAL_SYNC;

public record GroupSyncPayload(String groupName) implements CustomPayload {
    public static final Id<GroupSyncPayload> ID = new Id<>(INITIAL_SYNC);
    public static final PacketCodec<RegistryByteBuf, GroupSyncPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, GroupSyncPayload::groupName, GroupSyncPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}