package me.anticode.ascendant_arcana.screenhandler;

import dev.architectury.networking.NetworkManager;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.init.AArcanaBlocks;
import me.anticode.ascendant_arcana.init.AArcanaItems;
import me.anticode.ascendant_arcana.init.AArcanaMenus;
import me.anticode.ascendant_arcana.logic.AArcanaEnchantmentHelper;
import me.anticode.ascendant_arcana.networking.EnchantingScreenSync;
import me.anticode.ascendant_arcana.recipe.EnchantmentRecipe;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AArcanaEnchantingMenu extends AbstractContainerMenu {
    private final Container inventory;
    private final ContainerLevelAccess context;
    public final int[] enchantmentPower = new int[] { 0 };
    public List<Enchantment> unlockedTreasures = new ArrayList<>();
    public Player player;
    public EnchantmentRecipe recipe;
    ItemStack last;

    public AArcanaEnchantingMenu(int Id, Inventory playerInventory) {
        this(Id, playerInventory, ContainerLevelAccess.NULL);
    }

    public AArcanaEnchantingMenu(int Id, Inventory playerInventory, ContainerLevelAccess context) {
        super(AArcanaMenus.ENCHANTING.get(), Id);

        this.context = context;

        inventory = new SimpleContainer(4) {
            public void setChanged() {
                super.setChanged();
                AArcanaEnchantingMenu.this.slotsChanged(this);
            }
        };
        player = playerInventory.player;
        inventory.startOpen(player);

        addSlot(new EnchantableToolSlot(inventory, 0, 22, 61));
        addSlot(new MagicalScrapSlot(inventory, 1, 164, 60));
        addSlot(new EnchantmentIngredientSlot(inventory, 2, 164, 78));
        addSlot(new EnchantmentIngredientSlot(inventory, 3, 164, 96));

        addDataSlot(DataSlot.shared(enchantmentPower, 0));

        updatePower();

        int x, y;
        for (y = 0; y < 3; ++y) {
            for (x = 0; x < 9; ++x) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 36 + x * 18, 137 + y * 18));
            }
        }
        for (y = 0; y < 9; y++) {
            this.addSlot(new Slot(playerInventory, y, 36 + y * 18, 195));
        }
    }

    @Override
    public void slotsChanged(Container inventory) {
        if (inventory != this.inventory) return;
        ItemStack itemStack = inventory.getItem(0);
        if (last == null || itemStack == ItemStack.EMPTY || (last != ItemStack.EMPTY && !last.is(itemStack.getItem()))) {
            dumpContents(true);
            if (last != null && (itemStack.isEmpty() || (!itemStack.isEnchanted() && !itemStack.isEnchantable()))) return;
            last = itemStack;
            updatePower();
        }
    }

    private void updatePower() {
        context.execute((level, pos) -> {
            int i = 0;

            for (BlockPos blockPos : EnchantmentTableBlock.BOOKSHELF_OFFSETS) {
                if (EnchantmentTableBlock.isValidBookShelf(level, pos, blockPos)) {
                    if (level.getBlockEntity(pos.offset(blockPos), BlockEntityType.CHISELED_BOOKSHELF).isPresent()) {
                        ChiseledBookShelfBlockEntity chiseledBookshelf = (ChiseledBookShelfBlockEntity) level.getBlockEntity(pos.offset(blockPos));
                        assert chiseledBookshelf != null;
                        for (int j = 0; j < chiseledBookshelf.getContainerSize(); j++) {
                            ItemStack itemStack1 = chiseledBookshelf.getItem(j);
                            for (Map.Entry<Enchantment, Integer> enchantInstance : EnchantmentHelper.getEnchantments(itemStack1).entrySet()) {
                                int rarityMultiplier = switch (enchantInstance.getKey().getRarity()) {
                                    case UNCOMMON -> 2;
                                    case RARE -> 3;
                                    case VERY_RARE -> 5;
                                    default -> 1;
                                };
                                i += enchantInstance.getValue() * rarityMultiplier;
                                if ((enchantInstance.getKey().isTreasureOnly() || AscendantArcana.config.books_remove_scrap_cost || AscendantArcana.config.books_tier_bypass != 0) && !unlockedTreasures.contains(enchantInstance.getKey())) {
                                    unlockedTreasures.add(enchantInstance.getKey());
                                }
                            }
                        }
                    } else i++;
                }
            }
            NetworkManager.sendToPlayer((ServerPlayer) player, EnchantingScreenSync.Id, new EnchantingScreenSync(containerId, unlockedTreasures).write());
            if (level.getBlockState(pos).getBlock().defaultBlockState().is(AArcanaBlocks.COPPER_ENCHANTING_TABLE.get())) {
                if (i > AscendantArcana.config.uncommon_enchanting_power) i = AscendantArcana.config.uncommon_enchanting_power;
            }
            enchantmentPower[0] = i;
        });
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack stackCopy = ItemStack.EMPTY;

        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack moveStack = slot.getItem();
            stackCopy = moveStack.copy();
            if (index <= 3) {
                if (!this.moveItemStackTo(moveStack, 4, 40, true)) return ItemStack.EMPTY;
            } else if (moveStack.isEnchantable() || moveStack.isEnchanted() || moveStack.is(Items.BOOK) || moveStack.is(Items.ENCHANTED_BOOK)) {
                if (moveStack.is(Items.BOOK)) {
                    stackCopy = moveStack.copyWithCount(1);
                    moveStack.shrink(1);
                    ((Slot)this.slots.get(0)).setByPlayer(stackCopy);
                }
                else if (!this.moveItemStackTo(moveStack, 0, 1, false)) return ItemStack.EMPTY;
            } else if (moveStack.is(AArcanaItems.ENCHANTED_SCRAP.get())) {
                if (!this.moveItemStackTo(moveStack, 1, 2, false)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(moveStack, 2, 4, false)) return ItemStack.EMPTY;
            }

            if (moveStack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();

            if (moveStack.getCount() == stackCopy.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, moveStack);
        }
        return stackCopy;
    }

    @Override
    public boolean stillValid(Player player) {
        return inventory.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        dumpContents(false);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        ItemStack itemStack = inventory.getItem(0);
        if (recipe == null) return false;
        Map<Enchantment, Integer> itemEnchants;
        if (itemStack.is(Items.ENCHANTED_BOOK)) itemEnchants = EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(itemStack));
        else itemEnchants = EnchantmentHelper.getEnchantments(itemStack);
        EnchantmentRecipe.EnchantmentLevelRecipe levelRecipe;
        if (itemEnchants.containsKey(recipe.enchantment)) {
            int appliedLevel = itemEnchants.get(recipe.enchantment);
            if (recipe.getLevels().size() < appliedLevel) levelRecipe = recipe.getLevels().get(appliedLevel);
            else levelRecipe = recipe.getLevels().get(recipe.getLevels().size() - 1);
        } else levelRecipe = recipe.getLevels().get(0);
        if (!recipe.enchantment.canEnchant(itemStack) && !(itemStack.is(Items.BOOK) || itemStack.is(Items.ENCHANTED_BOOK))) return false;
        ItemStack scrapStack = inventory.getItem(1);
        ItemStack primaryStack = inventory.getItem(2);
        ItemStack secondaryStack = inventory.getItem(3);

        // Verifying
        if (!AscendantArcana.config.disable_xp && levelRecipe.levelCost() > player.experienceLevel) return false;
        if (!AArcanaEnchantmentHelper.testEnchantmentCost(itemStack, AArcanaEnchantmentHelper.getEnchantmentCost(recipe.enchantment))) return false;
        if ((!AscendantArcana.config.books_remove_scrap_cost || !unlockedTreasures.contains(recipe.enchantment)) && levelRecipe.scrapStack() != null) {
            if (!levelRecipe.scrapStack().test(scrapStack)) return false;
        }
        if (levelRecipe.primaryIngredientStack() != null) {
            if (!levelRecipe.primaryIngredientStack().test(primaryStack)) return false;
        }
        if (levelRecipe.secondaryIngredientStack() != null) {
            if (!levelRecipe.secondaryIngredientStack().test(secondaryStack)) return false;
        }
        if (itemEnchants.containsKey(recipe.enchantment)) {
            if (itemEnchants.get(recipe.enchantment) + 1 > recipe.enchantment.getMaxLevel()) return false;
        } else if (!EnchantmentHelper.isEnchantmentCompatible(itemEnchants.keySet(), recipe.enchantment)) return false;

        context.execute((level, pos) -> {
            player.onEnchantmentPerformed(itemStack, levelRecipe.levelCost());
            if ((!AscendantArcana.config.books_remove_scrap_cost || !unlockedTreasures.contains(recipe.enchantment)) && levelRecipe.scrapStack() != null)
                scrapStack.setCount(scrapStack.getCount() - levelRecipe.scrapStack().getCount());
            ItemStack newStack = itemStack;
            if (scrapStack.isEmpty()) {
                inventory.setItem(1, ItemStack.EMPTY);
            }
            if (levelRecipe.primaryIngredientStack() != null) {
                primaryStack.setCount(primaryStack.getCount() - levelRecipe.primaryIngredientStack().getCount());
                if (primaryStack.isEmpty()) {
                    inventory.setItem(2, ItemStack.EMPTY);
                }
            }
            if (levelRecipe.secondaryIngredientStack() != null) {
                secondaryStack.setCount(secondaryStack.getCount() - levelRecipe.secondaryIngredientStack().getCount());
                if (secondaryStack.isEmpty()) {
                    inventory.setItem(3, ItemStack.EMPTY);
                }
            }
            if (itemStack.is(Items.BOOK)) {
                newStack = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(recipe.enchantment, 1));
            } else if (itemStack.is(Items.ENCHANTED_BOOK)) {
                if (itemEnchants.containsKey(recipe.enchantment)) {
                    newStack = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(recipe.enchantment, itemEnchants.get(recipe.enchantment) + 1));
                    for (Map.Entry<Enchantment, Integer> entry : itemEnchants.entrySet()) {
                        if (entry.getKey() == recipe.enchantment) continue;
                        EnchantedBookItem.addEnchantment(newStack, new EnchantmentInstance(entry.getKey(), entry.getValue()));
                    }
                } else {
                    EnchantedBookItem.addEnchantment(newStack, new EnchantmentInstance(recipe.enchantment, 1));
                }
            } else {
                if (itemEnchants.containsKey(recipe.enchantment)) {
                    itemEnchants.put(recipe.enchantment, itemEnchants.get(recipe.enchantment) + 1);
                    EnchantmentHelper.setEnchantments(itemEnchants, newStack);
                } else {
                    newStack.enchant(recipe.enchantment, 1);
                }
            }
            player.awardStat(Stats.ENCHANT_ITEM);
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayer)player, newStack, levelRecipe.levelCost());
            }

            getSlot(0).set(newStack);

            inventory.setChanged();
            slotsChanged(inventory);
            level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);
        });

        return true;
    }

    public void dumpContents(boolean skipFirst) {
        if (inventory.isEmpty()) return;
        for (int i = skipFirst ? 1 : 0; i < inventory.getContainerSize(); ++i) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack != ItemStack.EMPTY) {
                if (!this.moveItemStackTo(itemStack, 4, 40, true)) player.drop(itemStack, true);
            }
        }
    }

    private static class EnchantableToolSlot extends Slot {
        public EnchantableToolSlot(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.isEnchantable() || stack.isEnchanted() || stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private class MagicalScrapSlot extends Slot {
        public MagicalScrapSlot(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(AArcanaItems.ENCHANTED_SCRAP.get()) && AArcanaEnchantingMenu.this.recipe != null;
        }
    }

    private class EnchantmentIngredientSlot extends Slot {
        public EnchantmentIngredientSlot(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            EnchantmentRecipe recipe = AArcanaEnchantingMenu.this.recipe;
            if (recipe == null) return false;
            ItemStack enchantTargetStack = inventory.getItem(0);
            Map<Enchantment, Integer> itemEnchants;
            if (enchantTargetStack.is(Items.ENCHANTED_BOOK)) itemEnchants = EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(enchantTargetStack));
            else itemEnchants = EnchantmentHelper.getEnchantments(enchantTargetStack);
            EnchantmentRecipe.EnchantmentLevelRecipe levelRecipe;
            if (itemEnchants.containsKey(recipe.enchantment)) {
                int appliedLevel = itemEnchants.get(recipe.enchantment);
                if (recipe.getLevels().size() < appliedLevel) levelRecipe = recipe.getLevels().get(appliedLevel);
                else levelRecipe = recipe.getLevels().get(recipe.getLevels().size() - 1);
            } else levelRecipe = recipe.getLevels().get(0);
            int index = getContainerSlot();
            return index == 2 ? levelRecipe.primaryIngredientStack() != null : levelRecipe.secondaryIngredientStack() != null;
        }
    }
}
