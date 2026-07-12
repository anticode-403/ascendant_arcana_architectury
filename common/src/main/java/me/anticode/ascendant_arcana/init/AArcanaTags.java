package me.anticode.ascendant_arcana.init;

import me.anticode.ascendant_arcana.AscendantArcana;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class AArcanaTags {
    public static class Items {

        public static final TagKey<Item> RELICS = createItemTag("relics");
        public static final TagKey<Item> INFUSION_BLACKLIST = createItemTag("infusion_blacklist");
        public static final TagKey<Item> RESTORINE_BLACKLIST = createItemTag("restorine_blacklist");
        public static final TagKey<Item> HEARTS = createItemTag("heart_items");

        private static TagKey<Item> createItemTag(String name) {
            return TagKey.create(Registries.ITEM, new ResourceLocation(AscendantArcana.MOD_ID, name));
        }
    }

    public static class Blocks {
        public static final TagKey<Block> ENCHANTING_TABLES = createBlockTag("enchanting_tables");

        private static TagKey<Block> createBlockTag(String name) {
            return TagKey.create(Registries.BLOCK, new ResourceLocation(AscendantArcana.MOD_ID, name));
        }
    }

    public static void initialize() {}
}
