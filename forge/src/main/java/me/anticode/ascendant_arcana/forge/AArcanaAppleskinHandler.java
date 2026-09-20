package me.anticode.ascendant_arcana.forge;

import me.anticode.ascendant_arcana.AscendantArcana;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import squeek.appleskin.api.event.HUDOverlayEvent;

public class AArcanaAppleskinHandler {
    @SubscribeEvent
    public void HUDOverlayEvent(HUDOverlayEvent event) {
        if (AscendantArcana.config.disable_xp && AscendantArcana.config.hide_xp_bar) {
            event.y += 6;
        }
    }
}
