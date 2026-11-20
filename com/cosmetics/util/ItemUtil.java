package com.cosmetics.util;

import com.cosmetics.CosmeticsPlugin;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;
import java.util.logging.Level;

public class ItemUtil {

    private static final String ITEMSADDER_PREFIX = "itemsadder:";
    private static final String ORAXEN_PREFIX = "oraxen:";
    private static volatile Boolean itemsAdderEnabled = null;
    private static volatile Boolean oraxenEnabled = null;

    public static boolean isItemsAdderAvailable() {
        if (itemsAdderEnabled == null) {
            synchronized (ItemUtil.class) {
                if (itemsAdderEnabled == null) {
                    itemsAdderEnabled = Bukkit.getPluginManager().isPluginEnabled("ItemsAdder");
                }
            }
        }
        return itemsAdderEnabled;
    }

    public static boolean isOraxenAvailable() {
        if (oraxenEnabled == null) {
            synchronized (ItemUtil.class) {
                if (oraxenEnabled == null) {
                    oraxenEnabled = Bukkit.getPluginManager().isPluginEnabled("Oraxen");
                }
            }
        }
        return oraxenEnabled;
    }

    public static ItemStack parseItem(String materialString, Material fallbackMaterial) {
        if (fallbackMaterial == null) {
            fallbackMaterial = Material.PAPER;
        }
        if (materialString == null || materialString.isBlank()) {
            logWarning("Empty material string, using fallback: " + fallbackMaterial.name());
            return new ItemStack(fallbackMaterial);
        }

        String trimmed = materialString.trim();

        if (trimmed.startsWith(ITEMSADDER_PREFIX)) {
            return parseItemsAdderItem(trimmed.substring(ITEMSADDER_PREFIX.length()), fallbackMaterial);
        }

        if (trimmed.startsWith(ORAXEN_PREFIX)) {
            return parseOraxenItem(trimmed.substring(ORAXEN_PREFIX.length()), fallbackMaterial);
        }

        if (trimmed.startsWith("base64:")) {
            return parseBase64Head(trimmed.substring("base64:".length()), fallbackMaterial);
        }

        return parseBukkitMaterial(trimmed, fallbackMaterial);
    }

    private static ItemStack parseItemsAdderItem(String namespacedId, Material fallbackMaterial) {
        if (!isItemsAdderAvailable()) {
            logWarning("ItemsAdder is not installed or disabled, cannot load item: " + namespacedId
                    + " - falling back to " + fallbackMaterial.name());
            return new ItemStack(fallbackMaterial);
        }

        try {
            Class<?> customStackClass = Class.forName("dev.lone.itemsadder.api.CustomStack");
            java.lang.reflect.Method getInstance = customStackClass.getMethod("getInstance", String.class);
            Object customStack = getInstance.invoke(null, namespacedId);

            if (customStack == null) {
                logWarning("ItemsAdder item not found: " + namespacedId + " - falling back to " + fallbackMaterial.name());
                return new ItemStack(fallbackMaterial);
            }

            java.lang.reflect.Method getItemStack = customStackClass.getMethod("getItemStack");
            ItemStack itemStack = (ItemStack) getItemStack.invoke(customStack);

            if (itemStack == null) {
                logWarning("ItemsAdder item returned null ItemStack: " + namespacedId + " - falling back to " + fallbackMaterial.name());
                return new ItemStack(fallbackMaterial);
            }

            return itemStack.clone();

        } catch (ClassNotFoundException e) {
            logWarning("ItemsAdder API classes not found (version mismatch?), cannot load: " + namespacedId);
            itemsAdderEnabled = false;
            return new ItemStack(fallbackMaterial);
        } catch (Exception e) {
            logWarning("Failed to load ItemsAdder item '" + namespacedId + "': " + e.getMessage()
                    + " - falling back to " + fallbackMaterial.name());
            return new ItemStack(fallbackMaterial);
        }
    }

