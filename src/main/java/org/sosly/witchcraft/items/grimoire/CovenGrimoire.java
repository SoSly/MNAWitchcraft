package org.sosly.witchcraft.items.grimoire;

import com.mna.items.sorcery.Grimoire;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.NonNullLazy;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.items.renderers.CovenGrimoireBookRenderer;

import java.util.function.Consumer;

public class CovenGrimoire extends Grimoire {
    public CovenGrimoire() {
        super(new ResourceLocation(Witchcraft.MOD_ID, "coven"), true);
    }
    
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final NonNullLazy<BlockEntityWithoutLevelRenderer> ister = NonNullLazy.of(() -> 
                new CovenGrimoireBookRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), 
                    Minecraft.getInstance().getEntityModels())
            );
            
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ister.get();
            }
        });
    }
}