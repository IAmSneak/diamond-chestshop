package com.gmail.sneakdevs.diamondchestshop;

import com.gmail.sneakdevs.diamondchestshop.config.DiamondChestShopConfig;
import com.gmail.sneakdevs.diamondeconomy.config.DiamondEconomyConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class ChestshopCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (DiamondEconomyConfig.getInstance().commandName != null && DiamondChestShopConfig.getInstance().useBaseCommand) {
            if (DiamondChestShopConfig.getInstance().chestshopCommandName != null) {
                dispatcher.register(
                        Commands.literal(DiamondEconomyConfig.getInstance().commandName)
                                .then(
                                        Commands.literal(DiamondChestShopConfig.getInstance().chestshopCommandName).executes(ChestshopCommand::chestshopCommand)
                                )
                );
            }
        } else {
            if (DiamondChestShopConfig.getInstance().chestshopCommandName != null) {
                dispatcher.register(
                        Commands.literal(DiamondChestShopConfig.getInstance().chestshopCommandName).executes(ChestshopCommand::chestshopCommand)
                );
            }
        }
    }

    private static int chestshopCommand(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(new TextComponent(
                "To create a chest shop: \n" +
                        "1) place a chest with a sign attached \n" +
                        "2) write \"buy\" or \"sell\" on the first line \n" +
                        "3) write the quantity of the item to be exchanged on the second line \n" +
                        "4) write the amount of currency to be exchanged on the third line \n" +
                        "5) hold the item to sell in your offhand and click the sign with a " + DiamondEconomyConfig.getCurrencyName(0) + "\n\n" +
                        "To buy/sell from a chestshop punch or right click the sign.\n" +
                        "If selling to the shop make sure the items are in your inventory and punch a shop's sign that contains \"buy\" on the first line.\n" +
                        "If buying from a shop make sure you have money in your account and right click a shop's sign that contains \"sell\" on the first line."
                ),
                false);
        return 1;
    }
}