    private static ItemStack parseOraxenItem(String itemId, Material fallbackMaterial) {
        if (!isOraxenAvailable()) {
            logWarning("Oraxen is not installed or disabled, cannot load item: " + itemId
                    + " - falling back to " + fallbackMaterial.name());
            return new ItemStack(fallbackMaterial);
        }

        try {
            Class<?> oraxenItemsClass = Class.forName("com.oraxen.api.OraxenItems");
            java.lang.reflect.Method getItemById = oraxenItemsClass.getMethod("getItemById", String.class);
            Object oraxenItem = getItemById.invoke(null, itemId);

            if (oraxenItem == null) {
                logWarning("Oraxen item not found: " + itemId + " - falling back to " + fallbackMaterial.name());
                return new ItemStack(fallbackMaterial);
            }

            ItemStack itemStack = null;
            Class<?> oraxenItemClass = oraxenItem.getClass();
            for (String methodName : new String[]{"getItemStack", "build", "parseItem"}) {
                try {
                    java.lang.reflect.Method method = oraxenItemClass.getMethod(methodName);
                    itemStack = (ItemStack) method.invoke(oraxenItem);
                    if (itemStack != null) break;
                } catch (NoSuchMethodException ignored) {}
            }

            if (itemStack == null) {
                logWarning("Oraxen item returned null ItemStack: " + itemId + " - falling back to " + fallbackMaterial.name());
                return new ItemStack(fallbackMaterial);
            }

            return itemStack.clone();

        } catch (ClassNotFoundException e) {
            logWarning("Oraxen API classes not found (version mismatch?), cannot load: " + itemId);
            oraxenEnabled = false;
            return new ItemStack(fallbackMaterial);
        } catch (Exception e) {
            logWarning("Failed to load Oraxen item '" + itemId + "': " + e.getMessage()
                    + " - falling back to " + fallbackMaterial.name());
            return new ItemStack(fallbackMaterial);
        }
    }

    private static ItemStack parseBukkitMaterial(String materialName, Material fallbackMaterial) {
        try {
            Material material = Material.valueOf(materialName.toUpperCase());
            return new ItemStack(material);
        } catch (IllegalArgumentException e) {
            logWarning("Invalid material name: " + materialName + " - falling back to " + fallbackMaterial.name());
            return new ItemStack(fallbackMaterial);
        }
    }

    private static ItemStack parseBase64Head(String base64Data, Material fallbackMaterial) {
        try {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = head.getItemMeta();

            if (meta instanceof SkullMeta skullMeta) {
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                profile.setProperty(new ProfileProperty("textures", base64Data));
                skullMeta.setPlayerProfile(profile);
                head.setItemMeta(skullMeta);
                return head;
            }

            logWarning("Failed to parse Base64 head texture - falling back to " + fallbackMaterial.name());
            return new ItemStack(fallbackMaterial);

        } catch (Exception e) {
            logWarning("Failed to create Base64 head: " + e.getMessage() + " - falling back to " + fallbackMaterial.name());
            return new ItemStack(fallbackMaterial);
        }
    }

    public static ItemStack parseItem(String materialString) {
        return parseItem(materialString, Material.PAPER);
    }

    public static ItemStack createItem(String materialString, String name, java.util.List<String> lore) {
        ItemStack item = parseItem(materialString);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) meta.setDisplayName(MessageUtil.color(name));
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore.stream().map(MessageUtil::color).toList());
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack createItem(String materialString, String name, java.util.List<String> lore, Integer customModelData) {
        ItemStack item = createItem(materialString, name, lore);
        if (customModelData != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setCustomModelData(customModelData);
                item.setItemMeta(meta);
            }
        }
        return item;
    }

    private static void logWarning(String message) {
        CosmeticsPlugin.getInstance().getLogger().log(Level.WARNING, "[ItemUtil] " + message);
    }
}
