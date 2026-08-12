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

import java.util.ArrayList;
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
                    .add(ResourceKey.create(Registries.BLOCK, AArcanaBlocks.COPPER_ENCHANTING_TABLE.getId()))
                    .add(ResourceKey.create(Registries.BLOCK, AArcanaBlocks.RESTORINE_BLOCK.getId()));
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

        private void description(TranslationBuilder translationBuilder, ItemLike item, String description) {
            translationBuilder.add(item.asItem().getDescriptionId() + ".description", description);
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
            description(translationBuilder, AArcanaBlocks.BUDDING_RESTORINE.get(), "A block that generates Restorine Buds over time when exposed to air.");
            translationBuilder.add(AArcanaBlocks.COPPER_ENCHANTING_TABLE.get(), "Copper Enchanting Table");
            description(translationBuilder, AArcanaBlocks.COPPER_ENCHANTING_TABLE.get(), "A block that allows you to enchant items up to Uncommon rarity.");
            translationBuilder.add(AArcanaBlocks.RESTORINE_BLOCK.get(), "Restorine Block");
            description(translationBuilder, AArcanaBlocks.RESTORINE_BLOCK.get(), "A block of compressed Restorine.");
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
            registerEnchantment(translationBuilder, AArcanaEnchantments.AMBUSH.get(), "Ambush", "When you hit a mob after throwing this Trident, teleport to it.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.ALCHEMISTS_HEART.get(), "Alchemist's Heart", "Increases the amplifier of all beneficial status effects.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.ARCHERS_GAMBIT.get(), "Archer's Gambit", "Briefly increased draw speed after consecutively hitting a target. Stacks 3 times.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.BASHING.get(), "Bashing", "While blocking press attack to charge forward and knock an enemy back.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.BLADEHEART.get(), "Bladeheart", "Slightly increases all damage dealt by physical attacks.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.BLAZEBOLT.get(), "Blazebolt", "Changes ammo type to Blaze Rods which fire a piercing, incinerating beam.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.CLEANSE.get(), "Cleanse", "Holding the shield up cleanses you of all status effects after a short time.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.COLDHEART.get(), "Coldheart", "Increases damage dealt by all cold attacks.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.CROSS_COUNTER.get(), "Cross Counter", "Blocking an attack immediately after raising the shield grants a brief damage bonus for the next attack.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.CUSHIONING.get(), "Cushioning", "Reduces damage dealt by colliding with blocks and other surfaces.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.DEBILITATING_CHAIN.get(), "Debilitating Chain", "Slaying a mob transfers all status effects to the nearest enemy.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.DEFLECT.get(), "Deflect", "Blocking a projectile with your shield will shoot it back.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.EVOKERS_WRATH.get(), "Evoker's Wrath", "Summons an Evoker Fang when the arrow lands.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.GUIDING.get(), "Guiding", "While holding this bow, fired arrows follow the direction you're looking.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.HELLWALKER.get(), "Hellwalker", "Crystalizes nearby lava so it can be walked on.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.HOBBLING_SHOT.get(), "Hobbling Shot", "Reduces movement speed and jump height, stacking 5 times.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.LIFETIDE.get(), "Lifetide", "On hit, sticks into the target and heals them for a short duration. You heal half as much.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.MIASMA.get(), "Miasma", "Tipped Arrows create a temporary effect cloud where they land.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.NETHER_HEART.get(), "Heart of the Nether", "Increases damage dealt by all fire attacks.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.PINCUSHION.get(), "Pincushion", "Reduced base damage, increases damage dealt based on the number of arrows stuck in the target.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.PROTECTIVE_ECHO.get(), "Protective Echo", "Instances of high damage are spread out over time.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.REJUVENATING_SHOT.get(), "Rejuvenating Shot", "Instead of doing damage, arrows heal for half the damage they would have done.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.REPEATING.get(), "Repeating", "Crouch to load multiple arrows at once, allowing you to rapidly fire them in sequence later.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.RICOCHET.get(), "Ricochet", "Arrows ricochet, dealing reduced initial damage but increasing with each bounce.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.ROCKETRY.get(), "Rocketry", "Changes ammo type to Firework Rockets, also granting knockback and reduced self damage.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.SALVO.get(), "Salvo", "Crouch to load multiple arrows at once, firing them in a wide blast. Arrows deal reduced base damage.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.SHATTERSHOT.get(), "Shattershot", "Changes ammo type to Amethyst Shards, firing a short-ranged blast of shrapnel that knocks enemies back and applies Sundered.");
            registerEnchantment(translationBuilder, AArcanaEnchantments.SINGULARITY.get(), "Singularity", "Summons a singularity on hit, dealing damage and pulling in nearby enemies after a slight delay.");
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

    public static class AABlockLootTableProvider extends FabricBlockLootTableProvider {
        protected AABlockLootTableProvider(FabricDataOutput dataOutput) {
            super(dataOutput);
        }

        @Override
        public void generate() {
            // Restorine Clusters
            dropWhenSilkTouch(AArcanaBlocks.SMALL_RESTORINE_BUD.get());
            dropWhenSilkTouch(AArcanaBlocks.MEDIUM_RESTORINE_BUD.get());
            dropWhenSilkTouch(AArcanaBlocks.LARGE_RESTORINE_BUD.get());
            add(AArcanaBlocks.RESTORINE_CLUSTER.get(), (block) -> createSilkTouchDispatchTable(block, (LootItem.lootTableItem(AArcanaItems.RESTORINE.get()).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2))).apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE)).when(MatchTool.toolMatches(net.minecraft.advancements.critereon.ItemPredicate.Builder.item().of(ItemTags.CLUSTER_MAX_HARVESTABLES)))).otherwise(this.applyExplosionDecay(block, LootItem.lootTableItem(AArcanaItems.RESTORINE.get()).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))))));
            add(AArcanaBlocks.MASSIVE_RESTORINE_CLUSTER.get(), (block) -> createSilkTouchDispatchTable(block, (LootItem.lootTableItem(AArcanaItems.RESTORINE.get()).apply(SetItemCountFunction.setCount(UniformGenerator.between(5, 6))).apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE)).when(MatchTool.toolMatches(net.minecraft.advancements.critereon.ItemPredicate.Builder.item().of(ItemTags.CLUSTER_MAX_HARVESTABLES)))).otherwise(this.applyExplosionDecay(block, LootItem.lootTableItem(AArcanaItems.RESTORINE.get()).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 6)))))));
            dropSelf(AArcanaBlocks.COPPER_ENCHANTING_TABLE.get());
            dropSelf(AArcanaBlocks.RESTORINE_BLOCK.get());
        }
    }

    public static class AARecipeProvider extends FabricRecipeProvider {
        public AARecipeProvider(FabricDataOutput output) {
            super(output);
        }

        public static class EnchantmentRecipeProvider implements FinishedRecipe {
            private final ResourceLocation id;
            private final Enchantment enchantment;
            private final List<EnchantmentLevelRecipeProvider> levels;

            EnchantmentRecipeProvider(Enchantment enchantment) {
                ResourceLocation enchantmentId = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
                assert enchantmentId != null;
                this.id = enchantmentId.withPrefix("enchantments/");
                this.enchantment = enchantment;
                this.levels = new ArrayList<>();
            }

            EnchantmentRecipeProvider(Enchantment enchantment, List<EnchantmentLevelRecipeProvider> levels, int levelCost) {
                ResourceLocation enchantmentId = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
                assert enchantmentId != null;
                this.id = enchantmentId.withPrefix("enchantments/");
                this.enchantment = enchantment;
                this.levels = levels;
            }

            public record EnchantmentLevelRecipeProvider(IngredientStack scrapStack, IngredientStack primaryIngredient, IngredientStack secondaryIngredient, int levelCost) {
                public void serializeRecipeData(JsonObject json) {
                    if (scrapStack != null) json.add("scrap_stack", scrapStack.toJson());
                    if (primaryIngredient != null) json.add("primary_ingredient", primaryIngredient.toJson());
                    if (secondaryIngredient != null) json.add("secondary_ingredient", secondaryIngredient.toJson());
                    if (levelCost != 0) json.addProperty("level_cost", levelCost);
                }
            }

            public EnchantmentRecipeProvider level(EnchantmentLevelRecipeProvider recipe) {
                this.levels.add(recipe);
                return this;
            }

            public void level(IngredientStack scrapStack, IngredientStack primaryIngredient, IngredientStack secondaryIngredient, int levelCost) {
                this.levels.add(new EnchantmentLevelRecipeProvider(scrapStack, primaryIngredient, secondaryIngredient, levelCost));
            }

            @Override
            public void serializeRecipeData(JsonObject json) {
                ResourceLocation enchantmentIdentifier = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
                if (enchantmentIdentifier == null) return;
                String enchantmentId = enchantmentIdentifier.toString();
                JsonArray jsonArray = new JsonArray();
                for (EnchantmentLevelRecipeProvider recipe : this.levels) {
                    JsonObject levelJson = new JsonObject();
                    recipe.serializeRecipeData(levelJson);
                    jsonArray.add(levelJson);
                }
                json.add("levels", jsonArray);
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
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, AArcanaBlocks.RESTORINE_BLOCK.get())
                    .define('r', AArcanaItems.RESTORINE.get())
                    .pattern("rr")
                    .pattern("rr")
                    .unlockedBy("obtain_restorine", InventoryChangeTrigger.TriggerInstance.hasItems(AArcanaItems.RESTORINE.get()))
                    .save(exporter);

            // Enchantments
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.AMBUSH.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 3),
                            IngredientStack.of(Items.ENDER_PEARL, 6),
                            IngredientStack.of(Items.AMETHYST_SHARD, 3),
                            3)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.ALCHEMISTS_HEART.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 12),
                            IngredientStack.of(AArcanaTags.Items.HEARTS),
                            IngredientStack.of(Items.GLISTERING_MELON_SLICE, 12),
                            3)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.ARCHERS_GAMBIT.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 5),
                            IngredientStack.of(Items.GOLD_INGOT, 3),
                            IngredientStack.of(Items.STRING, 3),
                            6))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 3),
                            IngredientStack.of(Items.GOLD_INGOT, 3),
                            IngredientStack.of(Items.STRING, 2),
                            4))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 3),
                            IngredientStack.of(Items.GOLD_INGOT, 4),
                            IngredientStack.of(Items.FEATHER, 2),
                            4
                    )));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.BASHING.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 3),
                            IngredientStack.of(Items.RABBIT_FOOT),
                            IngredientStack.of(Items.PISTON),
                            3))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 4),
                            IngredientStack.of(Items.RABBIT_FOOT, 2),
                            IngredientStack.of(Items.SLIME_BALL, 2),
                            3))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 3),
                            IngredientStack.of(Items.RABBIT_FOOT, 3),
                            IngredientStack.of(Items.SLIME_BALL, 3),
                            3)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.BLADEHEART.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 12),
                            IngredientStack.of(AArcanaTags.Items.HEARTS),
                            IngredientStack.of(Items.DIAMOND, 8),
                            6)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.BLAZEBOLT.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 6),
                            IngredientStack.of(Items.WITHER_SKELETON_SKULL, 1),
                            IngredientStack.of(Items.GHAST_TEAR, 2),
                            6))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 6),
                            IngredientStack.of(Items.QUARTZ, 18),
                            IngredientStack.of(Items.MAGMA_CREAM, 2),
                            6))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 6),
                            IngredientStack.of(Items.QUARTZ, 32),
                            IngredientStack.of(Items.MAGMA_CREAM, 12),
                            6)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.CLEANSE.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 1),
                            IngredientStack.of(Items.MILK_BUCKET),
                            IngredientStack.of(Items.BIG_DRIPLEAF),
                            3)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.COLDHEART.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 12),
                            IngredientStack.of(AArcanaTags.Items.HEARTS),
                            IngredientStack.of(Items.BLUE_ICE, 32),
                            6)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.CROSS_COUNTER.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 6),
                            IngredientStack.of(Items.CLOCK),
                            IngredientStack.of(Items.ECHO_SHARD, 4),
                            6))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 6),
                            IngredientStack.of(Items.COPPER_INGOT, 6),
                            IngredientStack.of(Items.ECHO_SHARD, 1),
                            6))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 6),
                            IngredientStack.of(Items.COPPER_INGOT, 12),
                            IngredientStack.of(Items.ECHO_SHARD, 2),
                            6)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.CUSHIONING.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 6),
                            IngredientStack.of(Items.PHANTOM_MEMBRANE, 4),
                            IngredientStack.of(ItemTags.WOOL, 2),
                            6))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 3),
                            IngredientStack.of(Items.PHANTOM_MEMBRANE),
                            IngredientStack.of(Items.FEATHER, 4),
                            2)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.DEBILITATING_CHAIN.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 4),
                            IngredientStack.of(Items.FERMENTED_SPIDER_EYE, 2),
                            IngredientStack.of(Items.REDSTONE, 2),
                            4))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 3),
                            IngredientStack.of(Items.SPIDER_EYE),
                            IngredientStack.of(Items.STRING, 3),
                            3)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.DEFLECT.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 4),
                            IngredientStack.of(Items.SCUTE, 3),
                            IngredientStack.of(Items.SLIME_BALL, 3),
                            4)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.EVOKERS_WRATH.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 4),
                            IngredientStack.of(Items.EMERALD, 4),
                            IngredientStack.of(Items.BONE, 4),
                            4)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.GUIDING.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 3),
                            IngredientStack.of(Items.GLOWSTONE_DUST, 4),
                            IngredientStack.of(Items.REDSTONE, 2),
                            3)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.HELLWALKER.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 6),
                            IngredientStack.of(Items.MAGMA_CREAM, 5),
                            IngredientStack.of(Items.FEATHER, 2),
                            6)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.HOBBLING_SHOT.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 4),
                            IngredientStack.of(Items.VINE),
                            IngredientStack.of(Items.BONE_MEAL, 6),
                            4))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 3),
                            IngredientStack.of(Items.SUGAR),
                            IngredientStack.of(Items.FERMENTED_SPIDER_EYE),
                            4))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 3),
                            IngredientStack.of(Items.COBWEB, 4),
                            null,
                            4)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.LIFETIDE.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 9),
                            IngredientStack.of(Items.HEART_OF_THE_SEA),
                            IngredientStack.of(Items.GHAST_TEAR, 4),
                            6)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.MIASMA.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 12),
                            IngredientStack.of(Items.DRAGON_BREATH, 4),
                            IngredientStack.of(Items.SPORE_BLOSSOM),
                            6))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 4),
                            IngredientStack.of(Items.ECHO_SHARD, 2),
                            IngredientStack.of(Items.SPORE_BLOSSOM),
                            6)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.NETHER_HEART.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 12),
                            IngredientStack.of(AArcanaTags.Items.HEARTS),
                            IngredientStack.of(Items.NETHERITE_INGOT),
                            6)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.PINCUSHION.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 3),
                            IngredientStack.of(Items.CACTUS, 4),
                            IngredientStack.of(Items.TORCHFLOWER_SEEDS, 2),
                            4)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.PROTECTIVE_ECHO.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 9),
                            IngredientStack.of(Items.ECHO_SHARD, 3),
                            IngredientStack.of(Items.POPPED_CHORUS_FRUIT, 3),
                            6)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.REJUVENATING_SHOT.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 4),
                            IngredientStack.of(Items.GHAST_TEAR, 4),
                            null,
                            4)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.REPEATING.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 4),
                            IngredientStack.of(Items.PITCHER_PLANT, 2),
                            IngredientStack.of(Items.TRIPWIRE_HOOK, 2),
                            4))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 2),
                            IngredientStack.of(Items.PITCHER_PLANT, 2),
                            IngredientStack.of(Items.REDSTONE, 2),
                            4)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.RICOCHET.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 3),
                            IngredientStack.of(Items.SLIME_BALL, 4),
                            IngredientStack.of(Items.AMETHYST_SHARD, 2),
                            4))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 3),
                            IngredientStack.of(Items.SLIME_BALL, 4),
                            IngredientStack.of(Items.FEATHER, 1),
                            4)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.ROCKETRY.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 4),
                            IngredientStack.of(Items.GUNPOWDER, 4),
                            IngredientStack.of(Items.BLAZE_POWDER, 2),
                            4)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.SALVO.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 4),
                            IngredientStack.of(Items.PITCHER_PLANT, 4),
                            IngredientStack.of(Items.AMETHYST_CLUSTER),
                            4))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 4),
                            IngredientStack.of(Items.PITCHER_PLANT, 2),
                            IngredientStack.of(Items.GLOWSTONE_DUST, 2),
                            4)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.SHATTERSHOT.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 6),
                            IngredientStack.of(Items.HONEY_BOTTLE, 4),
                            IngredientStack.of(Items.DIAMOND, 2),
                            4)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.SINGULARITY.get())
                    .level(new  EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 6),
                            IngredientStack.of(Items.ENDER_PEARL, 2),
                            IngredientStack.of(Items.ECHO_SHARD, 3),
                            6))
                    .level(new  EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 4),
                            IngredientStack.of(Items.ENDER_PEARL, 2),
                            IngredientStack.of(Items.ECHO_SHARD, 2),
                            4)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.SMELTING.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 3),
                            IngredientStack.of(Items.BLAZE_POWDER, 4),
                            null,
                            4)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.SONIC_BLAST.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 12),
                            IngredientStack.of(AArcanaTags.Items.HEARTS),
                            IngredientStack.of(Items.ECHO_SHARD, 6),
                            6)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.SOUL_BURST.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 1),
                            IngredientStack.of(Items.SCULK_CATALYST, 1),
                            IngredientStack.of(Items.GUNPOWDER, 4),
                            3)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.STORM_HEART.get())
                    .level(new  EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 12),
                            IngredientStack.of(AArcanaTags.Items.HEARTS),
                            IngredientStack.of(Items.LIGHTNING_ROD, 6),
                            6)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.STRAFE.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 6),
                            IngredientStack.of(Items.FEATHER, 6),
                            IngredientStack.of(Items.SUGAR, 4),
                            3)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.SUNDERING.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 6),
                            IngredientStack.of(Items.TORCHFLOWER_SEEDS, 2),
                            IngredientStack.of(Items.FERMENTED_SPIDER_EYE, 2),
                            6)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.SUREFOOT.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 6),
                            IngredientStack.of(Items.ANVIL),
                            null,
                            6)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.TURTLE_HEART.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 12),
                            IngredientStack.of(AArcanaTags.Items.HEARTS),
                            IngredientStack.of(Items.SCUTE, 6),
                            6)));
            exporter.accept(new EnchantmentRecipeProvider(AArcanaEnchantments.WITCH_HEART.get())
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 12),
                            IngredientStack.of(AArcanaTags.Items.HEARTS),
                            IngredientStack.of(Items.CAULDRON),
                            6)));

            // Vanilla
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.AQUA_AFFINITY)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI),
                            IngredientStack.of(Items.PRISMARINE_CRYSTALS, 3),
                            null,
                            6)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.CHANNELING)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 3),
                            IngredientStack.of(Items.LIGHTNING_ROD),
                            IngredientStack.of(Items.GOLD_INGOT, 3),
                            6)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.DEPTH_STRIDER)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 2),
                            IngredientStack.of(Items.SCUTE, 4),
                            null,
                            2))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 2),
                            IngredientStack.of(Items.SCUTE, 4),
                            null,
                            2))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 2),
                            IngredientStack.of(Items.SOUL_SAND, 2),
                            null,
                            2)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.FIRE_ASPECT)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 2),
                            IngredientStack.of(Items.BLAZE_POWDER, 4),
                            IngredientStack.of(Items.FLINT, 1),
                            2)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.FLAMING_ARROWS)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 2),
                            IngredientStack.of(Items.BLAZE_POWDER, 6),
                            IngredientStack.of(Items.FLINT, 2),
                            2)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.BLOCK_FORTUNE)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 3),
                            IngredientStack.of(Items.COPPER_INGOT, 6),
                            IngredientStack.of(Items.IRON_INGOT, 3),
                            3))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 3),
                            IngredientStack.of(Items.QUARTZ, 6),
                            IngredientStack.of(Items.GOLD_INGOT, 3),
                            3))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 3),
                            IngredientStack.of(Items.DIAMOND, 3),
                            IngredientStack.of(Items.ECHO_SHARD),
                            3)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.FROST_WALKER)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 3),
                            IngredientStack.of(Items.ICE, 4),
                            null,
                            3))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get()),
                            IngredientStack.of(Items.BLUE_ICE, 4),
                            null,
                            3)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.FALL_PROTECTION)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 2),
                            IngredientStack.of(Items.FEATHER, 5),
                            null,
                            3))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 3),
                            IngredientStack.of(Items.FEATHER, 6),
                            null,
                            3))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 2),
                            IngredientStack.of(Items.FEATHER, 5),
                            IngredientStack.of(Items.PHANTOM_MEMBRANE, 2),
                            3)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.INFINITY_ARROWS)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 4),
                            IngredientStack.of(Items.ECHO_SHARD, 2),
                            IngredientStack.of(Items.ARROW),
                            4)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.THORNS)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 3),
                            IngredientStack.of(Items.CACTUS, 2),
                            IngredientStack.of(Items.SLIME_BALL),
                            3))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 3),
                            IngredientStack.of(Items.PRISMARINE_SHARD, 4),
                            IngredientStack.of(Items.SLIME_BALL),
                            3))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 3),
                            IngredientStack.of(Items.QUARTZ, 6),
                            null,
                            3)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.KNOCKBACK)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 1),
                            IngredientStack.of(Items.PISTON, 2),
                            null,
                            3)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.SOUL_SPEED)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 2),
                            IngredientStack.of(Items.SOUL_SAND, 2),
                            IngredientStack.of(Items.SUGAR, 4),
                            3)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.SWIFT_SNEAK)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 3),
                            IngredientStack.of(Items.SUGAR, 4),
                            IngredientStack.of(Items.ECHO_SHARD),
                            3)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.MOB_LOOTING)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 3),
                            IngredientStack.of(Items.ENDER_PEARL, 3),
                            IngredientStack.of(Items.BONE, 2),
                            3))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 3),
                            IngredientStack.of(Items.RABBIT_FOOT, 1),
                            IngredientStack.of(Items.GUNPOWDER, 4),
                            3))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 3),
                            IngredientStack.of(Items.WITHER_SKELETON_SKULL, 1),
                            IngredientStack.of(Items.RABBIT_FOOT, 2),
                            3)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.RESPIRATION)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 3),
                            IngredientStack.of(Items.GLASS_BOTTLE, 2),
                            IngredientStack.of(Items.BAMBOO),
                            3))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 2),
                            IngredientStack.of(Items.GLASS_BOTTLE, 2),
                            null,
                            3)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.RIPTIDE)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 3),
                            IngredientStack.of(Items.NAUTILUS_SHELL, 1),
                            null,
                            3))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 3),
                            IngredientStack.of(Items.NAUTILUS_SHELL, 2),
                            IngredientStack.of(Items.SUGAR, 2),
                            3))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 3),
                            IngredientStack.of(Items.NAUTILUS_SHELL, 1),
                            IngredientStack.of(Items.PHANTOM_MEMBRANE, 3),
                            3)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.LOYALTY)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 2),
                            IngredientStack.of(Items.COD_BUCKET),
                            null,
                            3))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 3),
                            IngredientStack.of(ItemTags.FISHES, 3),
                            null,
                            3)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.SWEEPING_EDGE)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 3),
                            IngredientStack.of(Items.AMETHYST_SHARD, 2),
                            null,
                            3)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.PIERCING)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 3),
                            IngredientStack.of(Items.TORCHFLOWER),
                            null,
                            3))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 2),
                            IngredientStack.of(Items.FEATHER, 2),
                            null,
                            2)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.SILK_TOUCH)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 2),
                            IngredientStack.of(Items.STRING, 2),
                            IngredientStack.of(Items.HONEY_BOTTLE),
                            2)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.PUNCH_ARROWS)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 2),
                            IngredientStack.of(Items.PISTON, 2),
                            IngredientStack.of(Items.REDSTONE, 1),
                            2))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 2),
                            IngredientStack.of(Items.PISTON, 2),
                            null,
                            2)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.MULTISHOT)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get()),
                            IngredientStack.of(Items.ECHO_SHARD, 2),
                            null,
                            2)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.FISHING_SPEED)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 2),
                            IngredientStack.of(ItemTags.FISHES, 3),
                            IngredientStack.of(Items.IRON_NUGGET, 1),
                            3))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(Items.LAPIS_LAZULI, 2),
                            IngredientStack.of(ItemTags.FISHES, 2),
                            null,
                            3)));
            exporter.accept(new EnchantmentRecipeProvider(Enchantments.FISHING_LUCK)
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 2),
                            IngredientStack.of(Items.COD),
                            IngredientStack.of(Items.SALMON),
                            3))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 2),
                            IngredientStack.of(Items.PUFFERFISH),
                            null,
                            3))
                    .level(new EnchantmentRecipeProvider.EnchantmentLevelRecipeProvider(
                            IngredientStack.of(AArcanaItems.ENCHANTED_SCRAP.get(), 2),
                            IngredientStack.of(Items.TROPICAL_FISH),
                            null,
                            3)));
        }
    }
}
