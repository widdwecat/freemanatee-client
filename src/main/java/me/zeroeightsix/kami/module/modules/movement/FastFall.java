package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.Module;

@Module.Info(
        name = "FastFall",
        category = Module.Category.MOVEMENT
)
public class FastFall extends Module {


    public void onUpdate() {
        if (mc.player.onGround) // gonna try and add a slider here to change how far you go down
            --mc.player.motionY;
    }
}