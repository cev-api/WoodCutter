package dev.mimi.woodcutter.paper;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.StonecuttingRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class WoodCutterPaperPlugin extends JavaPlugin {
    private static final String RECIPE_RESOURCE = "/woodcutter/recipes.csv";

    @Override
    public void onEnable() {
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