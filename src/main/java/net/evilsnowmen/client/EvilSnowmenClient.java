package net.evilsnowmen.client;

import net.evilsnowmen.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class EvilSnowmenClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.EVIL_SNOWMAN, EvilSnowmanRenderer::new);
        // Снежок рисуется как предмет dirty_snowball (грязная текстура).
        EntityRendererRegistry.register(ModEntities.EVIL_SNOWBALL, ThrownItemRenderer::new);
    }
}
