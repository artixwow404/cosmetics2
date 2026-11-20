# Resource Pack Information

This plugin supports custom models and textures via Oraxen or a custom resource pack.

## Oraxen Integration (Recommended)

If you have Oraxen installed, you can define custom models in your Oraxen configuration:

### Example Oraxen Configuration

```yaml
# Oraxen items.yml
cosmetics:
  hats:
    crown:
      displayname: "<gold>Crown"
      material: CARVED_PUMPKIN
      model:
        custom_model_data: 1001
        parent: "item/generated"
        textures:
          layer0: "cosmetics/hats/crown"
    tophat:
      displayname: "<dark_gray>Top Hat"
      material: CARVED_PUMPKIN
      model:
        custom_model_data: 1002
        parent: "item/generated"
        textures:
          layer0: "cosmetics/hats/tophat"
    # ... add more hats

  wings:
    angel:
      displayname: "<white>Angel Wings"
      material: ELYTRA
      model:
        custom_model_data: 2001
        parent: "item/elytra"
        textures:
          layer0: "cosmetics/wings/angel"
```

### Using with Cosmetics Plugin

In `cosmetics.yml`, set the `model` property to match your Oraxen item ID:

```yaml
hats:
  crown:
    model: "cosmetics_hats_crown"  # Oraxen item ID
```

The plugin will automatically detect Oraxen and use the custom models when displaying hats on players.

## Custom Resource Pack (Without Oraxen)

If you don't use Oraxen, you can create a resource pack with custom models:

### Pack Structure

```
cosmetics-resourcepack/
├── pack.mcmeta
└── assets/
    └── minecraft/
        ├── models/
        │   └── item/
        │       ├── cosmetics_hat_crown.json
        │       └── ...
        └── textures/
            └── item/
                └── cosmetics/
                    ├── hats/
                    │   ├── crown.png
                    │   └── ...
                    └── wings/
                        ├── angel.png
                        └── ...
```

### Example Model (crown.json)

```json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "minecraft:item/cosmetics/hats/crown"
  },
  "overrides": [
    {
      "predicate": {"custom_model_data": 1001},
      "model": "item/cosmetics_hat_crown"
    }
  ]
}
```

### Configuration

In `cosmetics.yml`, use the CustomModelData value:

```yaml
hats:
  crown:
    model: "crown"
    custom-model-data: 1001
```

## Hat Display Methods

The plugin supports multiple hat display methods:

1. **Armor Stand** (default) - Creates invisible armor stands with the hat item
2. **Item Display Entity** (1.19.4+) - Uses `item_display` entities for better performance
3. **Block Display Entity** (1.19.4+) - Uses `block_display` entities
4. **Oraxen** - Uses Oraxen's model engine

The plugin automatically selects the best available method based on server version and installed plugins.

## Particle Effects

All particle effects use vanilla Minecraft particles. No resource pack needed for:
- Auras
- Trails
- Wings (particle-based)
- Companions (particle-based)

## Sounds

Kill sounds use vanilla Minecraft sounds by default. For custom sounds:

1. Add sound files to your resource pack in `assets/minecraft/sounds/cosmetics/`
2. Define in `sounds.json`
3. Reference in `cosmetics.yml` and `sounds.yml`

Example `sounds.json`:
```json
{
  "cosmetics.kill.custom": {
    "category": "player",
    "sounds": ["cosmetics/kill/custom_sound"]
  }
}
```

Then in `cosmetics.yml`:
```yaml
killsounds:
  custom:
    sound: "cosmetics.kill.custom"
```

## Installation

1. Create your resource pack with the structure above
2. Zip the folder (not the parent folder)
3. Upload to your server/resource pack host
4. Set `resource-pack` in `server.properties`
5. Set `resource-pack-sha1` for verification

## Testing

Use `/cosmetics` to open the menu and preview items. Right-click any cosmetic in the GUI to preview it.