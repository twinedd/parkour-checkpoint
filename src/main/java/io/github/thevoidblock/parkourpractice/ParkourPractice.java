package io.github.thevoidblock.parkourpractice;

import io.github.thevoidblock.parkourpractice.fakes.FakePlayerEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ParkourPractice implements ClientModInitializer {
    public static final String MOD_ID = "parkourcheckpoint";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static FakePlayerEntity GHOST_PLAYER;
    public static boolean ENABLED = false;
    public static boolean PLAYING = false;
    private static Input realInput;
    private static Snapshot checkpoint;
    private static final List<ReplayFrame> frames = new ArrayList<>();
    private static PlaybackInput playbackInput;
    private static int ticksPlayed;
    private static boolean skipCaptureOnce;
    private static boolean recordingOverflow;
    private static final int MAX_FRAMES = 12000;

    private record Snapshot(Vec3d position, Vec3d velocity, float yaw, float pitch, boolean onGround) {
        static Snapshot of(Entity entity) {
            return new Snapshot(entity.getEntityPos(), entity.getVelocity(), entity.getYaw(), entity.getPitch(), entity.isOnGround());
        }
    }

    @Override
    public void onInitializeClient() {
        KeyBindings.register();
        PracticeCommands.register();

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> stop(client));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> stop(client));
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> stop(client));
        ClientTickEvents.START_CLIENT_TICK.register(ParkourPractice::beforeTick);
        ClientTickEvents.END_CLIENT_TICK.register(ParkourPractice::afterTick);

        LOGGER.info("{} initialized", MOD_ID);
    }

    protected static void sendToggleNotification(MinecraftClient client, String module, boolean value) {
        if (client.player == null) return;
        client.player.sendMessage(
                Text.translatable("text.parkourcheckpoint." + module)
                        .append(" ")
                        .append((value ?
                                Text.translatable("text.parkourcheckpoint.on") :
                                Text.translatable("text.parkourcheckpoint.off"))
                                .formatted(value ? Formatting.GREEN : Formatting.RED)),
                true
        );
    }

    protected static void enable(MinecraftClient client) {
        if(ENABLED) return;

        if (client.player == null || client.world == null || PLAYING) return;
        checkpoint = Snapshot.of(client.player);
        realInput = client.player.input;
        frames.clear();
        recordingOverflow = false;
        skipCaptureOnce = true;
        ENABLED = true;

        GHOST_PLAYER = new FakePlayerEntity(client);
        client.world.addEntity(GHOST_PLAYER);

        client.player.input = new Input();
        message(client, "Training started. F6 resets, F7 replays, F8 cancels.");
    }

    private static void removeGhost(MinecraftClient client) {
        if (!ENABLED) return;
        ENABLED = false;
        if (GHOST_PLAYER != null && GHOST_PLAYER.getEntityWorld() instanceof ClientWorld world) {
            world.removeEntity(GHOST_PLAYER.getId(), Entity.RemovalReason.DISCARDED);
        }
        if (client.player != null && realInput != null) client.player.input = realInput;
        GHOST_PLAYER = null;
    }

    protected static void reset(MinecraftClient client) {
        if (!ENABLED || checkpoint == null || client.world == null) return;
        removeGhost(client);
        GHOST_PLAYER = new FakePlayerEntity(client);
        GHOST_PLAYER.refreshPositionAndAngles(checkpoint.position, checkpoint.yaw, checkpoint.pitch);
        GHOST_PLAYER.setVelocity(checkpoint.velocity);
        GHOST_PLAYER.setOnGround(checkpoint.onGround);
        client.world.addEntity(GHOST_PLAYER);
        client.player.input = new Input();
        ENABLED = true;
        frames.clear();
        recordingOverflow = false;
        skipCaptureOnce = true;
        message(client, "Attempt reset.");
    }

    protected static void apply(MinecraftClient client) {
        if (!ENABLED || client.player == null || checkpoint == null || frames.isEmpty()) return;
        if (recordingOverflow) {
            message(client, "Recording was too long. Reset and try a shorter segment.");
            return;
        }
        if (client.player.getEntityPos().distanceTo(checkpoint.position) > 0.08 ||
                client.player.getVelocity().distanceTo(checkpoint.velocity) > 0.08) {
            message(client, "Server position or motion changed since the checkpoint; replay cancelled.");
            return;
        }
        removeGhost(client);
        playbackInput = new PlaybackInput(List.copyOf(frames));
        client.player.input = playbackInput;
        ticksPlayed = 0;
        PLAYING = true;
        message(client, "Replaying " + frames.size() + " ticks. F8 aborts.");
    }

    protected static void stop(MinecraftClient client) {
        removeGhost(client);
        if (PLAYING && client.player != null && realInput != null) client.player.input = realInput;
        PLAYING = false;
        playbackInput = null;
        realInput = null;
        checkpoint = null;
        frames.clear();
        ticksPlayed = 0;
        recordingOverflow = false;
        skipCaptureOnce = false;
    }

    private static void beforeTick(MinecraftClient client) {
        if (!PLAYING || client.player == null || playbackInput == null) return;
        if (client.currentScreen != null || ticksPlayed >= frames.size()) {
            stop(client);
            return;
        }
        ReplayFrame frame = frames.get(ticksPlayed);
        client.player.setYaw(frame.yaw());
        client.player.setPitch(frame.pitch());
    }

    private static void afterTick(MinecraftClient client) {
        if (ENABLED && GHOST_PLAYER != null) {
            if (client.player == null || client.world == null) { stop(client); return; }
            if (GHOST_PLAYER.getY() < client.world.getBottomY() - 16) { reset(client); return; }
            if (skipCaptureOnce) {
                skipCaptureOnce = false;
            } else if (frames.size() < MAX_FRAMES) {
                frames.add(ReplayFrame.of(GHOST_PLAYER));
            } else if (!recordingOverflow) {
                recordingOverflow = true;
                message(client, "Recording limit reached (10 minutes). Reset for a shorter segment.");
            }
        }
        if (PLAYING && client.player != null && playbackInput != null) {
            int applied = playbackInput.appliedIndex();
            if (applied >= 0 && applied < frames.size()) {
                ReplayFrame frame = frames.get(applied);
                if (client.player.getEntityPos().distanceTo(frame.position()) > 0.30) {
                    message(client, "Replay stopped: movement differs from practice.");
                    stop(client);
                    return;
                }
                ticksPlayed = applied + 1;
                if (ticksPlayed >= frames.size()) {
                    message(client, "Replay finished. Check the server landing position.");
                    stop(client);
                }
            }
        }
    }

    private static void message(MinecraftClient client, String value) {
        if (client.player != null) client.player.sendMessage(Text.literal("[Parkour Checkpoint] " + value), false);
    }
}
