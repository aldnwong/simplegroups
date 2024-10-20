package ong.aldenw.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import ong.aldenw.GroupManager;
import ong.aldenw.data.GroupData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Objects;

@Mixin(PlayerEntity.class)
public abstract class DisplayNameMixin {
    @Shadow public abstract Text getName();

    @ModifyVariable(method = "getDisplayName", at = @At("STORE"), ordinal = 0)
    private MutableText injected(MutableText mutableText) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        GroupManager state = GroupManager.getServerState(Objects.requireNonNull(player.getServer()));
        String groupName = GroupManager.getPlayerState(player).groupName;
        GroupData groupState = state.groupList.get(groupName);
        if(groupName.isEmpty()) {
            return Text.empty().append(this.getName());
        }
        else if (groupState.prefix.isEmpty()) {
            return Text.empty().append(this.getName()).withColor(groupState.color);
        }
        else {
            return Text.empty().append(groupState.prefix).append(" ").append(this.getName()).withColor(groupState.color);
        }
    }
}
