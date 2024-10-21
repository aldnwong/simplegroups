package ong.aldenw.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import static ong.aldenw.SimpleGroups.DISPLAY_NAME_UPDATE;

public record UpdateDisplayNamePayload(String playerUuid, String prefix, int color) implements CustomPayload {
    public static final CustomPayload.Id<UpdateDisplayNamePayload> ID = new CustomPayload.Id<>(DISPLAY_NAME_UPDATE);
    public static final PacketCodec<RegistryByteBuf, UpdateDisplayNamePayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, UpdateDisplayNamePayload::playerUuid, PacketCodecs.STRING, UpdateDisplayNamePayload::prefix, PacketCodecs.INTEGER, UpdateDisplayNamePayload::color, UpdateDisplayNamePayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}