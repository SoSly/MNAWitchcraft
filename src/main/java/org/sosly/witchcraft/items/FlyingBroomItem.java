package org.sosly.witchcraft.items;

import com.mna.api.items.TieredItem;
import net.minecraft.world.item.Item;

public class FlyingBroomItem extends TieredItem {
    public FlyingBroomItem() {
        super((new Item.Properties()).stacksTo(1));
    }
}