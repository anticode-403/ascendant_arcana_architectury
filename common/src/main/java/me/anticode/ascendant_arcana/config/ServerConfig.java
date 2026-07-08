package me.anticode.ascendant_arcana.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.*;
import java.util.stream.Collectors;

@Config(name = "server")
public class ServerConfig implements ConfigData {
    @Comment("Multiplies the base enchantment capacity and all gains by this value. 1.0 is recommended.")
    public double capacity_multiplier = 1.0D;

    @Comment("Default relic capacity of an item, recommended is 2 but 1 could provide for more interesting gameplay.")
    public int base_relic_capacity = 2;

    @Comment("XP is by default disabled in Ascendant Arcana, but can be optionally enabled and is fully supported if you do so.")
    public boolean disable_xp = true;

    @Comment("This can sometimes cause issues with mods that modify the HUD.")
    public boolean hide_xp_bar = true;

    @Comment("Levels are linear if enabled. This adjusts the XP required to level up each time. Setting this to 0 disables it.")
    public int xp_per_level = 30;

    @Comment("Some vanilla mobs drop relics on death, like Witches, Wither Skeletons, and bosses. Recommended.")
    public boolean add_relics_to_entities = true;

    @Comment("Relics are automatically added to ANY chest loot table containing an enchanted book. Recommended.")
    public boolean add_relics_to_chests = true;

    @Comment("Add restorine to any chests containing amethyst shards or diamonds")
    public boolean add_restorine_to_chests = true;

    @Comment("Add unique boss drops, like the Warden Heart. Disabling this will make some enchantments unobtainable.")
    public boolean add_boss_drops = true;

    @Comment("The minimum amount of power (bookshelves and enchanted books within range of an enchanting table) to enchant.")
    public int minimum_enchanting_power = 0;

    @Comment("The minimum amount of power required to enchant uncommon enchantments.")
    public int uncommon_enchanting_power = 22;

    @Comment("The minimum amount of power required to enchant rare enchantments.")
    public int rare_enchanting_power = 55;

    @Comment("The minimum amount of power required to enchant very rare enchantments.")
    public int very_rare_enchanting_power = 100;

    @Comment("Enchanted Books can only have one enchantment on them. Otherwise, they use the regular capacity system.")
    public boolean single_enchantment_books = true;

    @Comment("""
            Ascendant Arcana disables many vanilla enchantments because they stress the capacity system too much with
            'required' enchantments. Enchantments are generally meant to be more interesting and meaningfully impactful
            but this list is configurable so you can enable or disable whatever you want.
            
            Do note that most enchantments on this list by default DO NOT come with recipes, so in order to see them in
            the Enchanting Table you must create your own enchantment recipes for them with a datapack.""")
    public Set<String> disabled_enchantments;

    @Comment("Items which have their base relic capacity value overwritten.")
    public Map<String, Integer> base_relic_capacity_overrides = new HashMap<>(Map.of(
            "minecraft:crossbow",
            1
    ));

    @Comment("""
            Items which have their base enchantment capacity overwritten. The default enchantment capacity of an item
            is determined by the vanilla enchantability stat of the item's material. For example, Diamond is 10 and
            Netherite is 15. Items with an enchantability of 1 have their capacity increased to 10 by default.""")
    public Map<String, Integer> base_enchantment_capacity_overrides = new HashMap<>(Map.of(
            "minecraft:bow",
            20,
            "minecraft:crossbow",
            20,
            "minecraft:enchanted_book",
            12,
            "minecraft:shield",
            15,
            "minecraft:trident",
            15
    ));

    @Comment("Enchantments which have their base rarity overwritten. Common is 1, Uncommon is 2, Rare is 3, Very Rare is 4. This might not work on modded enchantments.")
    public Map<String, Integer> overwritten_rarities = new HashMap<>(Map.ofEntries(
            Map.entry("minecraft:knockback", 1),
            Map.entry("minecraft:fire_aspect", 2),
            Map.entry("minecraft:punch", 1),
            Map.entry("minecraft:flame", 2),
            Map.entry("minecraft:respiration", 2),
            Map.entry("minecraft:sweeping", 2),
            Map.entry("minecraft:fortune", 4),
            Map.entry("minecraft:looting", 4),
            Map.entry("minecraft:infinity", 3),
            Map.entry("minecraft:lure", 1),
            Map.entry("minecraft:frost_walker", 2),
            Map.entry("minecraft:thorns", 2)
    ));

