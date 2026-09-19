package me.anticode.ascendant_arcana.client.render.types;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AArcanaRenderTypes {
    public static final Function<ResourceLocation, RenderType> EMISSIVE_BACKFACE_CULL = (TEXTURE) -> RenderType.create(
            "emissive_backface_culling",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            RenderType.CompositeState.builder().setShaderState(RenderStateShard.ShaderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(TEXTURE, false, false))
                    .setTransparencyState(RenderStateShard.GLINT_TRANSPARENCY)
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setOverlayState(RenderStateShard.NO_OVERLAY)
                    .setCullState(RenderStateShard.CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));

    public static final Function<ResourceLocation, RenderType> EMISSIVE = (TEXTURE) -> RenderType.create(
            "emissive",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            RenderType.CompositeState.builder().setShaderState(RenderStateShard.ShaderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(TEXTURE, false, false))
                    .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setOverlayState(RenderStateShard.NO_OVERLAY)
                    .setCullState(RenderStateShard.CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .createCompositeState(true));

    public static final RenderType LIGHTNING = RenderType.create(
            "lightning",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder().setShaderState(RenderType.RENDERTYPE_LIGHTNING_SHADER)
                    .setWriteMaskState(RenderType.COLOR_DEPTH_WRITE)
                    .setTransparencyState(RenderType.LIGHTNING_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setOutputState(RenderType.WEATHER_TARGET)
                    .createCompositeState(false));

    public static RenderType emissiveBackfaceCull(ResourceLocation texture) {
        return EMISSIVE_BACKFACE_CULL.apply(texture);
    }

    public static RenderType emissive(ResourceLocation texture) {
        return EMISSIVE.apply(texture);
    }

    public static RenderType lightning() {
        return LIGHTNING;
    }
}
