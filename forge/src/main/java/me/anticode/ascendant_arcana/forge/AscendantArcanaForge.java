package me.anticode.ascendant_arcana.forge;

import me.anticode.ascendant_arcana.AscendantArcana;
import dev.architectury.platform.forge.EventBuses;
import me.anticode.ascendant_arcana.client.AscendantArcanaClient;
import me.anticode.ascendant_arcana.client.screen.AArcanaEnchantingScreen;
import me.anticode.ascendant_arcana.init.AArcanaAttributes;
import me.anticode.ascendant_arcana.init.AArcanaItems;
import me.anticode.ascendant_arcana.init.AArcanaMenus;
import me.anticode.ascendant_arcana.item.RelicItem;
import me.anticode.ascendant_arcana.logic.Relics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AscendantArcana.MOD_ID)
public final class AscendantArcanaForge {
    public AscendantArcanaForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(AscendantArcana.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        // For some reason Forge requires attributes to be registered twice
//        AArcanaAttributes.initialize();
        AscendantArcana.initialize();
    }

    @Mod.EventBusSubscriber(modid = AscendantArcana.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class AscendantArcanaForgeClient {
        @SubscribeEvent
        public static void onClientSetup(final FMLClientSetupEvent event) {
            AscendantArcanaClient.initialize();

            event.enqueueWork(() -> {
                MenuScreens.register(AArcanaMenus.ENCHANTING.get(), AArcanaEnchantingScreen::new);

                ItemProperties.register(AArcanaItems.RELIC.get(), new ResourceLocation("relic_type"), ((itemStack, clientLevel, livingEntity, i) -> Relics.toId(RelicItem.getRelicType(itemStack)) / 5F));
                ItemProperties.register(AArcanaItems.RELIC.get(), new ResourceLocation("relic_strength"), (itemStack, clientWorld, livingEntity, seed) -> RelicItem.getRelicStrength(itemStack) / 5F);
            });
        }
    }
}
