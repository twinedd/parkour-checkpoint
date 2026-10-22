package io.github.thevoidblock.parkourpractice;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

final class PracticeCommands {
    private PracticeCommands() {}

    static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) ->
                dispatcher.register(literal("pcheckpoint")
                        .then(literal("start").executes(source -> run(ParkourPractice::enable)))
                        .then(literal("reset").executes(source -> run(ParkourPractice::reset)))
                        .then(literal("apply").executes(source -> run(ParkourPractice::apply)))
                        .then(literal("cancel").executes(source -> run(ParkourPractice::stop)))));
    }

    private static int run(java.util.function.Consumer<MinecraftClient> action) {
        action.accept(MinecraftClient.getInstance());
        return 1;
    }
}
