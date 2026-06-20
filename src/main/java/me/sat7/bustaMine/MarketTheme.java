package me.sat7.bustaMine;

import org.bukkit.Material;

import java.util.List;

public class MarketTheme {

    private static String currentTicker = "BTC";
    private static Material currentIcon = Material.GOLD_INGOT;

    public static void rollNext() {
        List<String> tickers = BustaMine.ccConfig.get().getStringList("Market.Tickers");
        if (!tickers.isEmpty()) {
            currentTicker = sanitizeTicker(tickers.get(BustaMine.generator.nextInt(tickers.size())));
        }

        List<String> icons = BustaMine.ccConfig.get().getStringList("Market.Icons");
        if (!icons.isEmpty()) {
            Material picked = null;
            int attempts = Math.min(icons.size(), 20);
            for (int i = 0; i < attempts; i++) {
                String name = icons.get(BustaMine.generator.nextInt(icons.size()));
                Material material = Material.matchMaterial(name);
                if (material != null && material.isItem()) {
                    picked = material;
                    break;
                }
            }
            if (picked != null) {
                currentIcon = picked;
            }
        }
    }

    public static String getCurrentTicker() {
        return currentTicker;
    }

    public static Material getCurrentIcon() {
        return currentIcon;
    }

    public static String getCurrentIconLabel() {
        String[] parts = currentIcon.name().toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.length() == 0) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }

    private static String sanitizeTicker(String ticker) {
        if (ticker == null || ticker.trim().length() == 0) {
            return "BTC";
        }
        ticker = ticker.trim().toUpperCase();
        if (ticker.startsWith("$")) {
            ticker = ticker.substring(1);
        }
        return ticker;
    }
}
