package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.module.Module;

@Module.Info(
        name = "Capes",
        category = Module.Category.RENDER
)

public class Capes extends Module {

    private static Capes INSTANCE;

    public Capes() {
        INSTANCE = this;
    }

    public static boolean isActive() {
        return INSTANCE.isEnabled();
    }

}