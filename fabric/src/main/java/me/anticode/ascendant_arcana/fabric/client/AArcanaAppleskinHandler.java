package me.anticode.ascendant_arcana.fabric.client;

import me.anticode.ascendant_arcana.AscendantArcana;
import squeek.appleskin.api.AppleSkinApi;
import squeek.appleskin.api.event.HUDOverlayEvent;

public class AArcanaAppleskinHandler implements AppleSkinApi {
    @Override
    public void registerEvents() {
        HUDOverlayEvent.Exhaustion.EVENT.register(event -> {
            if (AscendantArcana.config.hide_xp_bar && AscendantArcana.config.disable_xp) event.y += 6;
        });
        HUDOverlayEvent.HealthRestored.EVENT.register(event -> {
            if (AscendantArcana.config.hide_xp_bar && AscendantArcana.config.disable_xp) event.y += 6;
        });
        HUDOverlayEvent.HungerRestored.EVENT.register(event -> {
            if (AscendantArcana.config.hide_xp_bar && AscendantArcana.config.disable_xp) event.y += 6;
        });
        HUDOverlayEvent.Saturation.EVENT.register(event -> {
            if (AscendantArcana.config.hide_xp_bar && AscendantArcana.config.disable_xp) event.y += 6;
        });
    }
}
