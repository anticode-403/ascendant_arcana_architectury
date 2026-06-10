package me.anticode.ascendant_arcana.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import me.anticode.ascendant_arcana.AscendantArcana;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class AArcanaAttributes {
    public static DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(AscendantArcana.MOD_ID, Registries.ATTRIBUTE);

    public static final RegistrySupplier<Attribute> PROTECTION = register("generic.protection", new RangedAttribute("attribute.ascendant_arcana.generic.protection", 1.0d, 0.0d, 100.0d));
    public static final RegistrySupplier<Attribute> DAMAGE_TAKEN = register("generic.damage_taken", new RangedAttribute("attribute.ascendant_arcana.generic.damage_taken", 1.0d, 0.0d, 100.0d));

    private static RegistrySupplier<Attribute> register(String id, final Attribute attribute) {
        return ATTRIBUTES.register(id, () -> attribute);
    }

    public static void initialize() {
        ATTRIBUTES.register();
    }
}
