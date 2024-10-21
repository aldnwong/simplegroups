package ong.aldenw.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import static ong.aldenw.SimpleGroups.GROUP_UPDATE;

public record UpdatePayload(String playerUuid, String groupName) implements CustomPayload {
    public static final CustomPayload.Id<UpdatePayload> ID = new CustomPayload.Id<>(GROUP_UPDATE);
    public static final PacketCodec<RegistryByteBuf, UpdatePayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, UpdatePayload::playerUuid, PacketCodecs.STRING, UpdatePayload::groupName, UpdatePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}