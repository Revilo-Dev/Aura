package net.revilodev.aura.api;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

/** Client panel state signal for optional UI integrations. */
public final class PanelStateChangedEvent extends Event {
    private final ResourceLocation panelId;
    private final boolean open;

    public PanelStateChangedEvent(ResourceLocation panelId, boolean open) {
        this.panelId = panelId;
        this.open = open;
    }

    public ResourceLocation panelId() {
        return panelId;
    }

    public boolean open() {
        return open;
    }
}
