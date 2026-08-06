package tja.items.behaviors;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.util.List;

public class CreativeFluidCoverBehavior implements IItemBehaviour {

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("metaitem.creative_cover.tooltip.1"));
        lines.add(I18n.format("metaitem.creative_fluid_cover.tooltip.1"));
    }
}
