package me.anticode.ascendant_arcana.init;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.item.*;
import me.anticode.ascendant_arcana.logic.Relics;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AArcanaItems {
    private static final ResourceLocation EMPTY_ARMOR_SLOT_HELMET_TEXTURE = new ResourceLocation("item/empty_armor_slot_helmet");
    private static final ResourceLocation EMPTY_ARMOR_SLOT_CHESTPLATE_TEXTURE = new ResourceLocation("item/empty_armor_slot_chestplate");
    private static final ResourceLocation EMPTY_ARMOR_SLOT_LEGGINGS_TEXTURE = new ResourceLocation("item/empty_armor_slot_leggings");
    private static final ResourceLocation EMPTY_ARMOR_SLOT_BOOTS_TEXTURE = new ResourceLocation("item/empty_armor_slot_boots");
    private static final ResourceLocation EMPTY_SLOT_HOE_TEXTURE = new ResourceLocation("item/empty_slot_hoe");
    private static final ResourceLocation EMPTY_SLOT_AXE_TEXTURE = new ResourceLocation("item/empty_slot_axe");
    private static final ResourceLocation EMPTY_SLOT_SWORD_TEXTURE = new ResourceLocation("item/empty_slot_sword");
    private static final ResourceLocation EMPTY_SLOT_SHOVEL_TEXTURE = new ResourceLocation("item/empty_slot_shovel");
    private static final ResourceLocation EMPTY_SLOT_PICKAXE_TEXTURE = new ResourceLocation("item/empty_slot_pickaxe");

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(AscendantArcana.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Item> INFUSION_SMITHING_TEMPLATE = register(() -> new SmithingTemplateItem(
            Component.translatable("item.ascendant_arcana.smithing_template.infusion.applies_to").withStyle(ChatFormatting.BLUE),
            Component.translatable("item.ascendant_arcana.smithing_template.infusion.ingredients").withStyle(ChatFormatting.BLUE),
            Component.translatable("item.ascendant_arcana.smithing_template.infusion.title").withStyle(ChatFormatting.GRAY),
            Component.translatable("item.ascendant_arcana.smithing_template.infusion.base_slot_description"),
            Component.translatable("item.ascendant_arcana.smithing_template.infusion.additions_slot_description"),
            getGeneralToolSmithingBase(),
            List.of() // TODO: Create empty slot textures for relics
    ), "infusion_smithing_template");

    public static final RegistrySupplier<Item> RELIC = register(() -> new RelicItem(new Item.Properties()), "relic");

    public static final RegistrySupplier<Item> ENCHANTED_SCRAP = register(() -> new EnchantedScrapItem(new Item.Properties()), "enchanted_scrap");
    public static final RegistrySupplier<Item> RESTORINE = register(() -> new Item(new Item.Properties()), "restorine");

    public static final RegistrySupplier<Item> WARDEN_HEART = register(() -> new Item(new Item.Properties().rarity(Rarity.RARE).stacksTo(1).fireResistant()), "warden_heart");

    private static List<ResourceLocation> getGeneralToolSmithingBase() {
        return List.of(EMPTY_SLOT_AXE_TEXTURE, EMPTY_SLOT_HOE_TEXTURE, EMPTY_SLOT_PICKAXE_TEXTURE,
                EMPTY_SLOT_SWORD_TEXTURE, EMPTY_SLOT_SHOVEL_TEXTURE, EMPTY_ARMOR_SLOT_BOOTS_TEXTURE,
                EMPTY_ARMOR_SLOT_CHESTPLATE_TEXTURE, EMPTY_ARMOR_SLOT_LEGGINGS_TEXTURE, EMPTY_ARMOR_SLOT_HELMET_TEXTURE);
    }

    public static RegistrySupplier<Item> register(Supplier<Item> item, String id) {
        return ITEMS.register(id, item);
    }

    public static ItemStack after(Item item) {
        return new ItemStack(item);
    }

    public static void initialize() {
        ITEMS.register();

        List<Supplier<ItemStack>> relicEntries = new ArrayList<>();
        for (int i = 0; i < Relics.values().length * 5; i++) {
            int relicId = Mth.floor((double) i / 5);
            int strength = i + 1 - (relicId * 5);
            Relics relicType = Relics.fromId(relicId);
            relicEntries.add(() -> {
                ItemStack relic = new ItemStack(RELIC.get());
                RelicItem.writeRelicData(relic, relicType, strength);
                return relic;
            });
        }

        CreativeTabRegistry.append(AscendantArcana.ASCENDANT_ARCANA_TAB, ENCHANTED_SCRAP, RESTORINE, WARDEN_HEART, INFUSION_SMITHING_TEMPLATE);

        CreativeTabRegistry.appendStack(AscendantArcana.ASCENDANT_ARCANA_TAB, relicEntries.stream());
    }
}
