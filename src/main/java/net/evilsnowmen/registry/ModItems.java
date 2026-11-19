package net.evilsnowmen.registry;

import java.util.function.Function;

import net.evilsnowmen.EvilSnowmenMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public final class ModItems {
    private ModItems() {}

    /** Предмет-«скин» грязного снежка: им рисуется летящий снаряд. */
    public static final Item DIRTY_SNOWBALL = register(
            "dirty_snowball", Item::new, new Item.Properties().stacksTo(16));

    public static final Item EVIL_SNOWMAN_SPAWN_EGG = register(
            "evil_snowman_spawn_egg", SpawnEggItem::new,
            new Item.Properties().spawnEgg(ModEntities.EVIL_SNOWMAN));

    private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, EvilSnowmenMod.id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
    }

    public static void init() {}
}
