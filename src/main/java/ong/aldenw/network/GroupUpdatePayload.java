package ong.aldenw.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import static ong.aldenw.SimpleGroups.GROUP_UPDATE;

public record GroupUpdatePayload(String groupId) implements CustomPayload {
    public static final CustomPayload.Id<GroupUpdatePayload> ID = new CustomPayload.Id<>(GROUP_UPDATE);
    public static final PacketCodec<RegistryByteBuf, GroupUpdatePayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, GroupUpdatePayload::groupId, GroupUpdatePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}