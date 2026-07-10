# Ascendant Arcana

Ascendant Arcana is a mod that completely overhauls enchanting and enchantment-related progression, focusing on making enchantments unique modifiers to your tools, rather than glorified stat bonuses that made up most of your vanilla power.

## Progression

Enchanting is now open to the user much earlier than it previously was. An early version of the enchanting table, the **Copper Enchanting Table**, can now be crafted with copper, restorine, and a book! This Enchanting Table can only make common and uncommon enchantments, but its a very good source for early game enchantments!

## Enchanting

**Enchanting Tables are no longer random.** Enchantments are instead obtained with recipes inside the enchantment table, although what recipes you can and can't see depend on the surrounding blocks. Like before, the Enchanting Table gains power from Bookshelves. But unlike before, Chiseled Bookshelves now also contribute. Enchantments are locked based on their rarity and require a certain amount of power to unlock each rarity. Rare and Very Rare enchantments _require_ Chiseled Bookshelves to become unlocked, gaining extra power from each Enchanted Book inside.

Treasure Enchantments can also be placed inside nearby Chiseled Bookshelves to unlock their recipes in the Enchanting Table.

Most enchantments require Magical Scrap, which can be crafted with Gold Nuggets, Amethyst, and Lapis or obtained by scrapping enchanted items in the Grindstone.

## Repairing and Upgrades

**Stat bonuses are now found from Relics, a new item that can be found in chests, obtained from archaeology, vanilla bosses, and some magical enemies.** Early relics can be crafted from fairly simple resources, while later relics must be found or obtained by slaying bosses. Relics can provide bonuses to attack/mining speed, durability, damage, protection, or enchantment capacity.

Mending has also been removed. **Instead, caves can now generate with a new resource, Restorine, which acts as a universal repair ingredient.** Restorine even regrows on its own, similar to Amethyst Clusters, so once you've found a source you can keep going back for more. If you decide to use the item's original repair ingredient, it will repair double the amount it previously did!

## What's Next?

**Potion Overhaul,** complete with completely revamped recipes, new potions, new ingredients, and more uses for old ingredients.

**Spellbooks,** active use enchantments that can be applied to a new spellbook item, meant to be a late-game alternative to bows and crossbows.

**More Enchantments,** a host of new enchantments for armor, tridents, crossbows, and more!

**And More!**

# For Mod and Modpack Developers:
Adding compatibility is as simple as making a datapack. New enchantment recipes are as follows (using the Death Wish enchantment from Majrusz's Enchantments as an example):

```JSON
{
  "type": "ascendant_arcana:enchantment_recipe",
  "enchantment": "majruszsenchantments:death_wish",
  "level_cost": 3,
  "magical_scrap_cost": 3,
  "primary_ingredient": {
    "count": 2,
    "ingredient": {
      "item": "minecraft:fermented_spider_eye"
    }
  },
  "secondary_ingredient": {
    "count": 1,
    "ingredient": {
      "item": "minecraft:wither_skeleton_skull"
    }
  }
}
```
