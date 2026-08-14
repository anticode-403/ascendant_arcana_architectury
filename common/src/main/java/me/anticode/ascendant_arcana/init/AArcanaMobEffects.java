package me.anticode.ascendant_arcana.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.effect.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;

import java.util.function.Supplier;

public class AArcanaMobEffects {
    private static final DeferredRegister<MobEffect> STATUS_EFFECTS = DeferredRegister.create(AscendantArcana.MOD_ID, Registries.MOB_EFFECT);

    public static RegistrySupplier<MobEffect> ALLEGRO = register("allegro", AllegroEffect::new);
    public static RegistrySupplier<MobEffect> ARCHERS_GAMBIT = register("archers_gambit", ArchersGambitEffect::new);
    public static RegistrySupplier<MobEffect> CROSS_COUNTER = register("cross_counter", CrossCounterEffect::new);
    public static RegistrySupplier<MobEffect> ECHOING_DAMAGE = register("echoing_damage", EchoingDamageEffect::new);
    public static RegistrySupplier<MobEffect> HOBBLED = register("hobbled", HobbledEffect::new);
    public static RegistrySupplier<MobEffect> MEGANEURA = register("meganeura", MeganeuraEffect::new);
    public static RegistrySupplier<MobEffect> SUNDERED = register("sundered", SunderedEffect::new);

    public static RegistrySupplier<MobEffect> register(String name, Supplier<MobEffect> effect) {
        return STATUS_EFFECTS.register(name, effect);
    }

    public static void initialize() {
        STATUS_EFFECTS.register();
    }
}
