package dev.mimi.woodcutter.fabric;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;

public final class WoodCutterFabricMod implements ModInitializer {
    public static final String MOD_ID = "woodcutter";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final String DAMAGE_COMMAND = "stonecutterdamage";
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("woodcutter.properties");
    private static final String ENABLED_KEY = "stonecutter.damage.enabled";
    private static final String HEARTS_KEY = "stonecutter.damage.hearts";

    private static WoodCutterFabricMod instance;

    private final Map<Entity, GroundState> lastGroundStateByEntity = new WeakHashMap<>();
    private boolean stonecutterDamageEnabled = true;
    private double stonecutterDamageHearts = 1.0;

    @Override
    public void onInitialize() {
        instance = this;

        loadConfig();

        LOGGER.info("WoodCutter loaded. Stonecutter recipes are provided via generated datapack resources. Stonecutter damage is {} at {} heart(s).",
            stonecutterDamageEnabled ? "enabled" : "disabled",
            formatHearts(stonecutterDamageHearts));
    }

    public static WoodCutterFabricMod getInstance() {
        return instance;
    }

    public void handleEntityMoved(Entity entity) {
        if (entity.isRemoved()) {
            lastGroundStateByEntity.remove(entity);
            return;
        }

        if (!stonecutterDamageEnabled || stonecutterDamageHearts <= 0.0) {
            return;
        }

        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        Level level = living.level();
        if (level.isClientSide()) {
            return;
        }

        GroundSnapshot currentSnapshot = getGroundSnapshot(living, level);
        BlockPos currentGround = currentSnapshot.groundBlock();
        boolean currentOnStonecutter = currentSnapshot.onStonecutter();

        GroundState previous = lastGroundStateByEntity.get(living);
        if (previous != null && previous.isSameGround(level, currentGround)) {
            return;
        }

        if (currentOnStonecutter && (previous == null || !previous.onStonecutter())) {
            living.hurt(level.damageSources().generic(), (float) heartsToHealth(stonecutterDamageHearts));
        }

        lastGroundStateByEntity.put(living, new GroundState(level.dimension(), currentGround, currentOnStonecutter));
    }

    private GroundSnapshot getGroundSnapshot(LivingEntity living, Level level) {
        BlockPos feetBlock = living.blockPosition();
        if (level.getBlockState(feetBlock).is(Blocks.STONECUTTER)) {
            return new GroundSnapshot(feetBlock, true);
        }

        BlockPos belowFeet = feetBlock.below();
        return new GroundSnapshot(belowFeet, level.getBlockState(belowFeet).is(Blocks.STONECUTTER));
    }

    public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(DAMAGE_COMMAND)
            .requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
            .executes(context -> sendStatus(context.getSource()))
            .then(Commands.literal("status")
                .executes(context -> sendStatus(context.getSource())))
            .then(Commands.literal("on")
                .executes(context -> setEnabled(context.getSource(), true)))
            .then(Commands.literal("off")
                .executes(context -> setEnabled(context.getSource(), false)))
            .then(Commands.literal("set")
                .then(Commands.argument("hearts", DoubleArgumentType.doubleArg(0.0))
                    .executes(context -> setHearts(context.getSource(), DoubleArgumentType.getDouble(context, "hearts"))))));
    }

    private int sendStatus(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal(statusMessage()), false);
        return Command.SINGLE_SUCCESS;
    }

    private int setEnabled(CommandSourceStack source, boolean enabled) {
        stonecutterDamageEnabled = enabled;
        saveConfig();
        source.sendSuccess(() -> Component.literal(statusMessage()), false);
        return Command.SINGLE_SUCCESS;
    }

    private int setHearts(CommandSourceStack source, double hearts) {
        stonecutterDamageHearts = hearts;
        saveConfig();
        source.sendSuccess(() -> Component.literal(statusMessage()), false);
        return Command.SINGLE_SUCCESS;
    }

    private String statusMessage() {
        return "Stonecutter damage is " + (stonecutterDamageEnabled ? "enabled" : "disabled")
            + " at " + formatHearts(stonecutterDamageHearts) + " heart(s).";
    }

    private String formatHearts(double hearts) {
        String text = Double.toString(hearts);
        if (text.contains(".")) {
            text = text.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return text;
    }

    private double heartsToHealth(double hearts) {
        return hearts * 2.0;
    }

    private void loadConfig() {
        Properties properties = new Properties();

        if (Files.exists(CONFIG_PATH)) {
            try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
                properties.load(input);
            } catch (IOException e) {
                LOGGER.error("Failed to read config '{}'. Using defaults.", CONFIG_PATH, e);
            }
        }

        stonecutterDamageEnabled = Boolean.parseBoolean(properties.getProperty(ENABLED_KEY, "true"));

        String heartsValue = properties.getProperty(HEARTS_KEY, "1.0");
        try {
            stonecutterDamageHearts = Math.max(0.0, Double.parseDouble(heartsValue));
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid config value '{}' for {}. Using 1.0.", heartsValue, HEARTS_KEY);
            stonecutterDamageHearts = 1.0;
        }

        saveConfig();
    }

    private void saveConfig() {
        Properties properties = new Properties();
        properties.setProperty(ENABLED_KEY, Boolean.toString(stonecutterDamageEnabled));
        properties.setProperty(HEARTS_KEY, String.format(Locale.ROOT, "%s", stonecutterDamageHearts));

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
                properties.store(output, "WoodCutter Fabric Config");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save config '{}'.", CONFIG_PATH, e);
        }
    }

    private record GroundState(ResourceKey<Level> dimensionKey, BlockPos groundBlock, boolean onStonecutter) {
        private boolean isSameGround(Level currentLevel, BlockPos currentBlock) {
            return dimensionKey.equals(currentLevel.dimension()) && groundBlock.equals(currentBlock);
        }
    }

    private record GroundSnapshot(BlockPos groundBlock, boolean onStonecutter) {
    }
}
