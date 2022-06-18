package com.gmail.sneakdevs.diamondchestshop;

import com.gmail.sneakdevs.diamondchestshop.config.DiamondChestShopConfig;
import com.gmail.sneakdevs.diamondchestshop.sql.ChestshopDatabaseManager;
import com.gmail.sneakdevs.diamondchestshop.sql.ChestshopSQLiteDatabaseManager;
import com.gmail.sneakdevs.diamondeconomy.DiamondUtils;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

public class DiamondChestShop implements ModInitializer {
    public static final String MODID = "diamondschestshop";

    public static ChestshopDatabaseManager getDatabaseManager() {
        return new ChestshopSQLiteDatabaseManager();
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> ChestshopCommand.register(dispatcher));
        DiamondUtils.registerTable("CREATE TABLE IF NOT EXISTS chestshop (id integer PRIMARY KEY AUTOINCREMENT, item text NOT NULL, nbt text NOT NULL);");
        AutoConfig.register(DiamondChestShopConfig.class, JanksonConfigSerializer::new);
    }

    public static String signTextToReadable(String text) {
        return text.replaceAll("[\\D]", "").toLowerCase();
    }

    public static CompoundTag getNbtData(String text) throws CommandSyntaxException {
        CompoundTag nbt = NbtUtils.snbtToStructure(text);
        if (!text.contains("palette")) nbt.remove("palette");
        return nbt;
    }
}