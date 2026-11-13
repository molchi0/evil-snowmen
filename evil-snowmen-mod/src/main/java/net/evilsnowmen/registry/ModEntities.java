package net.evilsnowmen.registry;

import net.evilsnowmen.EvilSnowmenMod;
import net.evilsnowmen.entity.EvilSnowball;
import net.evilsnowmen.entity.EvilSnowman;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
    private ModEntities() {}

    public static final EntityType<EvilSnowman> EVIL_SNOWMAN = register(
            "evil_snowman",
            EntityType.Builder.<EvilSnowman>of(EvilSnowman::new, MobCategory.MONSTER)
                    .sized(0.7F, 1.9F)
                    .clientTrackingRange(8)
    );

    public static final EntityType<EvilSnowball> EVIL_SNOWBALL = register(
            "evil_snowball",
            EntityType.Builder.<EvilSnowball>of(EvilSnowball::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
    );

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, EvilSnowmenMod.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    /** Вызов нужен, чтобы статические поля инициализировались в нужный момент. */
    public static void init() {}
}
