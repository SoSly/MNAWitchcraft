package org.sosly.witchcraft.items.sympathy;

import com.mna.api.items.TieredItem;
import com.mna.items.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public class BloodyNeedleItem extends TieredItem {
    public BloodyNeedleItem() {
        super((new Item.Properties()).stacksTo(1));
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack item) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack item) {
        return new ItemStack(ItemInit.VINTEUM_NEEDLE.get());
    }

    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null) {
            tooltip.add(Component.translatable("item.mnaw.bound_poppet/not_bound"));
            return;
        }
        
        if (!nbt.hasUUID("target")) {
            tooltip.add(Component.translatable("item.mnaw.bound_poppet/not_bound").withStyle(ChatFormatting.DARK_PURPLE).withStyle(ChatFormatting.ITALIC));
            return;
        }

        UUID targetID = nbt.getUUID("target");
        Player target = Minecraft.getInstance().level.getPlayerByUUID(targetID);
        if (target != null) {
            tooltip.add(Component.translatable("item.mnaw.bound_poppet/player", target.getName().getString()).withStyle(ChatFormatting.DARK_PURPLE).withStyle(ChatFormatting.ITALIC));
            return;
        }
        
        String type = nbt.getString("type");
        tooltip.add(Component.translatable("item.mnaw.bound_poppet/mob", type).withStyle(ChatFormatting.DARK_PURPLE).withStyle(ChatFormatting.ITALIC));
    }
}
