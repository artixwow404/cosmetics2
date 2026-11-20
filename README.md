# Cosmetics Plugin

A comprehensive cosmetics plugin for Minecraft servers (Purpur/Paper 1.21.8) featuring auras, trails, wings, hats, pets, kill sounds, and companions.

## Features

- **8 Auras** - Fire, Ice, Electric, Shadow, Magic, Star, Nature, Energy
- **8 Trails** - Fire, Ice, Star, Shadow, Magic, Emerald, Electric, Smoke
- **3 Wings** - Angel, Demon, Energy
- **27 Hats** - Crown, Top Hat, Cowboy, Santa, Witch, King/Emperor Crowns, Demon Horns, Animal Ears, Hood, Hard Hat, Pirate, Wizard, Gold/Ice/Fire Crowns, Bunny/Devil Horns, Halo, Flower Crown, Viking, Samurai, Astronaut, Crown of Thorns, Party Hat
- **12 Pets** - Cat, Dog, Fox, Wolf, Panda, Rabbit, Slime, Mini Dragon, Bee, Axolotl, Skeleton Pet, Magic Spirit
- **15 Kill Sounds** - Explosion, Anvil, Level Up, Enderman, Wither, Dragon Death, Magic, Glass, TNT, Thunder, Bell, Note Block, Totem, Phantom, Custom
- **8 Companions** - Mini Dragon, Fairy, Spirit, Glow Orb, Mini Demon, Star Satellite, Phoenix, Will-o'-Wisp

## Economy System

Supports two currencies:
- **Coins** - Via Vault/EssentialsX Economy
- **DonateCoins** - Via PlayerPoints

Each cosmetic can be configured with its own currency and price.

## Requirements

- Java 21
- Purpur/Paper 1.21.8
- Vault (for Coins economy)
- EssentialsX (for Coins economy)
- PlayerPoints (for DonateCoins economy)
- Oraxen (optional, for custom models)

## Installation

1. Place `Cosmetics.jar` in your server's `plugins` folder
2. Ensure Vault, EssentialsX, and PlayerPoints are installed
3. Start the server - configuration files will be generated automatically
4. Configure prices in `plugins/Cosmetics/prices.yml`
5. Restart or use `/cosmetics reload`

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/cosmetics` | Open cosmetics menu | `cosmetics.use` |
| `/cosmetics menu <player>` | Open menu for player | `cosmetics.admin` |
| `/cosmetics give <player> <cosmetic>` | Give cosmetic to player | `cosmetics.give` |
| `/cosmetics remove <player> <cosmetic>` | Remove cosmetic from player | `cosmetics.remove` |
| `/cosmetics reload` | Reload configuration | `cosmetics.reload` |
| `/cosmetics clear <player>` | Clear all cosmetics from player | `cosmetics.clear` |
| `/cosmetics unlockall <player>` | Unlock all cosmetics for player | `cosmetics.unlockall` |

## Permissions

- `cosmetics.use` - Use cosmetics menu (default: true)
- `cosmetics.admin` - Admin commands (default: op)
- `cosmetics.give` - Give cosmetics (default: op)
- `cosmetics.remove` - Remove cosmetics (default: op)
- `cosmetics.reload` - Reload config (default: op)
- `cosmetics.clear` - Clear player cosmetics (default: op)
- `cosmetics.unlockall` - Unlock all for player (default: op)

## Configuration Files

- `config.yml` - General settings, database, GUI layout
- `cosmetics.yml` - Cosmetic definitions (particles, models, properties)
- `prices.yml` - Prices and currencies for each cosmetic
- `gui.yml` - GUI menus, items, pagination
- `messages.yml` - All plugin messages
- `sounds.yml` - Sound configurations

## Building from Source

Requires Maven and Java 21:

```bash
mvn clean package
```

The compiled JAR will be in `target/Cosmetics-1.0.0.jar`.

## Database

Uses SQLite (file: `plugins/Cosmetics/data.db`). No external database required.

## Performance

- Centralized task system (no per-player schedulers)
- Async database operations
- Particle limits and optimization
- Automatic cleanup on player quit/disable

## Support

For issues or feature requests, check the plugin's GitHub repository.