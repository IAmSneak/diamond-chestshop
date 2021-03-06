package com.gmail.sneakdevs.diamondchestshop.mixin;

import com.gmail.sneakdevs.diamondchestshop.interfaces.BaseContainerBlockEntityInterface;
import com.gmail.sneakdevs.diamondchestshop.interfaces.SignBlockEntityInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(method = "updateCustomBlockEntityTag(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"))
    private static void diamondchestshop_updateCustomBlockEntityTagMixin(Level world, Player player, BlockPos pos, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (world.getServer() != null && player != null) {
            if (world.getBlockEntity(pos) instanceof BaseContainerBlockEntity) {
                ((BaseContainerBlockEntityInterface) world.getBlockEntity(pos)).diamondchestshop_setOwner(player.getStringUUID());
            } else if (world.getBlockEntity(pos) instanceof SignBlockEntity) {
                ((SignBlockEntityInterface) world.getBlockEntity(pos)).diamondchestshop_setOwner(player.getStringUUID());
            }
        }
    }
}