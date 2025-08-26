package org.sosly.witchcraft.renderers.items;

import org.sosly.witchcraft.items.armor.MoonthreadArmorItem;
import org.sosly.witchcraft.items.models.MoonthreadArmorModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class MoonthreadArmorRenderer extends GeoArmorRenderer<MoonthreadArmorItem> {
    public MoonthreadArmorRenderer() {
        super(new MoonthreadArmorModel());
    }
}
