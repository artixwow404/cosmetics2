package com.cosmetics.cosmetic;

public enum CosmeticCategory {
    AURAS,
    TRAILS,
    WINGS,
    HATS,
    PETS,
    KILLSOUNDS,
    COMPANIONS;

    public String getDisplayName() {
        return switch (this) {
            case AURAS -> "Auras";
            case TRAILS -> "Trails";
            case WINGS -> "Wings";
            case HATS -> "Hats";
            case PETS -> "Pets";
            case KILLSOUNDS -> "Kill Sounds";
            case COMPANIONS -> "Companions";
        };
    }
}