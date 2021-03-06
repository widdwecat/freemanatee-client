package me.zopac.freemanatee.event;

import me.zopac.freemanatee.manatee;
import me.zopac.freemanatee.command.Command;
import me.zopac.freemanatee.command.commands.PeekCommand;
import me.zopac.freemanatee.event.events.DisplaySizeChangedEvent;
import me.zopac.freemanatee.gui.UIRenderer;
import me.zopac.freemanatee.gui.kami.KamiGUI;
import me.zopac.freemanatee.gui.rgui.component.container.use.Frame;
import me.zopac.freemanatee.module.ModuleManager;
import me.zopac.freemanatee.module.modules.render.BossStack;
import me.zopac.freemanatee.util.KamiTessellator;
import me.zopac.freemanatee.util.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiShulkerBox;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * Created by 086 on 11/11/2017.
 */
public class ForgeEventProcessor {
    float hue = 0;
    Color c;
    int rgb;
    int speed = 2;
    private int displayWidth;
    private int displayHeight;
    public static ForgeEventProcessor INSTANCE;
    public ForgeEventProcessor(){
        INSTANCE = this;
    }
    public Color getC(){
        return c;
    }
    public int getRgb(){
        return rgb;
    }
    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.isCanceled()) return;
//        manatee.EVENT_BUS.post(new UpdateEvent());

        if (Minecraft.getMinecraft().displayWidth != displayWidth || Minecraft.getMinecraft().displayHeight != displayHeight) {
            manatee.EVENT_BUS.post(new DisplaySizeChangedEvent());
            displayWidth = Minecraft.getMinecraft().displayWidth;
            displayHeight = Minecraft.getMinecraft().displayHeight;

            manatee.getInstance().getGuiManager().getChildren().stream()
                    .filter(component -> component instanceof Frame)
                    .forEach(component -> KamiGUI.dock((Frame) component));
        }

        if (PeekCommand.sb != null) {
            ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
            GuiShulkerBox gui = new GuiShulkerBox(Wrapper.getPlayer().inventory, PeekCommand.sb);
            gui.setWorldAndResolution(Wrapper.getMinecraft(), i, j);
            Minecraft.getMinecraft().displayGuiScreen(gui);
            PeekCommand.sb = null;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (Wrapper.getPlayer() == null) return;
        ModuleManager.onUpdate();
        manatee.getInstance().getGuiManager().callTick(manatee.getInstance().getGuiManager());
            c = Color.getHSBColor(hue, 1f, 1f);
            rgb = Color.HSBtoRGB(hue, 1f, 1f);
            hue += speed / 2000f;
            //Module onUpdate
        final Minecraft mc = Minecraft.getMinecraft();
            if (mc.player != null)
                ModuleManager.onUpdate();
        }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (event.isCanceled()) return;
        ModuleManager.onWorldRender(event);
    }

    @SubscribeEvent
    public void onRenderPre(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.BOSSINFO && ModuleManager.isModuleEnabled("BossStack")) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.isCanceled()) return;

        RenderGameOverlayEvent.ElementType target = RenderGameOverlayEvent.ElementType.EXPERIENCE;
        if (!Wrapper.getPlayer().isCreative() && Wrapper.getPlayer().getRidingEntity() instanceof AbstractHorse)
            target = RenderGameOverlayEvent.ElementType.HEALTHMOUNT;

        if (event.getType() == target) {
            ModuleManager.onRender();
            GL11.glPushMatrix();
            UIRenderer.renderAndUpdateFrames();
            GL11.glPopMatrix();
            KamiTessellator.releaseGL();
        } else if (event.getType() == RenderGameOverlayEvent.ElementType.BOSSINFO && ModuleManager.isModuleEnabled("BossStack")) {
            BossStack.render(event);
        }
    }
    public void setRainbowSpeed(int s){
        speed = s;
    }
    public int getRainbowSpeed(){
        return speed;
    }


    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState())
            ModuleManager.onBind(Keyboard.getEventKey());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatSent(ClientChatEvent event) {
        if (event.getMessage().startsWith(Command.getCommandPrefix())) {
            event.setCanceled(true);
            try {
                Wrapper.getMinecraft().ingameGUI.getChatGUI().addToSentMessages(event.getMessage());

                if (event.getMessage().length() > 1)
                    manatee.getInstance().commandManager.callCommand(event.getMessage().substring(Command.getCommandPrefix().length() - 1));
                else
                    Command.sendChatMessage("Please enter a command.");
            } catch (Exception e) {
                e.printStackTrace();
                Command.sendChatMessage("Error occured while running command! (" + e.getMessage() + ")");
            }
            event.setMessage("");
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDrawn(RenderPlayerEvent.Pre event) {
        manatee.EVENT_BUS.post(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDrawn(RenderPlayerEvent.Post event) {
        manatee.EVENT_BUS.post(event);
    }

    @SubscribeEvent()
    public void onChunkLoaded(ChunkEvent.Load event) {
        manatee.EVENT_BUS.post(event);
    }

    @SubscribeEvent()
    public void onChunkLoaded(ChunkEvent.Unload event) {
        manatee.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onInputUpdate(InputUpdateEvent event) {
        manatee.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onLivingEntityUseItemEventTick(LivingEntityUseItemEvent.Start entityUseItemEvent) {
        manatee.EVENT_BUS.post(entityUseItemEvent);
    }

    @SubscribeEvent
    public void onLivingDamageEvent(LivingDamageEvent event) {
        manatee.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onEntityJoinWorldEvent(EntityJoinWorldEvent entityJoinWorldEvent) {
        manatee.EVENT_BUS.post(entityJoinWorldEvent);
    }
    @SubscribeEvent
    public void onClientChatReceivedEvent(ClientChatReceivedEvent clientChatReceivedEvent) {
        manatee.EVENT_BUS.post(clientChatReceivedEvent);
    }
    @SubscribeEvent
    public void onPlayerPush(PlayerSPPushOutOfBlocksEvent event) {
        manatee.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        manatee.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent entityEvent) {
        manatee.EVENT_BUS.post(entityEvent);
    }

    @SubscribeEvent
    public void onRenderBlockOverlay(RenderBlockOverlayEvent event) {
        manatee.EVENT_BUS.post(event);
    }

}
