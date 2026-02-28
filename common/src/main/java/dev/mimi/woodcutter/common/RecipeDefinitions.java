package dev.mimi.woodcutter.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class RecipeDefinitions {
    private static final String RESOURCE_PATH = "/woodcutter/recipes.csv";

    private RecipeDefinitions() {
    }

    public static List<CuttingRecipeDef> loadAll() {
        InputStream stream = RecipeDefinitions.class.getResourceAsStream(RESOURCE_PATH);
        if (stream == null) {
            throw new IllegalStateException("Missing recipe resource: " + RESOURCE_PATH);
        }

        List<CuttingRecipeDef> definitions = new ArrayList<>();
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

                String key = parts[0].trim();
                String ingredient = parts[1].trim();
                String result = parts[2].trim();
                int count = Integer.parseInt(parts[3].trim());
                definitions.add(new CuttingRecipeDef(key, ingredient, result, count));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed reading recipe definitions", e);
        }

        return List.copyOf(definitions);
    }
}