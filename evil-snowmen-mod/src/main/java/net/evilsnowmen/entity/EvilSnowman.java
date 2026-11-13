package net.evilsnowmen.entity;

import net.evilsnowmen.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/** Злой снеговик: бродит по снегу и закидывает игрока грязными снежками. */
public class EvilSnowman extends Monster implements RangedAttackMob {

    public EvilSnowman(EntityType<? extends EvilSnowman> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 14.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    public static boolean checkSpawnRules(EntityType<EvilSnowman> type, ServerLevelAccessor level,
                                          EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        // Не требует темноты: злые снеговики гуляют и днём.
        return Monster.checkAnyLightMonsterSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void registerGoals() {
        // Стреляет раз в секунду (20 тиков) с дистанции до 16 блоков.
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.0D, 20, 16.0F));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        // Злые снеговики отвечают добрым (ванильным) снеговикам.
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                this, LivingEntity.class, 10, true, false,
                (entity, level) -> entity.getType() == EntityType.SNOW_GOLEM));
    }

    /** Урон от снежка доброго снеговика (в ванили у снежка 0 урона). 2.0 = 1 сердце. */
    public static final float GOOD_SNOWBALL_DAMAGE = 2.0F;

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (amount <= 0.0F
                && source.getDirectEntity() != null
                && source.getDirectEntity().getType() == EntityType.SNOWBALL
                && source.getEntity() != null
                && source.getEntity().getType() == EntityType.SNOW_GOLEM) {
            amount = GOOD_SNOWBALL_DAMAGE;
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        double dx = target.getX() - this.getX();
        double dy = target.getEyeY() - 1.1D;
        double dz = target.getZ() - this.getZ();
        double arc = Math.sqrt(dx * dx + dz * dz) * 0.2D;

        EvilSnowball snowball = new EvilSnowball(ModEntities.EVIL_SNOWBALL, serverLevel);
        snowball.setOwner(this);
        snowball.setPos(this.getX(), this.getEyeY() - 0.1D, this.getZ());
        snowball.shoot(dx, dy + arc - snowball.getY(), dz, 1.6F, 8.0F);
        serverLevel.addFreshEntity(snowball);

        this.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SNOW_GOLEM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SNOW_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SNOW_GOLEM_DEATH;
    }
}
