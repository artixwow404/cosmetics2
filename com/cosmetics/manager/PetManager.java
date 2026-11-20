package com.cosmetics.manager;

import com.cosmetics.CosmeticsPlugin;
import com.cosmetics.cosmetic.PetCosmetic;
import com.cosmetics.cosmetic.CompanionCosmetic;
import com.cosmetics.util.ItemUtil;
import com.cosmetics.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PetManager {

    private final CosmeticsPlugin plugin;
    private final Map<UUID, Entity> activePets = new ConcurrentHashMap<>();
    private final Map<UUID, Entity> activeCompanions = new ConcurrentHashMap<>();
    private final Map<UUID, Location> companionLocations = new ConcurrentHashMap<>();
    // For companions with multiple entities (like Gudodama's 7 spheres)
    private final Map<UUID, List<Entity>> multiEntityCompanions = new ConcurrentHashMap<>();

    public PetManager(CosmeticsPlugin plugin) {
        this.plugin = plugin;
    }

    public void spawnPet(Player player, PetCosmetic cosmetic) {
        despawnPet(player);

        EntityType entityType = cosmetic.getEntityType();
        Location spawnLoc = player.getLocation().add(1, 0, 1);

        Entity entity = spawnLoc.getWorld().spawnEntity(spawnLoc, entityType);
        if (entity instanceof Animals animal) {
            configurePet(animal, cosmetic, player);
        } else if (entity instanceof Tameable tameable) {
            tameable.setOwner(player);
            configurePet((Animals) entity, cosmetic, player);
        } else if (entity instanceof LivingEntity living) {
            configurePet(living, cosmetic, player);
        }

        activePets.put(player.getUniqueId(), entity);
    }

    private void configurePet(Animals animal, PetCosmetic cosmetic, Player owner) {
        animal.setCustomName(MessageUtil.color(cosmetic.getName()));
        animal.setCustomNameVisible(true);
        animal.setRemoveWhenFarAway(false);
        animal.setInvulnerable(true);
        animal.setCollidable(false);
        animal.setGravity(true);
        animal.setSilent(true);

        if (animal instanceof Tameable tameable) {
            tameable.setOwner(owner);
        }

        if (animal instanceof Ageable ageable) {
            ageable.setAdult();
        }

        String variant = cosmetic.getVariant();
        if (animal instanceof Cat cat && !variant.isEmpty()) {
            try {
                cat.setCatType(Cat.Type.valueOf(variant.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        } else if (animal instanceof Fox fox && !variant.isEmpty()) {
            try {
                fox.setFoxType(Fox.Type.valueOf(variant.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        } else if (animal instanceof Wolf wolf && !variant.isEmpty()) {
            try {
                wolf.setVariant(Wolf.Variant.valueOf(variant.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        } else if (animal instanceof Rabbit rabbit && !variant.isEmpty()) {
            try {
                rabbit.setRabbitType(Rabbit.Type.valueOf(variant.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        } else if (animal instanceof Panda panda && !variant.isEmpty()) {
            try {
                panda.setMainGene(org.bukkit.entity.Panda.Gene.valueOf(variant.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        } else if (animal instanceof Axolotl axolotl && !variant.isEmpty()) {
            try {
                axolotl.setVariant(Axolotl.Variant.valueOf(variant.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }

        double scale = cosmetic.getScale();
        if (scale != 1.0 && animal instanceof LivingEntity living) {
            living.setScale((float) scale);
        }
    }

    private void configurePet(LivingEntity entity, PetCosmetic cosmetic, Player owner) {
        entity.setCustomName(MessageUtil.color(cosmetic.getName()));
        entity.setCustomNameVisible(true);
        entity.setRemoveWhenFarAway(false);
        entity.setInvulnerable(true);
        entity.setCollidable(false);
        entity.setGravity(true);
        entity.setSilent(true);
        entity.setAI(false);

        double scale = cosmetic.getScale();
        if (scale != 1.0) {
            entity.setScale((float) scale);
        }
    }

    public void despawnPet(Player player) {
        Entity entity = activePets.remove(player.getUniqueId());
        if (entity != null && entity.isValid()) {
            entity.remove();
        }
    }

    public void spawnCompanion(Player player, CompanionCosmetic cosmetic) {
        despawnCompanion(player.getUniqueId());

        Location spawnLoc = player.getLocation().add(0, 1.5, 0);

        // Special handling for Gudodama companion
        if ("gudodama".equals(cosmetic.getId())) {
            spawnGudodamaCompanion(player, cosmetic, spawnLoc);
            return;
        }

        // Check if companion uses a custom ItemsAdder item (ItemDisplay)
        if (cosmetic.usesCustomItemDisplay()) {
            spawnItemDisplayCompanion(player, cosmetic, spawnLoc);
        } else {
            spawnEntityCompanion(player, cosmetic, spawnLoc);
        }
    }

    private void spawnEntityCompanion(Player player, CompanionCosmetic cosmetic, Location spawnLoc) {
        EntityType entityType = cosmetic.getEntityType();

        Entity entity = spawnLoc.getWorld().spawnEntity(spawnLoc, entityType);
        configureCompanion(entity, cosmetic);

        activeCompanions.put(player.getUniqueId(), entity);
        companionLocations.put(player.getUniqueId(), entity.getLocation());
    }

    private void spawnItemDisplayCompanion(Player player, CompanionCosmetic cosmetic, Location spawnLoc) {
        ItemStack displayItem = cosmetic.getDisplayItem();
        if (displayItem == null || displayItem.getType() == Material.AIR) {
            plugin.getLogger().warning("Companion " + cosmetic.getId() + " has invalid display item, falling back to entity");
            spawnEntityCompanion(player, cosmetic, spawnLoc);
            return;
        }

        try {
            // Spawn ITEM_DISPLAY entity (available in 1.20.4+) - use reflection for version safety
            Entity entity = spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.ITEM_DISPLAY);
            if (entity != null) {
                configureItemDisplay(entity, displayItem, cosmetic);
                activeCompanions.put(player.getUniqueId(), entity);
                companionLocations.put(player.getUniqueId(), entity.getLocation());
            } else {
                plugin.getLogger().warning("Failed to spawn ITEM_DISPLAY for companion " + cosmetic.getId());
                spawnEntityCompanion(player, cosmetic, spawnLoc);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn ItemDisplay for companion " + cosmetic.getId() + ": " + e.getMessage()
                    + " (requires Paper 1.20.4+). Falling back to entity.");
            spawnEntityCompanion(player, cosmetic, spawnLoc);
        }
    }

    /**
     * Spawns the exclusive Gudodama companion - 7 black BlockDisplay spheres with purple glow.
     */
    private void spawnGudodamaCompanion(Player player, CompanionCosmetic cosmetic, Location spawnLoc) {
        List<Entity> spheres = new CopyOnWriteArrayList<>();

        try {
            // Use reflection to access BlockDisplay class (1.20.4+)
            Class<?> blockDisplayClass = Class.forName("org.bukkit.entity.BlockDisplay");

            // Get properties from cosmetic config
            int sphereCount = cosmetic.getPropertyInt("gudodama-spheres", 7);
            double innerRadius = cosmetic.getPropertyDouble("gudodama-inner-radius", 0.5);
            double outerRadius = cosmetic.getPropertyDouble("gudodama-outer-radius", 1.5);
            String blockMaterialStr = cosmetic.getProperty("gudodama-block-material", "BLACK_CONCRETE");
            String glowColorStr = cosmetic.getProperty("gudodama-glow-color", "AA00FF");

            Material blockMaterial;
            try {
                blockMaterial = Material.valueOf(blockMaterialStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                blockMaterial = Material.BLACK_CONCRETE;
            }

            // Parse glow color
            org.bukkit.Color glowColor;
            try {
                String hex = glowColorStr.replace("#", "");
                if (hex.length() == 6) {
                    int r = Integer.parseInt(hex.substring(0, 2), 16);
                    int g = Integer.parseInt(hex.substring(2, 4), 16);
                    int b = Integer.parseInt(hex.substring(4, 6), 16);
                    glowColor = org.bukkit.Color.fromRGB(r, g, b);
                } else {
                    glowColor = org.bukkit.Color.fromRGB(0xAA, 0x00, 0xFF); // Purple
                }
            } catch (Exception e) {
                glowColor = org.bukkit.Color.fromRGB(0xAA, 0x00, 0xFF);
            }

            // Create BlockDisplay for each sphere
            for (int i = 0; i < sphereCount; i++) {
                try {
                    Entity entity = spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.BLOCK_DISPLAY);
                    if (entity != null && blockDisplayClass.isInstance(entity)) {
                        // Set the block state (black concrete)
                        org.bukkit.block.BlockState blockState = blockMaterial.createBlockData().getBlock().getState();
                        blockDisplayClass.getMethod("setBlockState", org.bukkit.block.data.BlockData.class)
                            .invoke(entity, blockMaterial.createBlockData());

                        // Configure BlockDisplay like ItemDisplay
                        configureBlockDisplay(entity, cosmetic, blockDisplayClass);

                        // Set glow color
                        blockDisplayClass.getMethod("setGlowColorOverride", org.bukkit.Color.class)
                            .invoke(entity, glowColor);
                        blockDisplayClass.getMethod("setGlowing", boolean.class).invoke(entity, true);

                        spheres.add(entity);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to spawn Gudodama sphere " + i + ": " + e.getMessage());
                }
            }

            if (!spheres.isEmpty()) {
                // Store the first sphere as the main companion for tracking
                activeCompanions.put(player.getUniqueId(), spheres.get(0));
                companionLocations.put(player.getUniqueId(), spawnLoc);
                // Store all spheres for multi-entity management
                multiEntityCompanions.put(player.getUniqueId(), spheres);

                // Apply common companion settings to all spheres
                for (Entity sphere : spheres) {
                    configureCompanion(sphere, cosmetic);
                }
            } else {
                plugin.getLogger().warning("Failed to spawn any Gudodama spheres, falling back to entity companion");
                spawnEntityCompanion(player, cosmetic, spawnLoc);
            }

        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("BlockDisplay class not found (server version < 1.20.4), Gudodama companion will not display correctly");
            spawnEntityCompanion(player, cosmetic, spawnLoc);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn Gudodama companion: " + e.getMessage());
            spawnEntityCompanion(player, cosmetic, spawnLoc);
        }
    }

    /**
     * Configures BlockDisplay entity with common settings.
     */
    private void configureBlockDisplay(Entity entity, CompanionCosmetic cosmetic, Class<?> blockDisplayClass) {
        try {
            // setItemDisplayTransform (same as ItemDisplay)
            Class<?> transformClass = Class.forName("org.bukkit.entity.ItemDisplay$ItemDisplayTransform");
            Object fixedTransform = transformClass.getField("FIXED").get(null);
            blockDisplayClass.getMethod("setItemDisplayTransform", transformClass).invoke(entity, fixedTransform);

            // setBrightness
            Class<?> brightnessClass = Class.forName("org.bukkit.entity.ItemDisplay$Brightness");
            Object brightness = brightnessClass.getConstructor(int.class, int.class).newInstance(15, 15);
            blockDisplayClass.getMethod("setBrightness", brightnessClass).invoke(entity, brightness);

            // setShadowRadius, setShadowStrength
            blockDisplayClass.getMethod("setShadowRadius", float.class).invoke(entity, 0f);
            blockDisplayClass.getMethod("setShadowStrength", float.class).invoke(entity, 0f);

            // setInterpolationDuration, setTeleportDuration
            blockDisplayClass.getMethod("setInterpolationDuration", int.class).invoke(entity, 0);
            blockDisplayClass.getMethod("setTeleportDuration", int.class).invoke(entity, 0);

            // setScale
            double scale = cosmetic.getScale();
            if (scale != 1.0) {
                blockDisplayClass.getMethod("setScale", float.class).invoke(entity, (float) scale);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to configure BlockDisplay: " + e.getMessage());
        }
    }

    private void configureItemDisplay(Entity entity, ItemStack displayItem, CompanionCosmetic cosmetic) {
        try {
            // Use reflection to access ItemDisplay methods (1.20.4+)
            Class<?> itemDisplayClass = Class.forName("org.bukkit.entity.ItemDisplay");
            if (itemDisplayClass.isInstance(entity)) {
                // setItemStack
                itemDisplayClass.getMethod("setItemStack", ItemStack.class).invoke(entity, displayItem.clone());

                // setItemDisplayTransform
                Class<?> transformClass = Class.forName("org.bukkit.entity.ItemDisplay$ItemDisplayTransform");
                Object fixedTransform = transformClass.getField("FIXED").get(null);
                itemDisplayClass.getMethod("setItemDisplayTransform", transformClass).invoke(entity, fixedTransform);

                // setBrightness
                Class<?> brightnessClass = Class.forName("org.bukkit.entity.ItemDisplay$Brightness");
                Object brightness = brightnessClass.getConstructor(int.class, int.class).newInstance(15, 15);
                itemDisplayClass.getMethod("setBrightness", brightnessClass).invoke(entity, brightness);

                // setShadowRadius, setShadowStrength
                itemDisplayClass.getMethod("setShadowRadius", float.class).invoke(entity, 0f);
                itemDisplayClass.getMethod("setShadowStrength", float.class).invoke(entity, 0f);

                // setInterpolationDuration, setTeleportDuration
                itemDisplayClass.getMethod("setInterpolationDuration", int.class).invoke(entity, 0);
                itemDisplayClass.getMethod("setTeleportDuration", int.class).invoke(entity, 0);

                // setScale
                double scale = cosmetic.getScale();
                if (scale != 1.0) {
                    itemDisplayClass.getMethod("setScale", float.class).invoke(entity, (float) scale);
                }
            }
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("ItemDisplay class not found (server version < 1.20.4), companion " + cosmetic.getId() + " will not display correctly");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to configure ItemDisplay for companion " + cosmetic.getId() + ": " + e.getMessage());
        }

        // Apply common companion settings
        configureCompanion(entity, cosmetic);
    }

    private void configureCompanion(Entity entity, CompanionCosmetic cosmetic) {
        entity.setCustomName(MessageUtil.color(cosmetic.getName()));
        entity.setCustomNameVisible(false);
        entity.setRemoveWhenFarAway(false);
        entity.setInvulnerable(true);
        entity.setCollidable(false);
        entity.setGravity(false);
        entity.setSilent(true);
        entity.setAI(false);
        entity.setInvisible(false);
        entity.setGlowing(true);

        double scale = cosmetic.getScale();
        if (entity instanceof LivingEntity living && scale != 1.0) {
            living.setScale((float) scale);
        }
    }

    public void despawnCompanion(UUID playerId) {
        // Remove main companion entity
        Entity entity = activeCompanions.remove(playerId);
        if (entity != null && entity.isValid()) {
            entity.remove();
        }
        // Remove all multi-entity companions (e.g., Gudodama spheres)
        List<Entity> multiEntities = multiEntityCompanions.remove(playerId);
        if (multiEntities != null) {
            for (Entity e : multiEntities) {
                if (e != null && e.isValid() && e != entity) { // Avoid double-removing the main entity
                    e.remove();
                }
            }
        }
        companionLocations.remove(playerId);
    }

    public void updateCompanionLocation(UUID playerId, Location location) {
        companionLocations.put(playerId, location);
        // Update main companion
        Entity entity = activeCompanions.get(playerId);
        if (entity != null && entity.isValid()) {
            entity.teleport(location);
        }
        // Update all multi-entity companions
        List<Entity> multiEntities = multiEntityCompanions.get(playerId);
        if (multiEntities != null) {
            for (Entity e : multiEntities) {
                if (e != null && e.isValid() && e != entity) {
                    e.teleport(location);
                }
            }
        }
    }

    public void updateAll() {
        for (Map.Entry<UUID, Entity> entry : activePets.entrySet()) {
            UUID playerId = entry.getKey();
            Entity pet = entry.getValue();
            Player player = Bukkit.getPlayer(playerId);

            if (player == null || !player.isOnline() || !pet.isValid()) {
                despawnPet(player);
                continue;
            }

            updatePetPosition(pet, player);
        }

        for (Map.Entry<UUID, Entity> entry : activeCompanions.entrySet()) {
            UUID playerId = entry.getKey();
            Entity companion = entry.getValue();
            Player player = Bukkit.getPlayer(playerId);

            if (player == null || !player.isOnline() || !companion.isValid()) {
                despawnCompanion(playerId);
                continue;
            }

            Location targetLoc = companionLocations.get(playerId);
            if (targetLoc != null) {
                companion.teleport(targetLoc);
                // Also teleport multi-entity companions
                List<Entity> multiEntities = multiEntityCompanions.get(playerId);
                if (multiEntities != null) {
                    for (Entity e : multiEntities) {
                        if (e != null && e.isValid() && e != companion) {
                            e.teleport(targetLoc);
                        }
                    }
                }
            }
        }
    }

    private void updatePetPosition(Entity pet, Player player) {
        Location playerLoc = player.getLocation();
        Location petLoc = pet.getLocation();

        double distance = petLoc.distance(playerLoc);
        if (distance > 5) {
            Location target = playerLoc.clone().add(
                (Math.random() - 0.5) * 2,
                0,
                (Math.random() - 0.5) * 2
            );
            Vector direction = target.toVector().subtract(petLoc.toVector()).normalize().multiply(0.5);
            pet.setVelocity(pet.getVelocity().add(direction));
        }

        if (pet instanceof LivingEntity living) {
            living.getPathfinder().moveTo(playerLoc, 1.0);
        }
    }

    public void cleanup() {
        for (Entity entity : activePets.values()) {
            if (entity.isValid()) entity.remove();
        }
        activePets.clear();

        for (Entity entity : activeCompanions.values()) {
            if (entity.isValid()) entity.remove();
        }
        activeCompanions.clear();

        // Clean up multi-entity companions
        for (List<Entity> entities : multiEntityCompanions.values()) {
            for (Entity entity : entities) {
                if (entity.isValid()) entity.remove();
            }
        }
        multiEntityCompanions.clear();

        companionLocations.clear();
    }

    public boolean hasActivePet(UUID playerId) {
        Entity entity = activePets.get(playerId);
        return entity != null && entity.isValid();
    }

    public boolean hasActiveCompanion(UUID playerId) {
        Entity entity = activeCompanions.get(playerId);
        return entity != null && entity.isValid();
    }
}