    @Comment("""
            Damage Relics apply to thrown Tridents, melee weapons, as well as arrows and rockets fired from bows and
            crossbows.
            Percentages are written as decimal values. For example, a 10% damage increase is written as 0.10 here.""")
    public Map<Integer, Double> damage_relic_strengths = new HashMap<>(Map.ofEntries(
            Map.entry(1, 0.10),
            Map.entry(2, 0.16),
            Map.entry(3, 0.22),
            Map.entry(4, 0.26),
            Map.entry(5, 0.30)
    ));

    @Comment("""
            Durability Relics can apply to any item with a durability value. For all vanilla items, the default
            values are better than Unbreaking 3.
            Because durability is stored as an integer in-game, durability relics cannot contain decimal values.""")
    public Map<Integer, Integer> durability_relic_strengths = new HashMap<>(Map.ofEntries(
            Map.entry(1, 600),
            Map.entry(2, 1200),
            Map.entry(3, 1800),
            Map.entry(4, 2400),
            Map.entry(5, 3000)
    ));

    @Comment("""
            Protection Relics reduce the damage taken from most sources. Generally, this is any instance of damage
            that the original Protection enchantment would protect you from. The default values here are slightly
            weaker than the original benefits of protection by 1% per level, meaning Protection 4 is 16% DR and
            an Ascendant Protection Relic is 15%.
            Percentages are written as decimal values. For example, a 3% damage resistance is written as 0.03 here.""")
    public Map<Integer, Double> protection_relic_strengths = new HashMap<>(Map.ofEntries(
            Map.entry(1, 0.03),
            Map.entry(2, 0.06),
            Map.entry(3, 0.09),
            Map.entry(4, 0.12),
            Map.entry(5, 0.15)
    ));


    @Comment("""
            Haste Relics behave in a way that is technically not accurate. On weapons, Haste relics apply half of
            their benefit (like other Haste effects in Minecraft). But for mining, Haste relics act as a level
            of Efficiency. That level is obtained by multiplying the value of the haste relic by 10 and rounding down.
            For example, a value of 0.10 (a 10% relic) becomes 1 level of Efficiency.
            This behavior is likely to be changed in the future.
            Percentages are written as decimal values. For example, a 10% haste increase is written as 0.10 here.""")
    public Map<Integer, Double> haste_relic_strengths = new HashMap<>(Map.ofEntries(
            Map.entry(1, 0.10),
            Map.entry(2, 0.20),
            Map.entry(3, 0.30),
            Map.entry(4, 0.40),
            Map.entry(5, 0.50)
    ));


    @Comment("""
            Enchantment Capacity Relics are used to expand your item's capability to hold powerful enchantments.
            These relics are additive.
            Because Enchantment Capacity is an integer, you cannot add a decimal value here.""")
    public Map<Integer, Integer> enchantment_capacity_relic_strengths = new HashMap<>(Map.ofEntries(
            Map.entry(1, 10),
            Map.entry(2, 15),
            Map.entry(3, 20),
            Map.entry(4, 25),
            Map.entry(5, 30)
    ));

    @Override
    public void validatePostLoad() throws ValidationException {
        ConfigData.super.validatePostLoad();
        if (disabled_enchantments == null) {
            disabled_enchantments = new LinkedHashSet<>();
            disabled_enchantments.add("minecraft:protection");
            disabled_enchantments.add("minecraft:sharpness");
            disabled_enchantments.add("minecraft:efficiency");
            disabled_enchantments.add("minecraft:quick_charge");
            disabled_enchantments.add("minecraft:power");
            disabled_enchantments.add("minecraft:bane_of_arthropods");
            disabled_enchantments.add("minecraft:blast_protection");
            disabled_enchantments.add("minecraft:projectile_protection");
            disabled_enchantments.add("minecraft:fire_protection");
            disabled_enchantments.add("minecraft:smite");
            disabled_enchantments.add("minecraft:impaling");
            disabled_enchantments.add("minecraft:mending");
            disabled_enchantments.add("minecraft:unbreaking");
            disabled_enchantments.add("majruszsenchantments:misanthropy");
            disabled_enchantments.add("majruszsenchantments:gold_fuelled");
            disabled_enchantments.add("majruszsenchantments:smelter");
            disabled_enchantments.add("majruszsenchantments:magic_protection");
            disabled_enchantments.add("majruszsenchantments:dodge");
            disabled_enchantments.add("majruszsenchantments:enlightenment");
            disabled_enchantments.add("majruszsenchantments:immortality");
            disabled_enchantments.add("ascendant_arcana:coldheart");
            disabled_enchantments.add("ascendant_arcana:heart_of_the_storm");
        }
    }
}
