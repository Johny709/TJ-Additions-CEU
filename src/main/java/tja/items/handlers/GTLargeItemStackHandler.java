package tja.items.handlers;

import gregtech.api.capability.INotifiableHandler;
import gregtech.api.metatileentity.MetaTileEntity;

import java.util.ArrayList;
import java.util.List;

public class GTLargeItemStackHandler extends LargeItemStackHandler implements INotifiableHandler {

    final List<MetaTileEntity> notifiableEntities = new ArrayList<>();
    private final MetaTileEntity metaTileEntity;
    private final boolean isExport;

    public GTLargeItemStackHandler(MetaTileEntity metaTileEntity, MetaTileEntity entityToNotify, boolean isExport, int slots, int capacity) {
        super(slots, capacity);
        this.metaTileEntity = metaTileEntity;
        this.isExport = isExport;
        if (entityToNotify != null)
            this.notifiableEntities.add(entityToNotify);
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        this.metaTileEntity.markDirty();
        for (MetaTileEntity metaTileEntity : this.notifiableEntities) {
            if (metaTileEntity != null && metaTileEntity.isValid()) {
                this.addToNotifiedList(metaTileEntity, this, this.isExport);
            }
        }
    }

    @Override
    public void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        if (metaTileEntity == null) return;
        this.notifiableEntities.add(metaTileEntity);
    }

    @Override
    public void removeNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
        this.notifiableEntities.remove(metaTileEntity);
    }
}
