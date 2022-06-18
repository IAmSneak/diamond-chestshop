package com.gmail.sneakdevs.diamondchestshop.mixin;

import com.gmail.sneakdevs.diamondchestshop.DiamondChestShop;
import com.gmail.sneakdevs.diamondchestshop.interfaces.BaseContainerBlockEntityInterface;
import com.gmail.sneakdevs.diamondchestshop.interfaces.ItemEntityInterface;
import com.gmail.sneakdevs.diamondchestshop.interfaces.SignBlockEntityInterface;
import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

@Mixin(value = SignBlock.class, priority = 999)
public abstract class SignBlockMixin extends BaseEntityBlock {
    
    protected SignBlockMixin(Properties properties) {
        super(properties);
    }

    //remove shop from chest
    @Override
    public void playerWillDestroy(Level world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        if (!world.isClientSide() && ((SignBlockEntityInterface) Objects.requireNonNull(world.getBlockEntity(pos))).diamondchestshop_getShop()) {
            BlockPos hangingPos = pos.offset(state.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepX(), state.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepY(), state.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepZ());
            List<ItemEntity> entities = world.getEntitiesOfClass(ItemEntity.class, new AABB(new BlockPos(hangingPos.getX() - 2, hangingPos.getY() - 2, hangingPos.getZ() - 2), new BlockPos(hangingPos.getX() + 2, hangingPos.getY() + 2, hangingPos.getZ() + 2)));
            while (entities.size() > 0) {
                if (entities.get(0).getUUID().equals(((SignBlockEntityInterface) world.getBlockEntity(pos)).diamondchestshop_getItemEntity())) {
                    entities.get(0).kill();
                }
                entities.remove(0);
            }
            if (world.getBlockEntity(hangingPos) instanceof BaseContainerBlockEntity shop) {
                ((BaseContainerBlockEntityInterface)shop).diamondchestshop_setShop(false);
                DiamondChestShop.getDatabaseManager().removeShop(((BaseContainerBlockEntityInterface)shop).diamondchestshop_getId());
                shop.setChanged();
            }
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void diamondchestshop_useMixin(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (!world.isClientSide()) {
            ItemStack itemStack = player.getItemInHand(hand);
            Item item = itemStack.getItem();
            BlockEntity be = world.getBlockEntity(pos);
            if (be == null) return;
            CompoundTag nbt = be.getUpdateTag();

            if (be.getUpdateTag().getBoolean("diamondchestshop_IsShop")) {
                //admin shops
                if (item.equals(Items.COMMAND_BLOCK)) {
                    ((SignBlockEntityInterface) be).diamondchestshop_setAdminShop(!((SignBlockEntityInterface) be).diamondchestshop_getAdminShop());
                    be.setChanged();
                    player.displayClientMessage(new TextComponent((((SignBlockEntityInterface) be).diamondchestshop_getAdminShop()) ? "Created admin shop" : "Removed admin shop"), true);
                    cir.setReturnValue(InteractionResult.PASS);
                    return;
                }
            }

            //create the chest shop
            if (item.equals(Registry.ITEM.get(ResourceLocation.tryParse(DiamondEconomyConfig.getInstance().currencies[0])))) {
                if (nbt.getBoolean("diamondchestshop_IsShop")) {
                    player.displayClientMessage(new TextComponent("This is already a shop"), true);
                    cir.setReturnValue(InteractionResult.PASS);
                    return;
                }

                BlockPos hangingPos = pos.offset(state.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepX(), state.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepY(), state.getValue(HorizontalDirectionalBlock.FACING).getOpposite().getStepZ());
                if (!(world.getBlockEntity(hangingPos) instanceof RandomizableContainerBlockEntity shop)) {
                    player.displayClientMessage(new TextComponent("Sign must be on a valid container"), true);
                    cir.setReturnValue(InteractionResult.PASS);
                    return;
                }

                if (!nbt.getString("diamondchestshop_ShopOwner").equals(player.getStringUUID()) || !((BaseContainerBlockEntityInterface)shop).diamondchestshop_getOwner().equals(player.getStringUUID())) {
                    player.displayClientMessage(new TextComponent("You must have placed down the sign and chest"), true);
                    cir.setReturnValue(InteractionResult.PASS);
                    return;
                }

                if (player.getOffhandItem().getItem().equals(Items.AIR)) {
                    player.displayClientMessage(new TextComponent("The sell item must be in your offhand"), true);
                    cir.setReturnValue(InteractionResult.PASS);
                    return;
                }

                if (((BaseContainerBlockEntityInterface)shop).diamondchestshop_getShop()) {
                    player.displayClientMessage(new TextComponent("That chest already is a shop"), true);
                    cir.setReturnValue(InteractionResult.PASS);
                    return;
                }

                if (!nbt.getString("Text1").toLowerCase().contains("sell") && !nbt.getString("Text1").toLowerCase().contains("buy")) {
                    player.displayClientMessage(new TextComponent("The first line must be either \"Buy\" or \"Sell\""), true);
                    cir.setReturnValue(InteractionResult.PASS);
                    return;
                }

                try {
                    int quantity = Integer.parseInt(DiamondChestShop.signTextToReadable(nbt.getString("Text2")));
                    int money = Integer.parseInt(DiamondChestShop.signTextToReadable(nbt.getString("Text3")));
                    if (quantity >= 1) {
                        if (money >= 0) {
                            ((SignBlockEntityInterface) be).diamondchestshop_setShop(true);
                            ((BaseContainerBlockEntityInterface)shop).diamondchestshop_setShop(true);
                            String itemStr = Registry.ITEM.getKey(player.getOffhandItem().getItem()).toString();
                            ((BaseContainerBlockEntityInterface)shop).diamondchestshop_setItem(itemStr);
                            try {
                                String tag = player.getOffhandItem().getTag().getAsString();
                                ((BaseContainerBlockEntityInterface)shop).diamondchestshop_setTag(tag);
                                ((BaseContainerBlockEntityInterface)shop).diamondchestshop_setId(DiamondChestShop.getDatabaseManager().addShop(itemStr, tag));
                            } catch (NullPointerException ignored) {
                                ((BaseContainerBlockEntityInterface)shop).diamondchestshop_setTag("{}");
                                ((BaseContainerBlockEntityInterface)shop).diamondchestshop_setId(DiamondChestShop.getDatabaseManager().addShop(itemStr, "{}"));
                            }
                            be.setChanged();
                            shop.setChanged();

                            ItemEntity itemEntity = EntityType.ITEM.create(world);
                            itemEntity.setItem(new ItemStack(player.getOffhandItem().getItem(), Math.min(quantity, player.getOffhandItem().getItem().getMaxStackSize())));
                            itemEntity.setUnlimitedLifetime();
                            itemEntity.setNeverPickUp();
                            itemEntity.setInvulnerable(true);
                            itemEntity.setNoGravity(true);
                            itemEntity.setPos(new Vec3(hangingPos.getX() + 0.5, hangingPos.getY() + 1.05, hangingPos.getZ() + 0.5));
                            ((ItemEntityInterface) itemEntity).diamondchestshop_setShop(true);
                            world.addFreshEntity(itemEntity);
                            ((SignBlockEntityInterface)be).diamondchestshop_setItemEntity(itemEntity.getUUID());
                            BlockState shopBlock = world.getBlockState(hangingPos);
                            if (shopBlock.getBlock().equals(Blocks.CHEST) && !ChestBlock.getBlockType(shopBlock).equals(DoubleBlockCombiner.BlockType.SINGLE)) {
                                Direction dir = ChestBlock.getConnectedDirection(shopBlock);
                                BlockEntity be2 = world.getBlockEntity(new BlockPos(shop.getBlockPos().getX() + dir.getStepX(), shop.getBlockPos().getY(), shop.getBlockPos().getZ() + dir.getStepZ()));
                                ((BaseContainerBlockEntityInterface)be2).diamondchestshop_setShop(true);
                            }
                            player.displayClientMessage(new TextComponent("Created shop with " + quantity + " " + player.getOffhandItem().getItem().getDescription().getString() + (((nbt.getString("Text1")).contains("sell")) ? " sold for $" : " bought for $") + money), true);
                        } else {
                            player.displayClientMessage(new TextComponent("Negative prices are not allowed"), true);
                        }
                    } else {
                        player.displayClientMessage(new TextComponent("Positive quantity required"), true);
                    }
                    cir.setReturnValue(InteractionResult.PASS);
                } catch (NumberFormatException ignored) {
                    player.displayClientMessage(new TextComponent("The second and third lines must be numbers (quantity then money)"), true);
                    cir.setReturnValue(InteractionResult.PASS);
                }
            }
        }
    }
}