package ong.aldenw.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import static ong.aldenw.SimpleGroups.UPDATE_PREFIX_CACHE;

public record UpdatePrefixPayload(String playerUuid, String prefix) implements CustomPayload {
    public static final CustomPayload.Id<UpdatePrefixPayload> ID = new CustomPayload.Id<>(UPDATE_PREFIX_CACHE);
    public static final PacketCodec<RegistryByteBuf, UpdatePrefixPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, UpdatePrefixPayload::playerUuid, PacketCodecs.STRING, UpdatePrefixPayload::prefix, UpdatePrefixPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}