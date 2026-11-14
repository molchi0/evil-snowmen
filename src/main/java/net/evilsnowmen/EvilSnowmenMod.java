package net.evilsnowmen;

import net.evilsnowmen.blizzard.Blizzard;
import net.evilsnowmen.entity.EvilSnowman;
import net.evilsnowmen.registry.ModEntities;
import net.evilsnowmen.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.resources.Identifier;

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

        // Естественного спавна по биомам нет: злые снеговики приходят только с бурей
        // (см. BlizzardSpawner), зато в любом биоме.
        Blizzard.init();
    }
}
