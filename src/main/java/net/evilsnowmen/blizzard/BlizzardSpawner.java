package net.evilsnowmen.blizzard;

import net.evilsnowmen.entity.EvilSnowman;
import net.evilsnowmen.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

/**
 * Спавнит злых снеговиков вокруг игроков, пока идёт буря.
 * Биом не проверяется, поэтому они появляются везде: в пустыне, джунглях, океане (на берегу) и т. д.
 * Вызывается из {@link Blizzard} только во время бури.
 */
public final class BlizzardSpawner {
    /** Как часто (в тиках) пробовать спавн. 40 тиков = 2 секунды. */
    private static final int INTERVAL = 40;
    /** Сколько злых снеговиков максимум рядом с одним игроком. */
    private static final int MAX_NEARBY = 6;
    /** Радиус, в котором считаются «рядом». */
    private static final double CAP_RADIUS = 64.0;
    /** Сколько снеговиков за одну попытку (от 1 до этого числа). */
    private static final int MAX_PER_WAVE = 2;
    /** Сколько случайных мест пробовать за волну. */
    private static final int ATTEMPTS = 10;
    /** Расстояние от игрока до места спавна (как у ванильных монстров: не ближе 24). */
    private static final double MIN_DISTANCE = 24.0;
    private static final double MAX_DISTANCE = 44.0;

    private BlizzardSpawner() {}

    public static void tick(ServerLevel level) {
        if (level.getGameTime() % INTERVAL != 0) {
            return;
        }
        // Как и ванильные монстры, не появляются на мирном.
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }

        RandomSource random = level.getRandom();
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || !player.isAlive()) {
                continue;
            }
            AABB area = player.getBoundingBox().inflate(CAP_RADIUS);
            if (level.getEntitiesOfClass(EvilSnowman.class, area).size() >= MAX_NEARBY) {
                continue;
            }

            int toSpawn = 1 + random.nextInt(MAX_PER_WAVE);
            for (int attempt = 0; attempt < ATTEMPTS && toSpawn > 0; attempt++) {
                if (trySpawn(level, player, random)) {
                    toSpawn--;
                }
            }
        }
    }

    private static boolean trySpawn(ServerLevel level, ServerPlayer player, RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2.0;
        double distance = MIN_DISTANCE + random.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
        int x = Mth.floor(player.getX() + Math.cos(angle) * distance);
        int z = Mth.floor(player.getZ() + Math.sin(angle) * distance);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

        BlockPos pos = new BlockPos(x, y, z);
        BlockPos below = pos.below();

        // Только под открытым небом (там, где бушует буря), на твёрдой земле, не в воде.
        if (!level.canSeeSky(pos)) {
            return false;
        }
        if (!level.getFluidState(pos).isEmpty() || !level.getFluidState(below).isEmpty()) {
            return false;
        }
        if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
            return false;
        }

        EvilSnowman snowman = new EvilSnowman(ModEntities.EVIL_SNOWMAN, level);
        snowman.setPos(x + 0.5D, y, z + 0.5D);
        snowman.setYRot(random.nextFloat() * 360.0F);
        if (!level.noCollision(snowman)) {
            return false;
        }
        return level.addFreshEntity(snowman);
    }
}
