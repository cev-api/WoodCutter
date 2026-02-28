package dev.mimi.woodcutter.fabric;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WoodCutterFabricMod implements ModInitializer {
    public static final String MOD_ID = "woodcutter";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("WoodCutter loaded. Stonecutter recipes are provided via generated datapack resources.");
    }
}