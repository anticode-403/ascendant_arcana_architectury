# Ascendant Arcana

Ascendant Arcana is a mod that completely overhauls enchanting and enchantment-related progression, focusing on making enchantments unique modifiers to your tools, rather than glorified stat bonuses that made up most of your vanilla power.

## Progression

Enchanting is now open to the user much earlier than it previously was. An early version of the enchanting table, the **Copper Enchanting Table**, can now be crafted with copper, restorine, and a book! This Enchanting Table can only make common and uncommon enchantments, but it's a very good source for early game enchantments!

## Enchanting

**Enchanting Tables are no longer random.** Enchantments are instead obtained with recipes inside the enchantment table, although what recipes you can and can't see depend on the surrounding blocks. Like before, the Enchanting Table gains power from Bookshelves. But unlike before, Chiseled Bookshelves now also contribute. Enchantments are locked based on their rarity and require a certain amount of power to unlock each rarity. Rare and Very Rare enchantments _require_ Chiseled Bookshelves to become unlocked, gaining extra power from each Enchanted Book inside.

Treasure Enchantments can also be placed inside nearby Chiseled Bookshelves to unlock their recipes in the Enchanting Table.

Most enchantments require Magical Scrap, which can be crafted with Gold Nuggets, Amethyst, and Lapis or obtained by scrapping enchanted items in the Grindstone.

## Repairing and Upgrades

**Stat bonuses are now found from Relics, a new item that can be found in chests, obtained from archaeology, vanilla bosses, and some magical enemies.** Early relics can be crafted from fairly simple resources, while later relics must be found or obtained by slaying bosses. Relics can provide bonuses to attack/mining speed, durability, damage, protection, or enchantment capacity.

Mending has also been removed. **Instead, caves can now generate with a new resource, Restorine, which acts as a universal repair ingredient.** Restorine even regrows on its own, similar to Amethyst Clusters, so once you've found a source you can keep going back for more. If you decide to use the item's original repair ingredient, it will repair double the amount it previously did!

## Other Changes

Additionally, Ascendant Arcana makes many smaller changes to make both enchantments and combat feel better and most consistent;
 - Infinity no longer requires and arrow
 - Crossbows cannot load rockets by default, now requiring the Rocketry enchantment
 - Piercing applies to bows only, not crossbows.
 - Arrows bypass vanilla i-frames when shot by the same entity, allowing enchantments like Multishot and Salvo to deal more damage if you hit the same entity multiple times with a single shot.
 - The Enchanting Table can no longer be obscured from bookshelves.
 - The Enchanting Table's range is increased (this is configurable)
 - Base mining speed for all tools is drastically increased (this is configurable)
 - Shields no longer have a 5 tick delay between raising the shield and actually being able to block attacks.

## What's Next?

**Potion Overhaul,** complete with completely revamped recipes, new potions, new ingredients, and more uses for old ingredients.

**Spellbooks,** active use enchantments that can be applied to a new spellbook item, meant to be a late-game alternative to bows and crossbows.

**More Enchantments,** a host of new enchantments for armor, bows, tools, and more!

**And More!**

# For Mod and Modpack Developers:
Adding compatibility is as simple as making a datapack. New enchantment recipes are as follows (using the Death Wish enchantment from Majrusz's Enchantments as an example):

```JSON
{
  "type": "ascendant_arcana:enchantment_recipe",
  "enchantment": "majruszsenchantments:death_wish",
  "levels": [
    {
      "level_cost": 3,
      "scrap_stack": {
        "ingredient": {
          "item": "ascendant_arcana:enchanted_scrap"
        },
        "count": 3
      },
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
  ]
}
```

Additionally, Ascendant Arcana creates the following recipe formats:

### Custom Infusion Smithing Templates
Custom infusion smithing templates can be made with the following recipe. `template_id` is the item ID to be used for the recipe, and `max_tier` is the highest tier of relics that can be used for the recipe, with a minimum of 1 and a maximum of 5.

The following is the basic recipe shipped with Ascendant Arcana by default.

```JSON
{
  "type": "ascendant_arcana:infusion_smithing_recipe",
  "template_id": "ascendant_arcana:infusion_smithing_template",
  "max_tier": 5
}
```

### Custom Universal Repair Ingredients
Custom universal repair ingredients can be made with the following recipe. `addition` determines how the repair amount is calculated. If `addition` is true, then `repair_amount` should be a flat integer value that determines how much is repaired at once. If `addition` is false, `repair_amount` should be a demical value between 0 and 1, with 1 being 100% of the item's durability being repaired and 0 being no durability repair.

The following is the basic recipe shipped with Ascendant Arcana by default.

```JSON
{
  "type": "ascendant_arcana:repair_recipe",
  "ingredient": {
    "item": "ascendant_arcana:restorine"
  },
  "addition": false,
  "repair_amount": 0.125
}
```

And lastly, relics are also data driven! The format is as follows (but is subject to change in future major updates):
```JSON
{
  "type": "ascendant_arcana:damage",
  "operation": "multiply_total",
  "target": "tool",
  "strengths": [
    0.10,
    0.16,
    0.22,
    0.26,
    0.30
  ]
}
```
`operation` accepts `addition` and `multiply_total`. 
`target` accepts `tool`, `armor`, `durability`, and `enchantable`.