package io.github.thevoidblock.parkourpractice;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import static io.github.thevoidblock.parkourpractice.ParkourPractice.*;

public class KeyBindings {
    private static final KeyBinding.Category CATEGORY = new KeyBinding.Category(Identifier.of(MOD_ID, "main"));

    public static final KeyBinding ACTIVATE = registerKeybind("activate", GLFW.GLFW_KEY_F4);
    public static final KeyBinding RESET = registerKeybind("reset", GLFW.GLFW_KEY_F6);
    public static final KeyBinding APPLY = registerKeybind("apply", GLFW.GLFW_KEY_F7);
    public static final KeyBinding ABORT = registerKeybind("abort", GLFW.GLFW_KEY_F8);

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(client.player == null || client.currentScreen != null) return;

            if(ACTIVATE.wasPressed()) {
                if(ENABLED || PLAYING) ParkourPractice.stop(client);
                else ParkourPractice.enable(client);
                ParkourPractice.sendToggleNotification(client, "ghost-player", ENABLED);
            }

            if(RESET.wasPressed() && ENABLED) {
                ParkourPractice.reset(client);
            }

            if(APPLY.wasPressed() && ENABLED) ParkourPractice.apply(client);
            if(ABORT.wasPressed() && (ENABLED || PLAYING)) ParkourPractice.stop(client);
        });
    }

    private static KeyBinding registerKeybind(String name, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding("key.parkourcheckpoint." + name, key, CATEGORY));
    }
}
