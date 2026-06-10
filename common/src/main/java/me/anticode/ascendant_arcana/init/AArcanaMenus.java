package me.anticode.ascendant_arcana.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.screenhandler.AArcanaEnchantingMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class AArcanaMenus {
    private static final DeferredRegister<MenuType<?>> SCREEN_HANDLER = DeferredRegister.create(AscendantArcana.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<MenuType<AArcanaEnchantingMenu>> ENCHANTING = SCREEN_HANDLER.register(
            "enchanting_table",
            () -> new MenuType<AArcanaEnchantingMenu>(AArcanaEnchantingMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static void initialize () {
        SCREEN_HANDLER.register();
    }
}
