package io.github.thevoidblock.parkourpractice;

import net.minecraft.client.input.Input;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;

import java.util.List;

final class PlaybackInput extends Input {
    private final List<ReplayFrame> frames;
    private int nextIndex;
    private int appliedIndex = -1;

    PlaybackInput(List<ReplayFrame> frames) {
        this.frames = frames;
    }

    @Override
    public void tick() {
        if (nextIndex >= frames.size()) {
            playerInput = PlayerInput.DEFAULT;
            movementVector = Vec2f.ZERO;
            return;
        }
        appliedIndex = nextIndex;
        playerInput = frames.get(nextIndex++).input();
        float forward = (playerInput.forward() ? 1 : 0) - (playerInput.backward() ? 1 : 0);
        float sideways = (playerInput.left() ? 1 : 0) - (playerInput.right() ? 1 : 0);
        movementVector = new Vec2f(sideways, forward).normalize();
    }

    int appliedIndex() {
        return appliedIndex;
    }
}
