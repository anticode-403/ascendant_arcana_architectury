package me.anticode.ascendant_arcana.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.entity.BlazeboltEntity;
import me.anticode.ascendant_arcana.entity.SingularityEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

public class AArcanaEntities {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(AscendantArcana.MOD_ID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<BlazeboltEntity>> BLAZEBOLT_ENTITY = register("blazebolt", () -> EntityType.Builder.<BlazeboltEntity>of(BlazeboltEntity::new, MobCategory.MISC).sized(EntityType.ARROW.getWidth(), EntityType.ARROW.getHeight()).build("blazebolt"));
    public static final RegistrySupplier<EntityType<SingularityEntity>> SINGULARITY_ENTITY = register("singularity", () -> EntityType.Builder.<SingularityEntity>of(SingularityEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).build("singularity"));

    public static <T extends Entity> RegistrySupplier<EntityType<T>> register(String name, Supplier<EntityType<T>> entityType) {
        return ENTITIES.register(name, entityType);
    }

    public static void initialize() {
        ENTITIES.register();
    }
}
