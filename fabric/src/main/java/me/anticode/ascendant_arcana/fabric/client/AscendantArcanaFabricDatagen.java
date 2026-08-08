package me.anticode.ascendant_arcana.fabric.client;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.init.*;
import me.anticode.ascendant_arcana.recipe.IngredientStack;
import me.anticode.ascendant_arcana.relics.RelicTypes;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.*;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AscendantArcanaFabricDatagen implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(AAItemTagProvider::new);
        pack.addProvider(AABlockTagProvider::new);
        pack.addProvider(AAModelProvider::new);
        pack.addProvider(AALanguageProvider::new);
        pack.addProvider(AARecipeProvider::new);
        pack.addProvider(AABlockLootTableProvider::new);
    }

    public static class AAItemTagProvider extends FabricTagProvider<Item> {

        public AAItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, Registries.ITEM, registriesFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider wrapperLookup) {
            tag(AArcanaTags.Items.RELICS)
                    .add(AArcanaItems.RELIC.getKey());
            tag(AArcanaTags.Items.HEARTS)
                    .add(AArcanaItems.WARDEN_HEART.getKey());
        }
    }

    public static class AABlockTagProvider extends FabricTagProvider<Block> {

        public AABlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, Registries.BLOCK, registriesFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider arg) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .add(ResourceKey.create(Registries.BLOCK, AArcanaBlocks.SMALL_RESTORINE_BUD.getId()))
                    .add(ResourceKey.create(Registries.BLOCK, AArcanaBlocks.MEDIUM_RESTORINE_BUD.getId()))
                    .add(ResourceKey.create(Registries.BLOCK, AArcanaBlocks.LARGE_RESTORINE_BUD.getId()))
                    .add(ResourceKey.create(Registries.BLOCK, AArcanaBlocks.RESTORINE_CLUSTER.getId()))
                    .add(ResourceKey.create(Registries.BLOCK, AArcanaBlocks.MASSIVE_RESTORINE_CLUSTER.getId()))
                    .add(ResourceKey.create(Registries.BLOCK, AArcanaBlocks.BUDDING_RESTORINE.getId()))
                    .add(ResourceKey.create(Registries.BLOCK, AArcanaBlocks.COPPER_ENCHANTING_TABLE.getId()));
            tag(BlockTags.ENCHANTMENT_POWER_PROVIDER)
                    .add(ResourceKey.create(Registries.BLOCK, BuiltInRegistries.BLOCK.getKey(Blocks.CHISELED_BOOKSHELF)));
        }
    }

    public static class AAModelProvider extends FabricModelProvider {
        public AAModelProvider(FabricDataOutput output) {
            super(output);
        }

        @Override
        public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {

            blockStateModelGenerator.family(AArcanaBlocks.BUDDING_RESTORINE.get());

            blockStateModelGenerator.createGenericCube(AArcanaBlocks.RESTORINE_BLOCK.get());

            // Restorine Clusters
            blockStateModelGenerator.createAmethystCluster(AArcanaBlocks.SMALL_RESTORINE_BUD.get());
            blockStateModelGenerator.createSimpleFlatItemModel(AArcanaBlocks.SMALL_RESTORINE_BUD.get());

            blockStateModelGenerator.createAmethystCluster(AArcanaBlocks.MEDIUM_RESTORINE_BUD.get());
            blockStateModelGenerator.createSimpleFlatItemModel(AArcanaBlocks.MEDIUM_RESTORINE_BUD.get());

            blockStateModelGenerator.createAmethystCluster(AArcanaBlocks.LARGE_RESTORINE_BUD.get());
            blockStateModelGenerator.createSimpleFlatItemModel(AArcanaBlocks.LARGE_RESTORINE_BUD.get());

            blockStateModelGenerator.createAmethystCluster(AArcanaBlocks.RESTORINE_CLUSTER.get());
            blockStateModelGenerator.createSimpleFlatItemModel(AArcanaBlocks.RESTORINE_CLUSTER.get());

            blockStateModelGenerator.createAmethystCluster(AArcanaBlocks.MASSIVE_RESTORINE_CLUSTER.get());
            blockStateModelGenerator.createSimpleFlatItemModel(AArcanaBlocks.MASSIVE_RESTORINE_CLUSTER.get());

            blockStateModelGenerator.blockStateOutput.accept(MultiVariantGenerator.multiVariant(AArcanaBlocks.CRYSTALIZED_LAVAL_BLOCK.get()).with(PropertyDispatch.property(BlockStateProperties.AGE_3).select(0, Variant.variant().with(VariantProperties.MODEL, blockStateModelGenerator.createSuffixedVariant(AArcanaBlocks.CRYSTALIZED_LAVAL_BLOCK.get(), "_0", ModelTemplates.CUBE_ALL, TextureMapping::cube))).select(1, Variant.variant().with(VariantProperties.MODEL, blockStateModelGenerator.createSuffixedVariant(AArcanaBlocks.CRYSTALIZED_LAVAL_BLOCK.get(), "_1", ModelTemplates.CUBE_ALL, TextureMapping::cube))).select(2, Variant.variant().with(VariantProperties.MODEL, blockStateModelGenerator.createSuffixedVariant(AArcanaBlocks.CRYSTALIZED_LAVAL_BLOCK.get(), "_2", ModelTemplates.CUBE_ALL, TextureMapping::cube))).select(3, Variant.variant().with(VariantProperties.MODEL, blockStateModelGenerator.createSuffixedVariant(AArcanaBlocks.CRYSTALIZED_LAVAL_BLOCK.get(), "_3", ModelTemplates.CUBE_ALL, TextureMapping::cube)))));
        }

        @Override
        public void generateItemModels(ItemModelGenerators itemModelGenerator) {
            itemModelGenerator.generateFlatItem(AArcanaItems.INFUSION_SMITHING_TEMPLATE.get(), ModelTemplates.FLAT_ITEM);

            itemModelGenerator.generateFlatItem(AArcanaItems.ENCHANTED_SCRAP.get(), ModelTemplates.FLAT_ITEM);
            itemModelGenerator.generateFlatItem(AArcanaItems.RESTORINE.get(), ModelTemplates.FLAT_ITEM);
            itemModelGenerator.generateFlatItem(AArcanaItems.WARDEN_HEART.get(), ModelTemplates.FLAT_ITEM);

            // Relics
            // We use model predicates here to change the relic texture on the fly while only using one item.
            // Minecraft doesn't provide us a way to do this easily, so we have to build the JSON file manually.
            JsonObject rootJsonObject = new JsonObject();
            rootJsonObject.addProperty("parent", "minecraft:item/generated");
            JsonObject textureJsonObject = new JsonObject();
            textureJsonObject.addProperty("layer0", "ascendant_arcana:item/relic_magic_1");
            rootJsonObject.add("textures", textureJsonObject);
            JsonArray jsonArray = new JsonArray();
            for (int i = 0; i < 5 * 5; i++) {
                int relicId = Mth.floor((double) i / 5);
                int strength = i + 1 - (relicId * 5);
                String relicName = switch(relicId) {
                    case 0 -> "damage";
                    case 1 -> "durability";
                    case 2 -> "protection";
                    case 3 -> "haste";
                    case 4 -> "enchantment_capacity";
                    default -> "error";
                };
                JsonObject overrideObject = new JsonObject();
                overrideObject.addProperty("model", AscendantArcana.MOD_ID + ":item/relics/relic_" + relicName + "_" + strength);
                JsonObject predicateObject = new JsonObject();
                predicateObject.addProperty(relicName, 1);
                predicateObject.addProperty("relic_strength", strength / 5F);
                overrideObject.add("predicate", predicateObject);
                jsonArray.add(overrideObject);

                ModelTemplates.FLAT_ITEM.create(new ResourceLocation(AscendantArcana.MOD_ID, "item/relics/relic_" + relicName + "_" + strength), TextureMapping.layer0(new ResourceLocation(AscendantArcana.MOD_ID, "item/relic_" + relicName + "_" + strength)), itemModelGenerator.output);
            }
            rootJsonObject.add("overrides", jsonArray);
            ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(AArcanaItems.RELIC.get()), TextureMapping.layer0(AArcanaItems.RELIC.get()), itemModelGenerator.output, (id, textures) -> rootJsonObject);
        }
    }

    public static class AALanguageProvider extends FabricLanguageProvider {

        protected AALanguageProvider(FabricDataOutput dataOutput) {
            super(dataOutput);
        }

        private void registerEnchantment(TranslationBuilder translationBuilder, Enchantment enchantment, String name, String description) {
            translationBuilder.add(enchantment, name);
            description(translationBuilder, enchantment, description);
        }

        private void registerStatusEffect(TranslationBuilder translationBuilder, MobEffect statusEffect, String name, String description) {
            translationBuilder.add(statusEffect, name);
            translationBuilder.add(statusEffect.getDescriptionId() + ".description", description);
        }

        public <T> void registerTag(TranslationBuilder translationBuilder, TagKey<T> tag, String description) {
            translationBuilder.add("tag." + tag.location().toLanguageKey(), description);
        }

        private void description(TranslationBuilder translationBuilder, Item item, String description) {
            translationBuilder.add(item.getDescriptionId() + ".description", description);
        }

        private void description(TranslationBuilder translationBuilder, Enchantment enchantment, String description) {
            translationBuilder.add(enchantment.getDescriptionId() + ".description", description);
        }

        @Override
        public void generateTranslations(TranslationBuilder translationBuilder) {
            // Smithing Templates
            String template_id = AArcanaItems.INFUSION_SMITHING_TEMPLATE.getId().getPath();
            translationBuilder.add("item." + AscendantArcana.MOD_ID + ".smithing_template." + template_id + ".applies_to", "Armor, Tools, and Weapons");
            translationBuilder.add("item." + AscendantArcana.MOD_ID + ".smithing_template." + template_id + ".ingredients", "Relics");
            translationBuilder.add("item." + AscendantArcana.MOD_ID + ".smithing_template." + template_id + ".title", "Infusion");
            translationBuilder.add("item." + AscendantArcana.MOD_ID + ".smithing_template." + template_id + ".base_slot_description", "Add Gear");
            translationBuilder.add("item." + AscendantArcana.MOD_ID + ".smithing_template." + template_id + ".additions_slot_description", "Add Relic");
            // Items
            translationBuilder.add(AArcanaItems.ENCHANTED_SCRAP.get(), "Enchanted Scrap");
            description(translationBuilder, AArcanaItems.ENCHANTED_SCRAP.get(), "An item made from the scrap of enchanted items and materials, used to make more enchantments.");
            translationBuilder.add(AArcanaItems.RESTORINE.get(), "Restorine");
            description(translationBuilder, AArcanaItems.RESTORINE.get(), "An item that acts as the universal repair ingredient.");
            translationBuilder.add(AArcanaItems.WARDEN_HEART.get(), "Warden Heart");
            description(translationBuilder, AArcanaItems.WARDEN_HEART.get(), "One of the Warden's many hearts, salvaged in whole and useful for enchanting.");
            // Relics
            translationBuilder.add(AArcanaItems.RELIC.get(), "%1$s Relic of %2$s");
            description(translationBuilder, AArcanaItems.RELIC.get(), "An item that can be infused into tools or armor at a Smithing Table, increasing their potential.");
            translationBuilder.add("item.relics.empty", "Empty Relic");
            translationBuilder.add("item.relics.unknown", "Unknown Relic");

            translationBuilder.add("item.relics.strength.1", "Dormant");
            translationBuilder.add("item.relics.strength.2", "Stirring");
            translationBuilder.add("item.relics.strength.3", "Waking");
            translationBuilder.add("item.relics.strength.4", "Awakened");
            translationBuilder.add("item.relics.strength.5", "Ascendant");

            translationBuilder.add("item.ascendant_arcana.relics.type.damage", "Damage");
            translationBuilder.add("item.ascendant_arcana.relics.type.durability", "Durability");
            translationBuilder.add("item.ascendant_arcana.relics.type.protection", "Protection");
            translationBuilder.add("item.ascendant_arcana.relics.type.haste", "Swiftness");
            translationBuilder.add("item.ascendant_arcana.relics.type.enchantment_capacity", "Enchantment Capacity");

            translationBuilder.add("item.ascendant_arcana.relics.name.damage", "Violence");
            translationBuilder.add("item.ascendant_arcana.relics.name.durability", "Immutability");
            translationBuilder.add("item.ascendant_arcana.relics.name.protection", "Shielding");
            translationBuilder.add("item.ascendant_arcana.relics.name.haste", "Haste");
            translationBuilder.add("item.ascendant_arcana.relics.name.enchantment_capacity", "Magic");

            translationBuilder.add("item.relics.tooltip", "+%1$s%3$s %2$s");
            translationBuilder.add("item.relics.tooltip.applied_any", "When Applied to Item:");
            translationBuilder.add("item.relics.tooltip.applied_tool", "When Applied to Tool:");
            translationBuilder.add("item.relics.tooltip.applied_armor", "When Applied to Armor:");
            translationBuilder.add("item.relics.tooltip.on_tool", "Infused Relics (%1$s/%2$s):");
            // Blocks
            translationBuilder.add(AArcanaBlocks.BUDDING_RESTORINE.get(), "Budding Restorine");
            translationBuilder.add(AArcanaBlocks.COPPER_ENCHANTING_TABLE.get(), "Copper Enchanting Table");
            // Restorine Clusters
            translationBuilder.add(AArcanaBlocks.SMALL_RESTORINE_BUD.get(), "Small Restorine Bud");
            translationBuilder.add(AArcanaBlocks.MEDIUM_RESTORINE_BUD.get(), "Medium Restorine Bud");
            translationBuilder.add(AArcanaBlocks.LARGE_RESTORINE_BUD.get(), "Large Restorine Bud");
            translationBuilder.add(AArcanaBlocks.RESTORINE_CLUSTER.get(), "Restorine Cluster");
            translationBuilder.add(AArcanaBlocks.MASSIVE_RESTORINE_CLUSTER.get(), "Massive Restorine Cluster");
            // Attributes
            translationBuilder.add(AArcanaAttributes.PROTECTION.get(), "Protection");
            translationBuilder.add(AArcanaAttributes.DAMAGE_TAKEN.get(), "Damage Taken");
            // Enchantments
            registerEnchantment(translationBuilder, AArcanaEnchantments.ARCHERS_GAMBIT.get(), "Archer's Gambit", "Briefly increased draw speed after consecutively hitting a target. Stacks 3 times.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.ALCHEMISTS_HEART.get(), "Alchemist's Heart", "Increases the amplifier of all beneficial status effects.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.AMBUSH.get(), "Ambush", "When you hit a mob after throwing this Trident, teleport to it.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.BLADEHEART.get(), "Bladeheart", "Slightly increases all damage dealt by physical attacks.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.COLDHEART.get(), "Coldheart", "Increases damage dealt by all cold attacks.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.CLEANSE.get(), "Cleanse", "Holding the shield up cleanses you of all status effects after a short time.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.CROSS_COUNTER.get(), "Cross Counter", "Blocking an attack immediately after raising the shield grants a brief damage bonus for the next attack.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.DEBILITATING_CHAIN.get(), "Debilitating Chain", "Slaying a mob transfers all status effects to the nearest enemy.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.DEFLECT.get(), "Deflect", "Blocking a projectile with your shield will shoot it back.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.EVOKERS_WRATH.get(), "Evoker's Wrath", "Summons an Evoker Fang when the arrow lands.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.HELLWALKER.get(), "Hellwalker", "Crystalizes nearby lava so it can be walked on.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.HOBBLING_SHOT.get(), "Hobbling Shot", "Reduces movement speed and jump height, stacking 5 times.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.LIFETIDE.get(), "Lifetide", "On hit, sticks into the target and heals them for a short duration. You heal half as much.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.NETHER_HEART.get(), "Heart of the Nether", "Increases damage dealt by all fire attacks.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.PINCUSHION.get(), "Pincushion", "Reduced base damage, increases damage dealt based on the number of arrows stuck in the target.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.PROTECTIVE_ECHO.get(), "Protective Echo", "Instances of high damage are spread out over time.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.REJUVENATING_SHOT.get(), "Rejuvenating Shot", "Instead of doing damage, arrows heal for half the damage they would have done.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.RICOCHET.get(), "Ricochet", "Arrows ricochet, dealing reduced initial damage but increasing with each bounce.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.SMELTING.get(), "Smelting", "Smelts blocks mined.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.SONIC_BLAST.get(), "Sonic Blast", "Holding up the shield charges a powerful sonic blast that ignores most forms of protection.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.SOUL_BURST.get(), "Soul Burst", "Slain enemies deal damage to nearby entities based on their maximum health.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.STORM_HEART.get(), "Heart of the Storm", "Increases the damage dealt by all lightning attacks.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.STRAFE.get(), "Strafe", "Allows you to sprint in any direction and reduces movement speed penalties while using an item.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.SUNDERING.get(), "Sundering", "On hit, sticks into the target and deals damage over time, reducing their armor.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.SUREFOOT.get(), "Surefoot", "Significantly increases knockback resistance and reduces the strength of most slowing effects.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.TURTLE_HEART.get(), "Heart of the Turtle", "Decreases all incoming and outgoing damage.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.WITCH_HEART.get(), "Witch's Heart", "Slightly increases damage dealt by all magic attacks.");

            registerEnchantment(translationBuilder, AArcanaEnchantments.DEPTHS_CURSE.get(), "Curse of the Depths", "Drags you to the bottom of a body of water.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.ENFEEBLEMENT_CURSE.get(), "Curse of Enfeeblement", "Reduces your maximum health.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.INACCURACY_CURSE.get(), "Curse of Inaccuracy", "Reduces the accuracy of bows and crossbows.");
            // Tooltips
            translationBuilder.add("item.book_contains_treasure_title", "Treasure Enchantment");
            translationBuilder.add("item.book_contains_treasure_body", "Store in Chiseled Bookshelves to unlock!");
            translationBuilder.add("item.enchantment_capacity", "Enchantment Capacity: %1$s/%2$s");
            // Enchantment UI stuff
            translationBuilder.add("gui.enchanting.level", "Level %1$s/%2$s");
            translationBuilder.add("gui.enchanting.max_level", "MAX LEVEL");
            translationBuilder.add("gui.enchanting.max_capacity", "This item cannot support more enchantments!");
            translationBuilder.add("gui.enchanting.low_level", "This Enchanting Table does not have enough power to decrypt this enchantment.");
            translationBuilder.add("gui.enchanting.treasure", "This enchantment is too exotic to decrypt without a copy nearby.");
            translationBuilder.add("gui.enchanting.no_item_title", "Ascendant Arcana");
            translationBuilder.add("gui.enchanting.no_item_body", "Enchanting has been completely overhauled. Each item has an enchantment capacity which cannot be exceeded and each enchantment requires various ingredients.\n\nTreasure enchantments can be unlocked by placing them in a nearby Chiseled Bookshelf.");
            translationBuilder.add("gui.enchanting.no_selection_body", "Select an enchantment and place the ingredients below. Capacity can be increased with relics.");
            translationBuilder.add("gui.enchanting.item_cost", "x%1$s %2$s");
            translationBuilder.add("gui.enchanting.enchant", "Enchant!");
            // Status Effects
            registerStatusEffect(translationBuilder, AArcanaMobEffects.ARCHERS_GAMBIT.get(), "Archer's Gambit", "Faster draw speed of bows and crossbows.");
            registerStatusEffect(translationBuilder, AArcanaMobEffects.ECHOING_DAMAGE.get(), "Echoing Damage", "Deals damage every second based on amplification.");
            registerStatusEffect(translationBuilder, AArcanaMobEffects.HOBBLED.get(), "Hobbled", "Slightly reduces movement speed and jump height.");
            registerStatusEffect(translationBuilder, AArcanaMobEffects.SUNDERED.get(), "Sundered", "Significantly reduces armor and armor toughness.");
            registerStatusEffect(translationBuilder, AArcanaMobEffects.CROSS_COUNTER.get(), "Cross Counter", "Increases attack damage for the next attack.");
            // Tags
            registerTag(translationBuilder, AArcanaTags.Items.HEARTS, "Warden Hearts");
            registerTag(translationBuilder, AArcanaTags.Items.RELICS, "Relics");
            registerTag(translationBuilder, AArcanaTags.Blocks.ENCHANTING_TABLES, "Enchanting Tables");
            // Emi
            translationBuilder.add("emi.category.ascendant_arcana.enchanting", "Enchanting");
            translationBuilder.add("gui.emi.ascendant_arcana.enchanting_power", "Required Enchanting Table power");
            translationBuilder.add("gui.emi.ascendant_arcana.level_cost", "XP Level Cost");
            translationBuilder.add("gui.emi.ascendant_arcana.capacity_cost", "Required Enchantment Capacity");
        }
    }

    public static class AARecipeProvider extends FabricRecipeProvider {
        public AARecipeProvider(FabricDataOutput output) {
            super(output);
        }

        public static class EnchantmentRecipeProvider implements FinishedRecipe {
            private final ResourceLocation id;
            private final Enchantment enchantment;
            private int magicalScrapCost;
            private IngredientStack primaryIngredient;
            private IngredientStack secondaryIngredient;
            private int levelCost;

            EnchantmentRecipeProvider(Enchantment enchantment) {
                ResourceLocation enchantmentId = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
                assert enchantmentId != null;
                this.id = enchantmentId.withPrefix("enchantments/");
                this.enchantment = enchantment;
            }

            EnchantmentRecipeProvider(Enchantment enchantment, int magicalScrapCost, IngredientStack primaryIngredient, IngredientStack secondaryIngredient, int levelCost) {
                ResourceLocation enchantmentId = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
                assert enchantmentId != null;
                this.id = enchantmentId.withPrefix("enchantments/");
                this.enchantment = enchantment;
                this.magicalScrapCost = magicalScrapCost;
                this.primaryIngredient = primaryIngredient;
                this.secondaryIngredient = secondaryIngredient;
                this.levelCost = levelCost;
            }

            public EnchantmentRecipeProvider scrap(int count) {
                this.magicalScrapCost = count;
                return this;
            }

            public EnchantmentRecipeProvider primary(ItemLike itemProvider, int count) {
                this.primaryIngredient = new IngredientStack(Ingredient.of(itemProvider), count);
                return this;
            }

            public EnchantmentRecipeProvider primary(TagKey<Item> items, int count) {
                this.primaryIngredient = new IngredientStack(Ingredient.of(items), count);
                return this;
            }

            public EnchantmentRecipeProvider secondary(ItemLike itemProvider, int count) {
                this.secondaryIngredient = new IngredientStack(Ingredient.of(itemProvider), count);
                return this;
            }

            public EnchantmentRecipeProvider secondary(TagKey<Item> items, int count) {
                this.secondaryIngredient = new IngredientStack(Ingredient.of(items), count);
                return this;
            }

            public EnchantmentRecipeProvider level(int levels) {
                this.levelCost = levels;
                return this;
            }

            @Override
            public void serializeRecipeData(JsonObject json) {
                ResourceLocation enchantmentIdentifier = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
                if (enchantmentIdentifier == null) return;
                String enchantmentId = enchantmentIdentifier.toString();

                if (magicalScrapCost != 0) json.addProperty("magical_scrap_cost", magicalScrapCost);
                else  json.addProperty("magical_scrap_cost", 3);
                if (primaryIngredient != null) json.add("primary_ingredient", primaryIngredient.toJson());
                if (secondaryIngredient != null) json.add("secondary_ingredient", secondaryIngredient.toJson());
                if (levelCost != 0) json.addProperty("level_cost", levelCost);
                json.addProperty("enchantment", enchantmentId);
            }

            @Override
            public ResourceLocation getId() {
                return id;
            }

            @Override
            public RecipeSerializer<?> getType() {
                return AArcanaRecipes.ENCHANTMENT_RECIPE_SERIALIZER.get();
            }

            @Override
            public @Nullable JsonObject serializeAdvancement() {
                return null;
            }

            @Override
            public @Nullable ResourceLocation getAdvancementId() {
                return null;
            }
        }

        public static class RelicRecipeJsonBuilder extends CraftingRecipeBuilder implements RecipeBuilder {
            private final RecipeCategory category;
            private final int strength;
            private final ResourceLocation relic;
            private final List<Ingredient> inputs = Lists.newArrayList();
            private final Advancement.Builder advancementBuilder = Advancement.Builder.recipeAdvancement();
            @Nullable
            private String group;

            public RelicRecipeJsonBuilder(RecipeCategory category, int strength, ResourceLocation relic) {
                this.category = category;
                this.strength = strength;
                this.relic = relic;
            }

            public static RelicRecipeJsonBuilder create(RecipeCategory category, int strength, ResourceLocation relic) {
                return new RelicRecipeJsonBuilder(category, strength, relic);
            }

            public RelicRecipeJsonBuilder input(TagKey<Item> tag) {
                return this.input(Ingredient.of(tag));
            }

            public RelicRecipeJsonBuilder input(ItemLike itemProvider) {
                return this.input(itemProvider, 1);
            }

            public RelicRecipeJsonBuilder input(ItemLike itemProvider, int size) {
                for(int i = 0; i < size; ++i) {
                    this.input(Ingredient.of(itemProvider));
                }

                return this;
            }

            public RelicRecipeJsonBuilder input(Ingredient ingredient) {
                return this.input(ingredient, 1);
            }

            public RelicRecipeJsonBuilder input(Ingredient ingredient, int size) {
                for(int i = 0; i < size; ++i) {
                    this.inputs.add(ingredient);
                }

                return this;
            }

            public RelicRecipeJsonBuilder unlockedBy(String string, CriterionTriggerInstance criterionConditions) {
                this.advancementBuilder.addCriterion(string, criterionConditions);
                return this;
            }

            public RelicRecipeJsonBuilder group(@Nullable String string) {
                this.group = string;
                return this;
            }

            @Override
            public Item getResult() {
                return AArcanaItems.RELIC.get();
            }

            public void save(Consumer<FinishedRecipe> exporter) {
                save(exporter, new ResourceLocation(AscendantArcana.MOD_ID, BuiltInRegistries.ITEM.getKey(getResult()).getPath() + "_" + relic.getPath() + "_" + strength));
            }

            public void save(Consumer<FinishedRecipe> exporter, ResourceLocation recipeId) {
                this.validate(recipeId);
                this.advancementBuilder.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId)).rewards(net.minecraft.advancements.AdvancementRewards.Builder.recipe(recipeId)).requirements(RequirementsStrategy.OR);
                exporter.accept(new RelicRecipeJsonBuilder.RelicRecipeJsonProvider(recipeId, this.strength, this.relic, this.group == null ? "" : this.group, determineBookCategory(this.category), this.inputs, this.advancementBuilder, recipeId.withPrefix("recipes/" + this.category.getFolderName() + "/")));
            }

            private void validate(ResourceLocation recipeId) {
                if (this.advancementBuilder.getCriteria().isEmpty()) {
                    throw new IllegalStateException("No way of obtaining recipe " + recipeId);
                }
            }

            public static class RelicRecipeJsonProvider extends CraftingRecipeBuilder.CraftingResult {
                private final ResourceLocation recipeId;
                private final int strength;
                private final ResourceLocation relic;
                private final String group;
                private final List<Ingredient> inputs;
                private final Advancement.Builder advancementBuilder;
                private final ResourceLocation advancementId;

                public RelicRecipeJsonProvider(ResourceLocation recipeId, int strength, ResourceLocation relic, String group, CraftingBookCategory craftingCategory, List<Ingredient> inputs, Advancement.Builder advancementBuilder, ResourceLocation advancementId) {
                    super(craftingCategory);
                    this.recipeId = recipeId;
                    this.strength = strength;
                    this.relic = relic;
                    this.group = group;
                    this.inputs = inputs;
                    this.advancementBuilder = advancementBuilder;
                    this.advancementId = advancementId;
                }

                public void serializeRecipeData(JsonObject json) {
                    super.serializeRecipeData(json);
                    if (!this.group.isEmpty()) {
                        json.addProperty("group", this.group);
                    }

                    JsonArray jsonArray = new JsonArray();

                    for(Ingredient ingredient : this.inputs) {
                        jsonArray.add(ingredient.toJson());
                    }

                    json.add("ingredients", jsonArray);
                    json.addProperty("strength", this.strength);
                    json.addProperty("relic", this.relic.toString());
                }

                public RecipeSerializer<?> getType() {
                    return AArcanaRecipes.RELIC_CRAFTING_RECIPE_SERIALIZER.get();
                }

                public ResourceLocation getId() {
                    return this.recipeId;
                }

                @Nullable
                public JsonObject serializeAdvancement() {
                    return this.advancementBuilder.serializeToJson();
                }

                @Nullable
                public ResourceLocation getAdvancementId() {
                    return this.advancementId;
                }
            }
        }


        @Override
        public void buildRecipes(Consumer<FinishedRecipe> exporter) {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, AArcanaItems.ENCHANTED_SCRAP.get())
                    .requires(Items.LAPIS_LAZULI, 2)
                    .requires(Items.GOLD_NUGGET, 5)
                    .requires(Items.AMETHYST_SHARD, 2)
                    .unlockedBy("obtain_lapis", InventoryChangeTrigger.TriggerInstance.hasItems(Items.LAPIS_LAZULI))
                    .save(exporter);

            // DAMAGE RELICS
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 1, RelicTypes.DAMAGE)
                    .input(Items.AMETHYST_SHARD)
                    .input(Items.IRON_INGOT)
                    .unlockedBy("obtain_amethyst", InventoryChangeTrigger.TriggerInstance.hasItems(Items.AMETHYST_SHARD))
                    .save(exporter);
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 2, RelicTypes.DAMAGE)
                    .input(AArcanaItems.RELIC.get())
                    .input(Items.IRON_INGOT, 3)
                    .input(Items.HONEYCOMB)
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter);
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 2, RelicTypes.DAMAGE)
                    .input(AArcanaItems.RELIC.get())
                    .input(AArcanaItems.RELIC.get())
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter, new ResourceLocation(AscendantArcana.MOD_ID, "relic_damage_combine_0"));
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 3, RelicTypes.DAMAGE)
                    .input(AArcanaItems.RELIC.get())
                    .input(Items.TORCHFLOWER_SEEDS, 2)
                    .input(Items.BONE, 2)
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter);
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 3, RelicTypes.DAMAGE)
                    .input(AArcanaItems.RELIC.get())
                    .input(AArcanaItems.RELIC.get())
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter, new ResourceLocation(AscendantArcana.MOD_ID, "relic_damage_combine_1"));
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 4, RelicTypes.DAMAGE)
                    .input(AArcanaItems.RELIC.get())
                    .input(Items.DIAMOND)
                    .input(Items.BLAZE_POWDER)
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter);
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 5, RelicTypes.DAMAGE)
                    .input(AArcanaItems.RELIC.get())
                    .input(AArcanaItems.RELIC.get())
                    .input(Items.DIAMOND)
                    .input(Items.BLAZE_POWDER)
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter);

            // DURABILITY RELICS
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 1, RelicTypes.DURABILITY)
                    .input(AArcanaItems.RESTORINE.get())
                    .input(Items.AMETHYST_SHARD)
                    .unlockedBy("obtain_amethyst", InventoryChangeTrigger.TriggerInstance.hasItems(Items.AMETHYST_SHARD))
                    .save(exporter);
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 2, RelicTypes.DURABILITY)
                    .input(AArcanaItems.RELIC.get())
                    .input(AArcanaItems.RESTORINE.get(), 3)
                    .input(Items.HONEYCOMB)
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter);
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 2, RelicTypes.DURABILITY)
                    .input(AArcanaItems.RELIC.get())
                    .input(AArcanaItems.RELIC.get())
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter, new ResourceLocation(AscendantArcana.MOD_ID, "relic_durability_combine_0"));
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 3, RelicTypes.DURABILITY)
                    .input(AArcanaItems.RELIC.get())
                    .input(AArcanaItems.RESTORINE.get(), 3)
                    .input(Items.TERRACOTTA, 2)
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter);
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 3, RelicTypes.DURABILITY)
                    .input(AArcanaItems.RELIC.get())
                    .input(AArcanaItems.RELIC.get())
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter, new ResourceLocation(AscendantArcana.MOD_ID, "relic_durability_combine_1"));
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 4, RelicTypes.DURABILITY)
                    .input(AArcanaItems.RELIC.get())
                    .input(Items.DIAMOND)
                    .input(AArcanaItems.RESTORINE.get(), 5)
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter);
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 5, RelicTypes.DURABILITY)
                    .input(AArcanaItems.RELIC.get())
                    .input(AArcanaItems.RELIC.get())
                    .input(Items.DIAMOND)
                    .input(AArcanaItems.RESTORINE.get())
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter);

            // PROTECTION RELICS
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 1, RelicTypes.PROTECTION)
                    .input(Items.AMETHYST_SHARD)
                    .input(Items.CLAY_BALL, 2)
                    .unlockedBy("obtain_amethyst", InventoryChangeTrigger.TriggerInstance.hasItems(Items.AMETHYST_SHARD))
                    .save(exporter);
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 2, RelicTypes.PROTECTION)
                    .input(AArcanaItems.RELIC.get())
                    .input(Items.CLAY_BALL, 6)
                    .input(Items.IRON_NUGGET)
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter);
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 2, RelicTypes.PROTECTION)
                    .input(AArcanaItems.RELIC.get())
                    .input(AArcanaItems.RELIC.get())
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter, new ResourceLocation(AscendantArcana.MOD_ID, "relic_protection_combine_0"));
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 3, RelicTypes.PROTECTION)
                    .input(AArcanaItems.RELIC.get())
                    .input(Items.DIAMOND)
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter);
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 3, RelicTypes.PROTECTION)
                    .input(AArcanaItems.RELIC.get())
                    .input(AArcanaItems.RELIC.get())
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter, new ResourceLocation(AscendantArcana.MOD_ID, "relic_protection_combine_1"));
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 4, RelicTypes.PROTECTION)
                    .input(AArcanaItems.RELIC.get())
                    .input(Items.DIAMOND)
                    .input(Items.NETHERITE_SCRAP)
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter);
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 5, RelicTypes.PROTECTION)
                    .input(AArcanaItems.RELIC.get())
                    .input(AArcanaItems.RELIC.get())
                    .input(Items.DIAMOND)
                    .input(Items.NETHERITE_SCRAP)
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter);

            // HASTE RELICS
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 1, RelicTypes.HASTE)
                    .input(Items.AMETHYST_SHARD)
                    .input(Items.GOLD_NUGGET)
                    .unlockedBy("obtain_amethyst", InventoryChangeTrigger.TriggerInstance.hasItems(Items.AMETHYST_SHARD))
                    .save(exporter);
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 2, RelicTypes.HASTE)
                    .input(AArcanaItems.RELIC.get())
                    .input(Items.GOLD_INGOT, 2)
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter);
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 2, RelicTypes.HASTE)
                    .input(AArcanaItems.RELIC.get())
                    .input(AArcanaItems.RELIC.get())
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter, new ResourceLocation(AscendantArcana.MOD_ID, "relic_haste_combine_0"));
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 3, RelicTypes.HASTE)
                    .input(AArcanaItems.RELIC.get())
                    .input(Items.GOLD_INGOT, 5)
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter);
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 3, RelicTypes.HASTE)
                    .input(AArcanaItems.RELIC.get())
                    .input(AArcanaItems.RELIC.get())
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter, new ResourceLocation(AscendantArcana.MOD_ID, "relic_haste_combine_1"));
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 4, RelicTypes.HASTE)
                    .input(AArcanaItems.RELIC.get())
                    .input(Items.AMETHYST_SHARD, 3)
                    .input(Items.GOLD_INGOT, 3)
                    .input(Items.BLAZE_POWDER)
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter);
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 5, RelicTypes.HASTE)
                    .input(AArcanaItems.RELIC.get())
                    .input(AArcanaItems.RELIC.get())
                    .input(Items.DIAMOND)
                    .input(Items.GOLD_INGOT)
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter);

            // ENCHANTMENT CAPACITY RELICS
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 1, RelicTypes.ENCHANTMENT_CAPACITY)
                    .input(Items.LAPIS_LAZULI)
                    .input(Items.AMETHYST_SHARD)
                    .unlockedBy("obtain_amethyst", InventoryChangeTrigger.TriggerInstance.hasItems(Items.AMETHYST_SHARD))
                    .save(exporter);
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 2, RelicTypes.ENCHANTMENT_CAPACITY)
                    .input(AArcanaItems.RELIC.get())
                    .input(Items.LAPIS_LAZULI, 3)
                    .input(Items.GOLD_NUGGET, 2)
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter);
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 2, RelicTypes.ENCHANTMENT_CAPACITY)
                    .input(AArcanaItems.RELIC.get())
                    .input(AArcanaItems.RELIC.get())
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter, new ResourceLocation(AscendantArcana.MOD_ID, "relic_enchantment_capacity_combine_0"));
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 3, RelicTypes.ENCHANTMENT_CAPACITY)
                    .input(AArcanaItems.RELIC.get())
                    .input(AArcanaItems.ENCHANTED_SCRAP.get(), 5)
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter);
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 3, RelicTypes.ENCHANTMENT_CAPACITY)
                    .input(AArcanaItems.RELIC.get())
                    .input(AArcanaItems.RELIC.get())
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter, new ResourceLocation(AscendantArcana.MOD_ID, "relic_enchantment_capacity_combine_1"));
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 4, RelicTypes.ENCHANTMENT_CAPACITY)
                    .input(AArcanaItems.RELIC.get())
                    .input(Items.DIAMOND)
                    .input(Items.LAPIS_LAZULI, 2)
                    .input(AArcanaItems.ENCHANTED_SCRAP.get(), 5)
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter);
            RelicRecipeJsonBuilder.create(RecipeCategory.MISC, 5, RelicTypes.ENCHANTMENT_CAPACITY)
                    .input(AArcanaItems.RELIC.get())
                    .input(AArcanaItems.RELIC.get())
                    .input(Items.DIAMOND)
                    .input(AArcanaItems.ENCHANTED_SCRAP.get())
                    .unlockedBy("obtain_relic", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RELIC.get()))
                    .save(exporter);

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AArcanaBlocks.COPPER_ENCHANTING_TABLE.get())
                    .define('b', Items.BOOK)
                    .define('c', Items.CUT_COPPER)
                    .define('g', AArcanaItems.RESTORINE.get())
                    .pattern(" b ").pattern("gcg").pattern("ccc")
                    .unlockedBy("obtain_copper", InventoryChangeTrigger.TriggerInstance.hasItems(Items.COPPER_INGOT))
                    .save(exporter);
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AArcanaItems.INFUSION_SMITHING_TEMPLATE.get())
                    .define('c', Blocks.CALCITE)
                    .define('r', AArcanaItems.RESTORINE.get())
                    .pattern(" c ")
                    .pattern("rrr")
                    .pattern("ccc")
                    .unlockedBy("obtain_restorine", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RESTORINE.get()))
                    .save(exporter);

            // Enchantments
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.ARCHERS_GAMBIT.get()).primary(Items.GOLD_INGOT, 3).level(7));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.ALCHEMISTS_HEART.get()).scrap(12).primary(AArcanaTags.Items.HEARTS, 1).secondary(Items.GLISTERING_MELON_SLICE, 16).level(15));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.AMBUSH.get()).primary(Items.ENDER_PEARL, 8).secondary(Items.AMETHYST_SHARD, 3).level(3));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.BLADEHEART.get()).scrap(12).primary(AArcanaTags.Items.HEARTS, 1).secondary(Items.DIAMOND, 8).level(15));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.COLDHEART.get()).scrap(12).primary(AArcanaTags.Items.HEARTS, 1).secondary(Items.BLUE_ICE, 32).level(15));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.CLEANSE.get()).scrap(9).primary(Items.MILK_BUCKET, 1).secondary(Items.PITCHER_PLANT, 4).level(9));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.CROSS_COUNTER.get()).scrap(3).primary(Items.CLOCK, 1).secondary(Items.IRON_NUGGET, 1).level(3));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.DEBILITATING_CHAIN.get()).primary(Items.FERMENTED_SPIDER_EYE, 3).secondary(Items.GUNPOWDER, 3).level(3));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.DEFLECT.get()).scrap(4).primary(Items.SLIME_BALL, 4).secondary(Items.SCUTE, 2).level(5));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.EVOKERS_WRATH.get()).scrap(2).primary(Items.TOTEM_OF_UNDYING, 1).level(3));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.HELLWALKER.get()).scrap(12).primary(Items.BLAZE_ROD, 2).secondary(Items.TORCHFLOWER, 1).level(6));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.HOBBLING_SHOT.get()).scrap(3).primary(Items.VINE, 6).secondary(Items.BONE_MEAL, 6).level(6));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.LIFETIDE.get()).scrap(12).primary(Items.GHAST_TEAR, 6).level(9));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.NETHER_HEART.get()).scrap(12).primary(AArcanaTags.Items.HEARTS, 1).secondary(Items.NETHERITE_INGOT, 2).level(15));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.PINCUSHION.get()).scrap(3).primary(Items.ECHO_SHARD, 1).secondary(Items.IRON_INGOT, 6).level(6));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.PROTECTIVE_ECHO.get()).scrap(6).primary(Items.POPPED_CHORUS_FRUIT, 4).secondary(Items.SCUTE, 2).level(8));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.REJUVENATING_SHOT.get()).scrap(6).primary(Items.GHAST_TEAR, 4).level(6));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.RICOCHET.get()).scrap(6).primary(Items.SLIME_BALL, 23).level(6));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.SMELTING.get()).primary(Items.BLAZE_ROD, 2).level(3));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.SONIC_BLAST.get()).scrap(6).primary(AArcanaTags.Items.HEARTS, 1).secondary(Items.ECHO_SHARD, 6).level(9));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.SOUL_BURST.get()).primary(Items.SCULK_CATALYST, 1).secondary(Items.GUNPOWDER, 12).level(3));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.STORM_HEART.get()).scrap(12).primary(AArcanaTags.Items.HEARTS, 1).secondary(Items.LIGHTNING_ROD, 6).level(15));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.STRAFE.get()).scrap(6).primary(Items.FEATHER, 12).level(4));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.SUNDERING.get()).scrap(4).primary(Items.TORCHFLOWER, 2).secondary(Items.FERMENTED_SPIDER_EYE, 4).level(6));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.SUREFOOT.get()).scrap(6).primary(Items.ANVIL, 1).level(6));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.TURTLE_HEART.get()).scrap(12).primary(AArcanaTags.Items.HEARTS, 1).secondary(Items.ANVIL, 1).level(15));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.WITCH_HEART.get()).scrap(12).primary(AArcanaTags.Items.HEARTS, 1).secondary(Items.CAULDRON, 1).level(15));

            exporter.accept(new EnchantmentRecipeProvider(Enchantments.AQUA_AFFINITY).scrap(3).primary(Items.PRISMARINE_CRYSTALS, 4).level(3));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.CHANNELING).scrap(5).primary(Items.LIGHTNING_ROD, 1).secondary(Items.GOLD_INGOT, 3).level(3));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.DEPTH_STRIDER).scrap(6).primary(Items.SCUTE, 4).level(3));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.FIRE_ASPECT).scrap(6).primary(Items.BLAZE_POWDER, 9).secondary(Items.FLINT, 2).level(3));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.FLAMING_ARROWS).scrap(6).primary(Items.BLAZE_POWDER, 12).secondary(Items.FLINT, 2).level(3));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.BLOCK_FORTUNE).scrap(12).primary(Items.DIAMOND, 3).secondary(Items.ECHO_SHARD, 1).level(3));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.FROST_WALKER).scrap(6).primary(Items.BLUE_ICE, 2).secondary(Items.ECHO_SHARD, 1).level(3));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.FALL_PROTECTION).scrap(2).primary(Items.FEATHER, 5).level(3));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.INFINITY_ARROWS).scrap(12).primary(Items.ECHO_SHARD, 2).secondary(Items.ARROW, 1).level(3));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.THORNS).scrap(3).primary(Items.CACTUS, 2).secondary(Items.SLIME_BALL, 1).level(3));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.KNOCKBACK).scrap(1).primary(Items.PISTON, 2).level(3));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.SOUL_SPEED).scrap(6).primary(Items.SCULK_CATALYST, 1).secondary(Items.FEATHER, 2).level(6));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.SWIFT_SNEAK).scrap(6).primary(Items.FEATHER, 5).secondary(Items.ECHO_SHARD, 1).level(6));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.MOB_LOOTING).scrap(12).primary(Items.ENDER_PEARL, 3).level(3));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.RESPIRATION).scrap(3).primary(Items.GLASS_BOTTLE, 2).secondary(Items.BAMBOO, 1).level(3));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.RIPTIDE).scrap(9).primary(Items.NAUTILUS_SHELL, 2).secondary(Items.PRISMARINE_CRYSTALS, 3).level(9));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.LOYALTY).scrap(4).primary(Items.COPPER_INGOT, 6).secondary(Items.SALMON, 1).level(5));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.SWEEPING_EDGE).scrap(2).primary(Items.AMETHYST_SHARD, 2).level(3));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.PIERCING).scrap(2).primary(Items.STONE, 3).level(3));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.SILK_TOUCH).scrap(4).primary(Items.STRING, 6).level(5));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.PUNCH_ARROWS).scrap(2).primary(Items.PISTON, 2).secondary(Items.REDSTONE, 4).level(4));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.MULTISHOT).scrap(6).primary(Items.ECHO_SHARD, 2).level(3));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.FISHING_SPEED).scrap(1).primary(Items.IRON_NUGGET, 1).secondary(Items.COD, 3).level(3));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.FISHING_LUCK).scrap(4).primary(Items.RABBIT_FOOT, 1).level(3));
        }
    }

    public static class AABlockLootTableProvider extends FabricBlockLootTableProvider {
        protected AABlockLootTableProvider(FabricDataOutput dataOutput) {
            super(dataOutput);
        }

        @Override
        public void generate() {
            // Restorine Clusters
            createSilkTouchOnlyTable(AArcanaBlocks.SMALL_RESTORINE_BUD.get());
            createSilkTouchOnlyTable(AArcanaBlocks.MEDIUM_RESTORINE_BUD.get());
            createSilkTouchOnlyTable(AArcanaBlocks.LARGE_RESTORINE_BUD.get());
            add(AArcanaBlocks.RESTORINE_CLUSTER.get(), (block) -> createSilkTouchDispatchTable(block, (LootItem.lootTableItem(AArcanaItems.RESTORINE.get()).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2))).apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE)).when(MatchTool.toolMatches(net.minecraft.advancements.critereon.ItemPredicate.Builder.item().of(ItemTags.CLUSTER_MAX_HARVESTABLES)))).otherwise(this.applyExplosionDecay(block, LootItem.lootTableItem(AArcanaItems.RESTORINE.get()).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))))));
            add(AArcanaBlocks.MASSIVE_RESTORINE_CLUSTER.get(), (block) -> createSilkTouchDispatchTable(block, (LootItem.lootTableItem(AArcanaItems.RESTORINE.get()).apply(SetItemCountFunction.setCount(UniformGenerator.between(5, 6))).apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE)).when(MatchTool.toolMatches(net.minecraft.advancements.critereon.ItemPredicate.Builder.item().of(ItemTags.CLUSTER_MAX_HARVESTABLES)))).otherwise(this.applyExplosionDecay(block, LootItem.lootTableItem(AArcanaItems.RESTORINE.get()).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 6)))))));
            createSilkTouchOnlyTable(AArcanaBlocks.COPPER_ENCHANTING_TABLE.get());
        }
    }
}
