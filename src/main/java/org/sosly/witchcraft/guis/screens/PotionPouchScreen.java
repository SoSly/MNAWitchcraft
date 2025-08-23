package org.sosly.witchcraft.guis.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.guis.containers.PotionPouchContainer;

public class PotionPouchScreen extends AbstractPlayerInventoryScreen<PotionPouchContainer> {
    public static final ResourceLocation InventoryTexture = new ResourceLocation(Witchcraft.MOD_ID, "textures/gui/potion_pouch.png");

    public PotionPouchScreen(PotionPouchContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 256;
        this.imageHeight = 256;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        this.renderBackground(graphics);
        int xPos = this.leftPos + (this.imageWidth / 2);
        int yPos = (this.topPos + this.imageHeight) - 90;

        this.renderPlayerInventory(graphics, xPos, yPos);

        xPos -= 96;
        yPos -= 72;

        int pockets = menu.getPotionPouchInventory().getPouch().getOrCreateTag().getInt("pocket");
        switch (pockets) {
            case 2: {
                graphics.blit(InventoryTexture, xPos, yPos, 0, 0, 104, 32);
                break;
            }
            case 1: {
                graphics.blit(InventoryTexture, xPos, yPos, 0, 86, 104, 32);
                break;
            }
            default: {
                graphics.blit(InventoryTexture, xPos, yPos, 0, 118, 104, 32);
                break;
            }
        }

        yPos -= 32 * (pockets + 1);
        graphics.blit(InventoryTexture, xPos, yPos, 0, 150, 104, 36);

        graphics.blit(InventoryTexture, xPos + 99, yPos - 1, 105, 150, 36, 36);
    }

    @Override
    public void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX - this.leftPos, mouseY - this.topPos);
    }

    private void renderPatches(GuiGraphics graphics, int xPos, int yPos) {

    }
}
