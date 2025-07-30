package org.sosly.witchcraft.items.alchemy;

import com.mna.KeybindInit;
import com.mna.api.items.ITieredItem;
import com.mna.blocks.tileentities.ChalkRuneTile;
import com.mna.items.base.IRadialInventorySelect;
import com.mna.items.base.IRadialMenuItem;
import com.mna.items.base.ItemBagBase;
import com.mna.items.filters.ItemFilterGroup;
import com.mna.items.ritual.ItemPractitionersPatch;
import com.mna.items.ritual.PractitionersPouchPatches;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.sosly.witchcraft.ServerConfig;
import org.sosly.witchcraft.guis.providers.PotionPouchProvider;
import org.sosly.witchcraft.inventories.PotionPouchInventory;
import org.sosly.witchcraft.utils.SympathyHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PotionPouchItem extends ItemBagBase implements IRadialInventorySelect, ITieredItem<PotionPouchItem> {
    private int tier;

    public PotionPouchItem() {
        super(new Properties().stacksTo(1));
    }

    public boolean addPatchToPouch(ItemStack pouch, ItemStack patch) {
        if (!canModifyPouch(patch)) {
            return false;
        }

        ItemPractitionersPatch patchItem = (ItemPractitionersPatch) patch.getItem();
        if (patchItem.getPatch() == PractitionersPouchPatches.DEPTH) {
            pouch.getOrCreateTag().putInt("depth", patchItem.getLevel());
            return true;
        } else if (patchItem.getPatch() == PractitionersPouchPatches.SPEED) {
            pouch.getOrCreateTag().putInt("speed", patchItem.getLevel());
            return true;
        } else if (patchItem.getPatch() == PractitionersPouchPatches.CONVEYANCE) {
            pouch.getOrCreateTag().putInt("conveyance", 1);
            return true;
        } else if (patchItem.getPatch() == PractitionersPouchPatches.COLLECTION) {
            pouch.getOrCreateTag().putInt("collection", 1);
            return true;
        }

        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltips, TooltipFlag flag) {
        MutableComponent component = Component.translatable("item.mna.patch.prompt");

        List<String> patches = ((PotionPouchItem)stack.getItem()).getPatches(stack);
        for (int i = 0; i < patches.size(); i++) {
            component.append(Component.translatable("item.mna.patch_" + patches.get(i) + ".simple"));
            if (i < patches.size() - 2) {
                component.append(", ");
            }
            if (i == patches.size() - 2) {
                component.append(", and ");
            }
        }
        component.withStyle(ChatFormatting.YELLOW);
        tooltips.add(component);

        String txt = I18n.get((KeybindInit.RadialMenuOpen.get()).getKey().getDisplayName().getString());
        tooltips.add(Component.translatable("item.mna.item-with-gui.radial-open", txt).withStyle(ChatFormatting.AQUA));

        super.appendHoverText(stack, level, tooltips, flag);
    }

    @Override
    public int getCachedTier() {
        return this.tier;
    }

    @Override
    public void setCachedTier(int tier) {
        this.tier = tier;
    }

    public boolean canModifyPouch(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemPractitionersPatch)) {
            return false;
        }

        ItemPractitionersPatch patchItem = (ItemPractitionersPatch) stack.getItem();
        PractitionersPouchPatches patch = patchItem.getPatch();

        return patch == PractitionersPouchPatches.DEPTH ||
                patch == PractitionersPouchPatches.SPEED ||
                patch == PractitionersPouchPatches.CONVEYANCE ||
                patch == PractitionersPouchPatches.COLLECTION;
    }

    public int capacity(ItemStack stack) {
        return getInventory(stack).getSlots();
    }

    @Override
    public int capacity() {
        return 5;
    }

    @Override
    public int capacity(@Nullable Player player) {
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (stack.getItem() instanceof PotionPouchItem) {
            return ((PotionPouchItem) stack.getItem()).capacity(stack);
        }
        return 0;
    }

    @Override
    public ItemFilterGroup filterGroup() {
        return null;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack pouch, @NotNull Level level, @NotNull LivingEntity target) {
        if (level.isClientSide()) {
            return pouch;
        }

        ItemStack potionStack = getPotionStack(pouch);
        if (potionStack.isEmpty()) {
            return pouch;
        }
        if (pouch.getOrCreateTag().getInt("conveyance") == 1 && pouch.getOrCreateTag().contains("conveyance_target")) {
            UUID targetUUID = pouch.getOrCreateTag().getUUID("conveyance_target");
            Player playerTarget = level.getPlayerByUUID(targetUUID);
            if (playerTarget != null) {
                if (ServerConfig.bossesBlockSympathy && SympathyHelper.isInBossArena((ServerLevel) level, target)) {
                    target.sendSystemMessage(Component.translatable("rituals.sympathy.target_protected"));
                    return pouch;
                }

                if (ServerConfig.bossesImmuneToSympathy && SympathyHelper.isBoss(target)) {
                    target.sendSystemMessage(Component.translatable("rituals.sympathy.target_protected"));
                    return pouch;
                }
                // todo: support broken sympathy ritual
                target = playerTarget;
            }
        }
        potionStack.finishUsingItem(level, target);
        getInventory(pouch).setStackInSlot(getIndex(pouch), potionStack);
        return pouch;
    }

    @Override
    public IItemHandlerModifiable getInventory(ItemStack itemStack) {
        return this.getInventory(itemStack, null);
    }

    @Override
    public IItemHandlerModifiable getInventory(ItemStack itemStack, @Nullable Player player) {
        return new PotionPouchInventory(itemStack);
    }

    @Override
    public int getIndex(ItemStack stack) {
        return stack.getOrCreateTag().getInt("radial_index");
    }

    public List<String> getPatches(ItemStack stack) {
        List<String> patches = new ArrayList<>();
        int depth = stack.getOrCreateTag().getInt("depth");
        if (depth == 1) {
            patches.add("depth");
        } else if (depth == 2) {
            patches.add("depth_2");
        }

        int speed = stack.getOrCreateTag().getInt("speed");
        if (speed == 1) {
            patches.add("speed");
        } else if (speed == 2) {
            patches.add("speed_2");
        } else if (speed == 3) {
            patches.add("speed_3");
        }

        if (stack.getOrCreateTag().getInt("conveyance") == 1) {
            patches.add("conveyance");
        }

        if (stack.getOrCreateTag().getInt("collection") == 1) {
            patches.add("collection");
        }

        return patches;
    }

    public static int getPotionDepth(ItemStack pouch) {
        int depth = pouch.getOrCreateTag().getInt("depth");
        if (depth == 1) {
            return 32;
        }
        if (depth == 2) {
            return 64;
        }
        return 16;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        ItemStack potionStack = getPotionStack(stack);
        if (potionStack.isEmpty()) {
            return 0;
        }

        float speed = stack.getOrCreateTag().getInt("speed");
        float modifier = 1.0f / (speed + 1);
        return (int) (potionStack.getItem().getUseDuration(potionStack) * modifier);
    }

    @Override
    public void setIndex(@Nullable Player player, InteractionHand hand, ItemStack stack, int idx) {
        this.setIndex(stack, idx);
    }

    @Override
    public void setIndex(ItemStack stack, int idx) {
        stack.getOrCreateTag().putInt("radial_index", idx);
    }

    public ItemStack getPotionStack(ItemStack stack) {
        return getInventory(stack).getStackInSlot(getIndex(stack));
    }

    @Override
    public MenuProvider getProvider(ItemStack itemStack) {
        return new PotionPouchProvider(itemStack);
    }

    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level level, Player player, @Nonnull InteractionHand hand) {
        boolean guiOpened = false;
        if (!level.isClientSide() && hand == InteractionHand.MAIN_HAND) {
            guiOpened = this.openGuiIfModifierPressed(player.getItemInHand(hand), player, level);
        }

        if (guiOpened) {
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }

        if (level.isClientSide()) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        ItemStack pouch = player.getItemInHand(hand);
        ItemStack potionStack = getPotionStack(pouch);
        if (potionStack.isEmpty()) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        potionStack.use(level, player, hand);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        return this.use(context.getLevel(), player, context.getHand()).getResult();
    }
}
