package net.evilsnowmen.entity;

import net.evilsnowmen.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Грязный снежок злого снеговика: наносит урон, замораживает
 * (иней на экране, как в powder snow) и накладывает замедление, как стрела зимогора.
 */
public class EvilSnowball extends ThrowableItemProjectile {
    /** 3.0 = 1.5 сердца. */
    public static final float DAMAGE = 3.0F;
    /** 600 тиков = 30 секунд, как у стрелы зимогора. */
    public static final int SLOWNESS_TICKS = 600;
    /** Выше 140 тиков — полное обледенение; потом убывает по 2 за тик. */
    public static final int FREEZE_TICKS = 260;

    public EvilSnowball(EntityType<? extends EvilSnowball> type, Level level) {
        super(type, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.DIRTY_SNOWBALL;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        // Снеговики не бьют друг друга.
        return !(entity instanceof EvilSnowman) && super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Entity target = result.getEntity();
        DamageSource source = this.damageSources().thrown(this, this.getOwner());
        if (target.hurtServer(serverLevel, source, DAMAGE) && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SLOWNESS_TICKS, 0));
            if (living.canFreeze()) {
                living.setTicksFrozen(Math.max(living.getTicksFrozen(), FREEZE_TICKS));
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    this.getX(), this.getY(), this.getZ(), 8, 0.12, 0.12, 0.12, 0.04);
            this.discard();
        }
    }
}
