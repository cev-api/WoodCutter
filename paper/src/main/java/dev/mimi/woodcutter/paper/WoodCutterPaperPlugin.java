package dev.mimi.woodcutter.paper;

import io.papermc.paper.event.entity.EntityMoveEvent;
import io.papermc.paper.event.server.ServerResourcesReloadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.StonecuttingRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class WoodCutterPaperPlugin extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
    private static final String RECIPE_RESOURCE = "/woodcutter/recipes.csv";
    private static final String DAMAGE_ENABLED_PATH = "stonecutter-damage.enabled";
    private static final String DAMAGE_HEARTS_PATH = "stonecutter-damage.hearts";
    private static final String DAMAGE_COMMAND = "stonecutterdamage";

    private boolean stonecutterDamageEnabled;
    private double stonecutterDamageHealth;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadDamageSettings();
        Bukkit.getPluginManager().registerEvents(this, this);
        registerCommands();
        registerRecipes();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResourcesReloaded(ServerResourcesReloadedEvent event) {
        registerRecipes();
    }

    private void registerRecipes() {
        List<RecipeDef> defs = loadDefinitions();
        int added = 0;

        for (RecipeDef def : defs) {
            Material ingredient = materialFromId(def.ingredient());
            Material result = materialFromId(def.result());
            if (ingredient == null || result == null) {
                getLogger().warning("Skipping invalid recipe " + def.key() + " (ingredient=" + def.ingredient() + ", result=" + def.result() + ")");
                continue;
            }

            NamespacedKey key = new NamespacedKey(this, def.key());
            StonecuttingRecipe recipe = new StonecuttingRecipe(key, new ItemStack(result, def.count()), new RecipeChoice.MaterialChoice(ingredient));
            if (Bukkit.addRecipe(recipe)) {
                added++;
            }
        }

        getLogger().info("Registered " + added + " custom stonecutter recipes.");
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityMove(EntityMoveEvent event) {
        if (!stonecutterDamageEnabled || stonecutterDamageHealth <= 0.0) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }

        applyStonecutterContactDamage(living, event.getFrom(), event.getTo());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!stonecutterDamageEnabled || stonecutterDamageHealth <= 0.0) {
            return;
        }

        applyStonecutterContactDamage(event.getPlayer(), event.getFrom(), event.getTo());
    }

    private void applyStonecutterContactDamage(LivingEntity living, @Nullable Location from, @Nullable Location to) {
        if (isSameBlock(from, to)) {
            return;
        }

        if (!isStandingOnStonecutter(to) || isStandingOnStonecutter(from)) {
            return;
        }

        living.damage(stonecutterDamageHealth);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            sender.sendMessage(statusMessage());
            return true;
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);
        switch (subcommand) {
            case "on" -> {
                stonecutterDamageEnabled = true;
                saveDamageSettings();
                sender.sendMessage(statusMessage());
            }
            case "off" -> {
                stonecutterDamageEnabled = false;
                saveDamageSettings();
                sender.sendMessage(statusMessage());
            }
            case "set" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /" + label + " set <hearts>");
                    return true;
                }

                double hearts;
                try {
                    hearts = Double.parseDouble(args[1]);
                } catch (NumberFormatException ex) {
                    sender.sendMessage("Hearts must be a number (example: 1 or 0.5).");
                    return true;
                }

                if (hearts < 0.0) {
                    sender.sendMessage("Hearts cannot be negative.");
                    return true;
                }

                stonecutterDamageHealth = hearts * 2.0;
                saveDamageSettings();
                sender.sendMessage(statusMessage());
            }
            default -> sender.sendMessage("Usage: /" + label + " <status|on|off|set <hearts>>");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return filteredCompletions(args[0], List.of("status", "on", "off", "set"));
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return filteredCompletions(args[1], List.of("0.5", "1", "2"));
        }

        return List.of();
    }

    private List<String> filteredCompletions(String input, List<String> options) {
        String lowered = input.toLowerCase(Locale.ROOT);
        return options.stream()
            .filter(option -> option.startsWith(lowered))
            .toList();
    }

    private boolean isSameBlock(@Nullable Location first, @Nullable Location second) {
        if (first == null || second == null) {
            return false;
        }

        if (first.getWorld() != second.getWorld()) {
            return false;
        }

        return first.getBlockX() == second.getBlockX()
            && first.getBlockY() == second.getBlockY()
            && first.getBlockZ() == second.getBlockZ();
    }

    private boolean isStandingOnStonecutter(@Nullable Location location) {
        if (location == null) {
            return false;
        }

        Material feetBlock = location.getBlock().getType();
        if (feetBlock == Material.STONECUTTER) {
            return true;
        }

        return location.getBlock().getRelative(BlockFace.DOWN).getType() == Material.STONECUTTER;
    }

    private String statusMessage() {
        return "Stonecutter damage is " + (stonecutterDamageEnabled ? "enabled" : "disabled")
            + " at " + formatHearts(stonecutterDamageHealth / 2.0) + " heart(s).";
    }

    private String formatHearts(double hearts) {
        return BigDecimal.valueOf(hearts).stripTrailingZeros().toPlainString();
    }

    private void registerCommands() {
        PluginCommand command = getCommand(DAMAGE_COMMAND);
        if (command == null) {
            getLogger().warning("Missing command '/" + DAMAGE_COMMAND + "' in plugin.yml.");
            return;
        }

        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    private void loadDamageSettings() {
        stonecutterDamageEnabled = getConfig().getBoolean(DAMAGE_ENABLED_PATH, true);
        double hearts = getConfig().getDouble(DAMAGE_HEARTS_PATH, 1.0);
        if (hearts < 0.0) {
            hearts = 0.0;
        }
        stonecutterDamageHealth = hearts * 2.0;
    }

    private void saveDamageSettings() {
        getConfig().set(DAMAGE_ENABLED_PATH, stonecutterDamageEnabled);
        getConfig().set(DAMAGE_HEARTS_PATH, stonecutterDamageHealth / 2.0);
        saveConfig();
    }

    private List<RecipeDef> loadDefinitions() {
        InputStream stream = WoodCutterPaperPlugin.class.getResourceAsStream(RECIPE_RESOURCE);
        if (stream == null) {
            throw new IllegalStateException("Missing recipe resource: " + RECIPE_RESOURCE);
        }

        List<RecipeDef> defs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                String[] parts = trimmed.split(",");
                if (parts.length != 4) {
                    throw new IllegalStateException("Invalid CSV entry at line " + lineNumber + ": " + line);
                }

                defs.add(new RecipeDef(
                    parts[0].trim(),
                    parts[1].trim(),
                    parts[2].trim(),
                    Integer.parseInt(parts[3].trim())
                ));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load recipe definitions", e);
        }

        return defs;
    }

    private Material materialFromId(String id) {
        return Material.matchMaterial(id.toUpperCase(Locale.ROOT));
    }

    private record RecipeDef(String key, String ingredient, String result, int count) {
    }
}
