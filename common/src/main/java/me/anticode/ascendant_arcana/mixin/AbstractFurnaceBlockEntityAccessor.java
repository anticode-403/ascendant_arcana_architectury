package me.anticode.ascendant_arcana.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceBlockEntityAccessor {
    @Invoker("createExperience")
    static void ascendant_arcana$dropExperience(ServerLevel world, Vec3 pos, int multiplier, float experience) {
        throw new UnsupportedOperationException();
    }
}
