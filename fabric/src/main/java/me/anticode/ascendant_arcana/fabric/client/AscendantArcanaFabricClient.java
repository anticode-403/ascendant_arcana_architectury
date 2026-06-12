package me.anticode.ascendant_arcana.fabric.client;

import me.anticode.ascendant_arcana.client.AscendantArcanaClient;
import me.anticode.ascendant_arcana.client.screen.AArcanaEnchantingScreen;
import me.anticode.ascendant_arcana.init.AArcanaBlocks;
import me.anticode.ascendant_arcana.init.AArcanaItems;
import me.anticode.ascendant_arcana.init.AArcanaMenus;
import me.anticode.ascendant_arcana.item.RelicItem;
import me.anticode.ascendant_arcana.logic.Relics;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
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

        ItemProperties.register(AArcanaItems.RELIC.get(), new ResourceLocation("relic_type"), ((itemStack, clientLevel, livingEntity, i) -> Relics.toId(RelicItem.getRelicType(itemStack)) / 5F));
        ItemProperties.register(AArcanaItems.RELIC.get(), new ResourceLocation("relic_strength"), (itemStack, clientWorld, livingEntity, seed) -> RelicItem.getRelicStrength(itemStack) / 5F);

        MenuScreens.register(AArcanaMenus.ENCHANTING.get(), AArcanaEnchantingScreen::new);
    }
}
