package org.sosly.witchcraft.items.models;

import net.minecraft.resources.ResourceLocation;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.items.armor.MoonthreadArmorItem;
import software.bernie.geckolib.model.GeoModel;

public class MoonthreadArmorModel extends GeoModel<MoonthreadArmorItem> {
    public MoonthreadArmorModel() {
    }

    public ResourceLocation getModelResource(MoonthreadArmorItem object) {
        return new ResourceLocation(Witchcraft.MOD_ID, "geo/moonthread_armor.geo.json");
    }

    public ResourceLocation getTextureResource(MoonthreadArmorItem object) {
        return new ResourceLocation(Witchcraft.MOD_ID, "textures/item/moonthread_armor.png");
    }

    public ResourceLocation getAnimationResource(MoonthreadArmorItem animatable) {
        // todo: get some animations maybe
        return new ResourceLocation("mna", "animations/none.anim.json");
    }
}
