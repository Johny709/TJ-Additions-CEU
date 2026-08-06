package tja.items.behaviors;

import gregtech.api.GTValues;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.util.List;

public class SupraSolarPanelBehavior implements IItemBehaviour {

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("metaitem.cover.solar.panel.tooltip.1"));
        lines.add(I18n.format("metaitem.cover.solar.panel.tooltip.2"));
        lines.add(I18n.format("metaitem.supra_solar_panel.tooltip.1"));
        lines.add(I18n.format("gregtech.universal.tooltip.voltage_out", Integer.MAX_VALUE, GTValues.VOCNF[GTValues.MAX]));
    }
}
