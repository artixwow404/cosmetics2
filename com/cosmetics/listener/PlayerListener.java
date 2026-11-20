package com.cosmetics.listener;

import com.cosmetics.CosmeticsPlugin;
import com.cosmetics.cosmetic.CosmeticCategory;
import com.cosmetics.cosmetic.KillSoundCosmetic;
import com.cosmetics.data.DatabaseManager;
import com.cosmetics.manager.PetManager;
import com.cosmetics.manager.SoundManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerListener implements Listener {

    private final CosmeticsPlugin plugin;

    public PlayerListener(CosmeticsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getDatabaseManager().loadPlayerData(player.getUniqueId()).thenAccept(data -> {
            if (!player.isOnline()) return;

            for (CosmeticCategory category : CosmeticCategory.values()) {
                String activeId = data.getActive(category.name().toLowerCase());
                if (activeId != null && !activeId.isEmpty()) {
                    var cosmetic = plugin.getCosmeticsManager().getCosmetic(category, activeId);
                    if (cosmetic != null) {
                        cosmetic.onEquip(player.getUniqueId());
                    }
                }
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        for (CosmeticCategory category : CosmeticCategory.values()) {
            String activeId = plugin.getDatabaseManager().getCachedData(uuid).getActive(category.name().toLowerCase());
            if (activeId != null && !activeId.isEmpty()) {
                var cosmetic = plugin.getCosmeticsManager().getCosmetic(category, activeId);
                if (cosmetic != null) {
                    cosmetic.onUnequip(uuid);
                }
            }
        }

        plugin.getPetManager().cleanup();
        plugin.getDatabaseManager().savePlayerData(uuid);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        DatabaseManager.PlayerCosmeticData data = plugin.getDatabaseManager().getCachedData(uuid);
        if (data == null) return;

        for (CosmeticCategory category : CosmeticCategory.values()) {
            String activeId = data.getActive(category.name().toLowerCase());
            if (activeId != null && !activeId.isEmpty()) {
                var cosmetic = plugin.getCosmeticsManager().getCosmetic(category, activeId);
                if (cosmetic != null) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> cosmetic.onEquip(uuid), 5L);
                }
            }
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        DatabaseManager.PlayerCosmeticData data = plugin.getDatabaseManager().getCachedData(uuid);
        if (data == null) return;

        for (CosmeticCategory category : CosmeticCategory.values()) {
            String activeId = data.getActive(category.name().toLowerCase());
            if (activeId != null && !activeId.isEmpty()) {
                var cosmetic = plugin.getCosmeticsManager().getCosmetic(category, activeId);
                if (cosmetic != null) {
                    cosmetic.onUnequip(uuid);
                    cosmetic.onEquip(uuid);
                }
            }
        }
    }
}