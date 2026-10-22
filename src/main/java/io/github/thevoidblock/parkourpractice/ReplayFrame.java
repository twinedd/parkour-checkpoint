package io.github.thevoidblock.parkourpractice;

import io.github.thevoidblock.parkourpractice.fakes.FakePlayerEntity;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec3d;

record ReplayFrame(PlayerInput input, float yaw, float pitch, Vec3d position) {
    static ReplayFrame of(FakePlayerEntity ghost) {
        return new ReplayFrame(ghost.input.playerInput, ghost.getYaw(), ghost.getPitch(), ghost.getEntityPos());
    }
}
