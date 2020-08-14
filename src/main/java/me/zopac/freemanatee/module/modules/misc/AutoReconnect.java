package me.zopac.freemanatee.module.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zopac.freemanatee.event.events.GuiScreenEvent;
import me.zopac.freemanatee.module.Module;
import me.zopac.freemanatee.setting.Setting;
import me.zopac.freemanatee.setting.Settings;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;

@Module.Info(
        name = "AutoReconnect",
        category = Module.Category.MISC
)
public class AutoReconnect extends Module {

    private Setting<Integer> seconds = register(Settings.integerBuilder("Seconds").withValue(5).withMinimum(0).build());
    private static ServerData cServer;

    @EventHandler
    public Listener<GuiScreenEvent.Closed> closedListener = new Listener<>(event -> {
        if (event.getScreen() instanceof GuiConnecting)
            cServer = mc.currentServerData;
    });

    @EventHandler
    public Listener<GuiScreenEvent.Displayed> displayedListener = new Listener<>(event -> {
        if (isEnabled() && event.getScreen() instanceof GuiDisconnected && (cServer != null || mc.currentServerData != null))
            event.setScreen(new KamiGuiDisconnected((GuiDisconnected) event.getScreen()));
    });

    private class KamiGuiDisconnected extends GuiDisconnected {

        int millis = seconds.getValue() * 1000;
        long cTime;

        public KamiGuiDisconnected(GuiDisconnected disconnected) {
            super(disconnected.parentScreen, disconnected.reason, disconnected.message);
            cTime = System.currentTimeMillis();
        }

        @Override
        public void updateScreen() {
            if (millis <= 0)
                mc.displayGuiScreen(new GuiConnecting(parentScreen, mc, cServer == null ? mc.currentServerData : cServer));
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            super.drawScreen(mouseX, mouseY, partialTicks);

            long a = System.currentTimeMillis();
            millis -= a - cTime;
            cTime = a;

            String s = "Reconnecting in " + Math.max(0, Math.floor((double) millis / 100) / 10) + "s";
            fontRenderer.drawString(s, width / 2 - fontRenderer.getStringWidth(s) / 2, height - 16, 0xffffff, true);
        }

    }

}