package me.zeroeightsix.kami.module.modules.combat;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.Friends;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

@Module.Info(name = "OffhandBed", category = Module.Category.COMBAT)
public class OffhandBed extends Module {

    private Setting<Boolean> totemdisable = this.register(Settings.b("TotemDisable", false));
    private Setting<Integer> health = this.register(Settings.integerBuilder("Health Switch").withRange(1, 36).withValue(16));
    int beds;

    public void onEnable() {
        if (totemdisable.getValue()) {
            ModuleManager.getModuleByName("AutoTotem").disable();
        }
    }

    public void onDisable() {
        if (totemdisable.getValue()) {
            ModuleManager.getModuleByName("AutoTotem").enable();
        }
    }

    public void onUpdate() {
        beds = mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.BED).mapToInt(ItemStack::getCount).sum();

        if (mc.currentScreen instanceof GuiContainer || mc.world == null || mc.player == null)
            return;
        if (!shouldTotem()) {
            if (!(mc.player.getHeldItemOffhand() != ItemStack.EMPTY && mc.player.getHeldItemOffhand().getItem() == Items.BED)) {
                final int slot = getGapSlot() < 9 ? getGapSlot() + 36 : getGapSlot();
                if (getGapSlot() != -1) {
                    mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
                }
            }
        } else if (!(mc.player.getHeldItemOffhand() != ItemStack.EMPTY && mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING)) {
            final int slot = getTotemSlot() < 9 ? getTotemSlot() + 36 : getTotemSlot();
            if (getTotemSlot() != -1) {
                mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player);
            }
        }
    }

    private boolean nearPlayers() {
        return mc.world.playerEntities.stream().anyMatch(e -> e != mc.player && e.getEntityId() != -1488 && !Friends.isFriend(e.getName()) && mc.player.getDistance(e) <= 200);
    }

    private boolean shouldTotem() {
        if (mc.player != null) {
            return (mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.ELYTRA || !nearPlayers() || mc.player.getHealth() + mc.player.getAbsorptionAmount() <= health.getValue());
        }
        return (mc.player.getHealth() + mc.player.getAbsorptionAmount()) <= health.getValue() || mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.ELYTRA || !nearPlayers();
    }

    private boolean isEmpty(BlockPos pos){
        return mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos)).stream().filter(e -> e instanceof EntityEnderCrystal).count() == 0;
    }

    private boolean isGappleAABBEmpty(){
        return isEmpty(mc.player.getPosition().add(1, 0, 0)) && isEmpty(mc.player.getPosition().add(-1, 0, 0)) && isEmpty(mc.player.getPosition().add(0, 0, 1)) && isEmpty(mc.player.getPosition().add(0, 0, -1)) && isEmpty(mc.player.getPosition());
    }

    int getGapSlot() {
        int gapSlot = -1;
        for (int i = 45; i > 0; i--) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.BED) {
                gapSlot = i;
                break;
            }
        }
        return gapSlot;
    }

    int getTotemSlot() {
        int totemSlot = -1;
        for (int i = 45; i > 0; i--) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.TOTEM_OF_UNDYING) {
                totemSlot = i;
                break;
            }
        }
        return totemSlot;
    }

    @Override
    public String getHudInfo() {
        return "\u00A77[\u00A7f" + beds + "\u00A77]";
    }
}