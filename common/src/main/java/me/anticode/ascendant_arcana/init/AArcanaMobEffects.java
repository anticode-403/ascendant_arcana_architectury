package me.anticode.ascendant_arcana.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.effect.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;

public class AArcanaMobEffects {
    private static final DeferredRegister<MobEffect> STATUS_EFFECTS = DeferredRegister.create(AscendantArcana.MOD_ID, Registries.MOB_EFFECT);

    public static RegistrySupplier<MobEffect> ARCHERS_GAMBIT = register("archers_gambit", new ArchersGambitEffect());
    public static RegistrySupplier<MobEffect> CROSS_COUNTER = register("cross_counter", new CrossCounterEffect());
    public static RegistrySupplier<MobEffect> ECHOING_DAMAGE = register("echoing_damage", new EchoingDamageEffect());
    public static RegistrySupplier<MobEffect> HOBBLED = register("hobbled", new HobbledEffect());
    public static RegistrySupplier<MobEffect> SUNDERED = register("sundered", new SunderedEffect());

    public static RegistrySupplier<MobEffect> register(String name, MobEffect effect) {
        return STATUS_EFFECTS.register(name, () -> effect);
    }

    public static void initialize() {
        STATUS_EFFECTS.register();
    }
}
