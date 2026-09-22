package me.anticode.ascendant_arcana.fabric.client;

import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.client.AscendantArcanaClient;
import me.anticode.ascendant_arcana.client.screen.AArcanaEnchantingScreen;
import me.anticode.ascendant_arcana.init.AArcanaBlocks;
import me.anticode.ascendant_arcana.init.AArcanaMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class AscendantArcanaFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AscendantArcanaClient.initialize();

        BlockEntityRenderers.register(AArcanaBlocks.COPPER_ENCHANTING_TABLE_BLOCK_ENTITY.get(), EnchantTableRenderer::new);

        BlockRenderLayerMap.INSTANCE.putBlock(AArcanaBlocks.SMALL_RESTORINE_BUD.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AArcanaBlocks.MEDIUM_RESTORINE_BUD.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AArcanaBlocks.LARGE_RESTORINE_BUD.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AArcanaBlocks.RESTORINE_CLUSTER.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AArcanaBlocks.MASSIVE_RESTORINE_CLUSTER.get(), RenderType.cutout());

        MenuScreens.register(AArcanaMenus.ENCHANTING.get(), AArcanaEnchantingScreen::new);

        ResourceManagerHelper.registerBuiltinResourcePack(
                new ResourceLocation(AscendantArcana.MOD_ID, "ascendant_arcana_classic"),
                FabricLoader.getInstance().getModContainer(AscendantArcana.MOD_ID).get(),
                Component.translatable("pack.ascendant_arcana.ascendant_arcana_classic"),
                ResourcePackActivationType.NORMAL);
    }
}
