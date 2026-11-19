package net.evilsnowmen.blizzard;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Вечная буря (бывший мод Eternal Blizzard).
 *
 * <ul>
 *   <li>Днём в Верхнем мире всегда идёт дождь (в холодных биомах это снег).</li>
 *   <li>Ночью, а также после команды /blizzard start, идёт буря: гроза, много снежинок
 *       вокруг игроков, игроки под открытым небом замерзают, как в рыхлом снеге.</li>
 *   <li>Во время бури {@link BlizzardSpawner} спавнит злых снеговиков в любом биоме.</li>
 * </ul>
 */
public final class Blizzard {
    public static final Logger LOGGER = LoggerFactory.getLogger("evil_snowmen/blizzard");

    /** Как часто (в тиках) проверять и принудительно выставлять погоду. */
    private static final int WEATHER_CHECK_INTERVAL = 20;

    /** Ночь в ванильном понимании: с 13000 по 23000 тик суток. */
    private static final long NIGHT_START = 13000L;
    private static final long NIGHT_END = 23000L;
    private static final long DAY_LENGTH = 24000L;

    /**
     * На сколько тиков заморозки прибавлять за тик бури.
     * Ваниль сама убавляет 2 за тик вне рыхлого снега, поэтому +3 даёт чистый +1 в тик,
     * как у рыхлого снега: полная заморозка (140 тиков) примерно за 7 секунд.
     */
    private static final int FREEZE_PER_TICK = 3;

    /** Снежинки: каждые N тиков, сколько штук и в каком облаке вокруг игрока. */
    private static final int SNOWFLAKE_INTERVAL = 2;
    private static final int SNOWFLAKES_PER_BURST = 14;
    private static final double SNOWFLAKE_SPREAD_XZ = 9.0;
    private static final double SNOWFLAKE_SPREAD_Y = 3.0;
    private static final double SNOWFLAKE_HEIGHT_ABOVE = 3.0;
    private static final double SNOWFLAKE_SPEED = 0.04;

    /** Буря по команде: сколько тиков осталось (0 = не запущена). */
    private static int forcedBlizzardTicks = 0;

    private Blizzard() {}

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(Blizzard::onServerTick);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> forcedBlizzardTicks = 0);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(
                        Commands.literal("blizzard")
                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .then(Commands.literal("start")
                                        // /blizzard start: на 5 минут
                                        .executes(ctx -> startBlizzard(ctx.getSource().getServer(), 300, ctx.getSource()))
                                        // /blizzard start <секунды>
                                        .then(Commands.argument("seconds", IntegerArgumentType.integer(1, 86400))
                                                .executes(ctx -> startBlizzard(
                                                        ctx.getSource().getServer(),
                                                        IntegerArgumentType.getInteger(ctx, "seconds"),
                                                        ctx.getSource()))))
                                .then(Commands.literal("stop")
                                        .executes(ctx -> stopBlizzard(ctx.getSource().getServer(), ctx.getSource())))
                )
        );
        LOGGER.info("Eternal blizzard loaded: it will always be raining, evil snowmen come with the storm.");
    }

    /** Буря идёт прямо сейчас: гроза при ночи или после /blizzard start. */
    public static boolean isStormActive(ServerLevel level) {
        return (forcedBlizzardTicks > 0 || isNight(level)) && level.isThundering();
    }

    private static int startBlizzard(MinecraftServer server, int seconds, CommandSourceStack source) {
        forcedBlizzardTicks = seconds * 20;
        enforceWeather(server, server.overworld(), true);
        source.sendSuccess(() -> Component.literal("Метель началась на " + seconds + " с."), true);
        return seconds;
    }

    private static int stopBlizzard(MinecraftServer server, CommandSourceStack source) {
        forcedBlizzardTicks = 0;
        enforceWeather(server, server.overworld(), isNight(server.overworld()));
        source.sendSuccess(() -> Component.literal("Метель по команде остановлена."), true);
        return 1;
    }

    private static void onServerTick(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        if (overworld == null) {
            return;
        }

        if (forcedBlizzardTicks > 0) {
            forcedBlizzardTicks--;
        }

        boolean storm = forcedBlizzardTicks > 0 || isNight(overworld);

        if (server.getTickCount() % WEATHER_CHECK_INTERVAL == 0) {
            enforceWeather(server, overworld, storm);
        }

        if (storm && overworld.isThundering()) {
            affectPlayers(overworld, server.getTickCount());
            BlizzardSpawner.tick(overworld);
        }
    }

    private static boolean isNight(ServerLevel level) {
        long timeOfDay = Math.floorMod(level.getOverworldClockTime(), DAY_LENGTH);
        return timeOfDay >= NIGHT_START && timeOfDay < NIGHT_END;
    }

    /**
     * Погода переключается обычной командой /weather, так что работает
     * корректно при любом внутреннем устройстве погоды в движке.
     */
    private static void enforceWeather(MinecraftServer server, ServerLevel overworld, boolean storm) {
        String command = null;

        if (storm) {
            if (!overworld.isThundering()) {
                command = "weather thunder";
            }
        } else if (!overworld.isRaining() || overworld.isThundering()) {
            command = "weather rain";
        }

        if (command != null) {
            server.getCommands().performPrefixedCommand(
                    server.createCommandSourceStack().withSuppressedOutput(),
                    command
            );
        }
    }

    private static void affectPlayers(ServerLevel level, int tick) {
        boolean spawnSnow = tick % SNOWFLAKE_INTERVAL == 0;

        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || !player.isAlive()) {
                continue;
            }
            // Только под открытым небом, как и положено буре
            if (!level.canSeeSky(player.blockPosition())) {
                continue;
            }

            // Много снежинок вокруг игрока
            if (spawnSnow) {
                level.sendParticles(
                        ParticleTypes.SNOWFLAKE,
                        player.getX(), player.getY() + SNOWFLAKE_HEIGHT_ABOVE, player.getZ(),
                        SNOWFLAKES_PER_BURST,
                        SNOWFLAKE_SPREAD_XZ, SNOWFLAKE_SPREAD_Y, SNOWFLAKE_SPREAD_XZ,
                        SNOWFLAKE_SPEED
                );
            }

            // Заморозка как в рыхлом снеге
            if (player.isCreative() || !player.canFreeze()) {
                continue;
            }
            int frozen = player.getTicksFrozen();
            int max = player.getTicksRequiredToFreeze();
            player.setTicksFrozen(Math.min(max, frozen + FREEZE_PER_TICK));
        }
    }
}
