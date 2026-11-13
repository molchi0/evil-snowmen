package net.evilsnowmen.client;

import net.evilsnowmen.EvilSnowmenMod;
import net.evilsnowmen.entity.EvilSnowman;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class EvilSnowmanRenderer extends MobRenderer<EvilSnowman, LivingEntityRenderState, EvilSnowmanModel> {
    private static final Identifier TEXTURE = EvilSnowmenMod.id("textures/entity/evil_snowman.png");

    public EvilSnowmanRenderer(EntityRendererProvider.Context context) {
        // Модель запекается напрямую, без регистрации слоя: так меньше зависимостей от API Fabric.
        super(context, new EvilSnowmanModel(EvilSnowmanModel.createBodyLayer().bakeRoot()), 0.5F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }
}
