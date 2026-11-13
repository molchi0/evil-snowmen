package net.evilsnowmen;

import net.evilsnowmen.entity.EvilSnowman;
import net.evilsnowmen.registry.ModEntities;
import net.evilsnowmen.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.biome.Biomes;

public class EvilSnowmenMod implements ModInitializer {
    public static final String MOD_ID = "evil_snowmen";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        // Порядок важен: предметы ссылаются на сущности.
        ModEntities.init();
        ModItems.init();

        FabricDefaultAttributeRegistry.register(ModEntities.EVIL_SNOWMAN, EvilSnowman.createAttributes());

        SpawnPlacements.register(
                ModEntities.EVIL_SNOWMAN,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EvilSnowman::checkSpawnRules
        );

        // Естественный спавн в заснеженных биомах (и днём, и ночью).
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(
                        Biomes.SNOWY_PLAINS,
                        Biomes.ICE_SPIKES,
                        Biomes.SNOWY_TAIGA,
                        Biomes.SNOWY_SLOPES,
                        Biomes.GROVE,
                        Biomes.FROZEN_PEAKS,
                        Biomes.JAGGED_PEAKS,
                        Biomes.FROZEN_RIVER,
                        Biomes.SNOWY_BEACH
                ),
                MobCategory.MONSTER,
                ModEntities.EVIL_SNOWMAN,
                60, 1, 2
        );
    }
}
