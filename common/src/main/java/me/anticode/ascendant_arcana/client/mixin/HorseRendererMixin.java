package me.anticode.ascendant_arcana.client.mixin;

import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HorseRenderer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.world.entity.animal.horse.Horse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseRenderer.class)
public abstract class HorseRendererMixin extends AbstractHorseRenderer<Horse, HorseModel<Horse>> {
    public HorseRendererMixin(EntityRendererProvider.Context context, HorseModel<Horse> horseModel, float f) {
        super(context, horseModel, f);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addSpinAttackEffectLayer(EntityRendererProvider.Context context, CallbackInfo ci) {
        this.addLayer(new SpinAttackEffectLayer(this, context.getModelSet()));
    }
}
