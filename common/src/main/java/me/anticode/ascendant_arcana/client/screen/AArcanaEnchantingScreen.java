package me.anticode.ascendant_arcana.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.architectury.networking.NetworkManager;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.init.AArcanaItems;
import me.anticode.ascendant_arcana.init.AArcanaRecipes;
import me.anticode.ascendant_arcana.logic.AArcanaEnchantmentHelper;
import me.anticode.ascendant_arcana.networking.EnchantingScreenRemoveRecipe;
import me.anticode.ascendant_arcana.networking.EnchantingScreenSendRecipe;
import me.anticode.ascendant_arcana.recipe.EnchantmentRecipe;
import me.anticode.ascendant_arcana.screenhandler.AArcanaEnchantingMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.joml.Matrix4f;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AArcanaEnchantingScreen extends AbstractContainerScreen<AArcanaEnchantingMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(AscendantArcana.MOD_ID, "textures/gui/container/enchanting_table.png");
    private static final ResourceLocation OVERLAYS = new ResourceLocation(AscendantArcana.MOD_ID, "textures/gui/container/enchanting_table_elements.png");
    List<EnchantmentRecipe> recipes = new ArrayList<>();
    private final List<EnchantmentTile> enchantments = new ArrayList<>();
    private List<Enchantment> unlockedTreasures = new ArrayList<>();
    private LetsGoEnchantingButton enchantingButton;
    private boolean enchantingButtonEnabled = false;
    private float scrollPosition;
    private boolean scrollerClicked;
    private ItemStack lastItem;
    private int lastImageWidth;
    private int lastImageHeight;
    private int lastPower = 0;
    private int selectedTile;
    private boolean anySelected;
    private boolean updateEnchantments = false;

    public AArcanaEnchantingScreen(AArcanaEnchantingMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.imageWidth = 229;
        this.imageHeight = 218;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        RenderSystem.disableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        context.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY) {
        super.renderLabels(context, mouseX, mouseY);
        boolean update = false;
        boolean partialUpdate = true;
        ItemStack itemStack = getMenu().getSlot(0).getItem();
        if (lastItem != null && !lastItem.is(itemStack.getItem())) {
            update = true;
            partialUpdate = false;
            recipes = new ArrayList<>();
            assert minecraft != null;
            assert minecraft.level != null;
            for (EnchantmentRecipe recipe : minecraft.level.getRecipeManager().getAllRecipesFor(AArcanaRecipes.ENCHANTMENT_RECIPE_TYPE.get())) {
                if (recipe.enchantment.canEnchant(itemStack)) {
                    recipes.add(recipe);
                }
            }
        }
        lastItem = itemStack;

        if (lastImageHeight != context.guiHeight() || lastImageWidth != context.guiWidth()) {
            update = true;
            lastImageWidth = context.guiWidth();
            lastImageHeight = context.guiHeight();
        }

        if (getMenu().enchantmentPower[0] != lastPower) {
            update = true;
            lastPower = getMenu().enchantmentPower[0];
        }

        if (getMenu().unlockedTreasures.size() != unlockedTreasures.size()) {
            update = true;
            unlockedTreasures = new ArrayList<>(unlockedTreasures);
        }

        if (!update && updateEnchantments) update = true;

        int panelX = 161;
        int panelY = 9;
        int scaledPanelX = panelX * 2;
        int scaledPanelY = panelY * 2;
        int scaledPanelWidth = 112;

        int k = (int)(94.0F * this.scrollPosition);
        boolean hasRecipes = recipes != null && !recipes.isEmpty();
        context.blit(OVERLAYS, 153, 9 + k, (hasRecipes && recipes.size() > 6 ? 0 : 6), 0, 6, 27);

        int progress;
        if (lastPower >= AscendantArcana.config.uncommon_enchanting_power) {
            if (lastPower >= AscendantArcana.config.rare_enchanting_power) {
                if (lastPower >= AscendantArcana.config.very_rare_enchanting_power) {
                    progress = 123;
                } else {
                    int relativePartial = lastPower - AscendantArcana.config.rare_enchanting_power;
                    int relativeGoal = AscendantArcana.config.very_rare_enchanting_power - AscendantArcana.config.rare_enchanting_power;
                    progress = 82 + Mth.floor((float)relativePartial / (float)relativeGoal * 41);
                }
            } else {
                int relativePartial = lastPower - AscendantArcana.config.uncommon_enchanting_power;
                int relativeGoal = AscendantArcana.config.rare_enchanting_power - AscendantArcana.config.uncommon_enchanting_power;
                int relative = Mth.floor(((float)relativePartial / (float)relativeGoal) * 41);
                progress = 41 + relative;
            }
        } else {
            progress = Mth.floor((float)lastPower / (float)AscendantArcana.config.uncommon_enchanting_power * 41F);
        }
        context.blit(OVERLAYS, 64, 8 + (123 - progress), 89, 135-progress, 5, progress);

        ItemStack stack = getMenu().getSlot(0).getItem();

        if (stack != ItemStack.EMPTY) {

            int maxCapacity = AArcanaEnchantmentHelper.getEnchantmentCapacity(stack);
            int usedCapacity = AArcanaEnchantmentHelper.getEnchantmentUsage(stack);
            float multiplier = (float) usedCapacity / maxCapacity;
            if (multiplier > 1) multiplier = 1;

            context.blit(OVERLAYS, 11, 53 + (32 - Mth.floor(32 * multiplier)), 84, 44 - Mth.floor(31 * multiplier), 5, Mth.floor(32 * multiplier));

            if (update) {
                if (!anySelected) partialUpdate = false;
                if (partialUpdate) partialClearEnchantments();
                else clearEnchantments();
                scrollPosition = 0;
            }
            if (hasRecipes && update) {
                int i = 0;
                for (EnchantmentRecipe recipe : recipes) {
                    addEnchantment(recipe, leftPos + 68, topPos + 8 + (i * 19), i);
                    i++;
                }
                if (partialUpdate) {
                    if (anySelected && enchantments.get(selectedTile) != null) {
                        Enchantment enchantment = enchantments.get(selectedTile).recipe.enchantment;
                        if (EnchantmentHelper.getEnchantments(itemStack).containsKey(enchantment)) {
                            if (enchantment.getMaxLevel() <= EnchantmentHelper.getEnchantments(itemStack).get(enchantment)) {
                                anySelected = false;
                                selectedTile = 0;
                                NetworkManager.sendToServer(EnchantingScreenRemoveRecipe.Id, new EnchantingScreenRemoveRecipe(getMenu().containerId).write());
                            }
                        }
                    } else {
                        anySelected = false;
                        selectedTile = 0;
                        NetworkManager.sendToServer(EnchantingScreenRemoveRecipe.Id, new EnchantingScreenRemoveRecipe(getMenu().containerId).write());
                    }
                }
            }

            if (!enchantments.isEmpty()) {

                EnchantmentTile tile = enchantments.get(selectedTile);
                EnchantmentRecipe recipe = recipes.get(selectedTile);
                boolean withinCapacity = AArcanaEnchantmentHelper.testEnchantmentCost(stack, AArcanaEnchantmentHelper.getEnchantmentCost(recipe.enchantment));

                if (anySelected && !tile.locked && !tile.maxLevel && withinCapacity && !tile.incompatible) {
                    if (AscendantArcana.config.disable_xp) {
                        context.blit(OVERLAYS, panelX + 1, panelY + 105, 12, 8, 9, 7);
                    } else {
                        if (getMenu().player.experienceLevel < recipe.levelCost) {
                            context.blit(OVERLAYS, panelX + 2, panelY + 105, 19, 0, 6, 7);
                        } else {
                            context.blit(OVERLAYS, panelX + 2, panelY + 105, 12, 0, 7, 7);
                        }
                        context.blit(OVERLAYS, panelX + 1, panelY + 113, 12, 8, 9, 7);
                    }
                } else if (anySelected && (tile.locked || !withinCapacity || tile.incompatible)) {
                    context.blit(OVERLAYS, panelX + 2, panelY + 47, 135, 0, 56, 57);
                    context.pose().pushPose();
                    context.pose().last().pose().scale(0.5F, 0.5F, 0.5F);
                    MutableComponent text = Component.empty();
                    if (recipe.enchantment.isTreasureOnly() && tile.locked) {
                        text = Component.translatable("gui.enchanting.treasure");
                    } else if (AArcanaEnchantmentHelper.getRequiredEnchantmentPower(recipe.enchantment) > getMenu().enchantmentPower[0] && tile.locked) {
                        text = Component.translatable("gui.enchanting.low_level");
                    } else if (!withinCapacity) {
                        text = Component.translatable("gui.enchanting.max_capacity");
                    } else if (tile.incompatible) {
                        text = Component.translatable("gui.enchanting.incompatible");
                    }
                    context.drawWordWrap(font, text, scaledPanelX + 4, scaledPanelY + 100, scaledPanelWidth, 5592405);
                    context.pose().popPose();
                } else {
                    context.blit(OVERLAYS, panelX + 2, panelY + 47, 191, 0, 56, 57);
                }
                context.pose().pushPose();
                context.pose().last().pose().scale(0.5F, 0.5F, 0.5F);
                if (anySelected) {
                    MutableComponent enchantmentTitle = Component.translatable(recipe.enchantment.getDescriptionId()).withStyle(ChatFormatting.UNDERLINE);
                    MutableComponent enchantmentDescription = Component.translatable(recipe.enchantment.getDescriptionId() + ".desc");
                    if (tile.locked) {
                        enchantmentTitle.withStyle(ChatFormatting.OBFUSCATED);
                        enchantmentDescription.withStyle(ChatFormatting.OBFUSCATED);
                    }
                    context.drawCenteredString(font, enchantmentTitle, scaledPanelX + 60, scaledPanelY + 2, 16777215);
                    context.drawWordWrap(font, enchantmentDescription, scaledPanelX + 2, scaledPanelY + 14, scaledPanelWidth, 5592405);
                    if (!tile.locked && !tile.maxLevel && withinCapacity && !tile.incompatible) {
                        int scrapColor = 16777215;
                        ItemStack scrapStack = getMenu().getSlot(1).getItem();
                        if (scrapStack.getCount() < recipe.magicalScrapCost) {
                            scrapColor = 11141120;
                        }
                        context.drawWordWrap(font, Component.translatable("gui.enchanting.item_cost", recipe.magicalScrapCost, Component.translatable(AArcanaItems.ENCHANTED_SCRAP.get().getDescriptionId())), scaledPanelX + 42, scaledPanelY + 102, 76,scrapColor);
                        ItemStack primaryItemStack = getMenu().getSlot(2).getItem();
                        if (recipe.primaryIngredientStack != null) {
                            int color = 16777215;
                            if (!recipe.primaryIngredientStack.getIngredient().test(primaryItemStack) || primaryItemStack.getCount() < recipe.primaryIngredientStack.getCount()) {
                                color = 11141120;
                            }
                            context.drawWordWrap(font, Component.translatable("gui.enchanting.item_cost", recipe.primaryIngredientStack.getCount(), Component.translatable(recipe.primaryIngredientStack.getIngredient().getItems()[0].getDescriptionId())), scaledPanelX + 42, scaledPanelY + 138, 76, color);
                        }
                        ItemStack secondaryItemStack = getMenu().getSlot(3).getItem();
                        if (recipe.secondaryIngredientStack != null) {
                            int color = 16777215;
                            if (!recipe.secondaryIngredientStack.getIngredient().test(secondaryItemStack) || secondaryItemStack.getCount() < recipe.secondaryIngredientStack.getCount()) {
                                color = 11141120;
                            }
                            context.drawWordWrap(font, Component.translatable("gui.enchanting.item_cost", recipe.secondaryIngredientStack.getCount(), Component.translatable(recipe.secondaryIngredientStack.getIngredient().getItems()[0].getDescriptionId())), scaledPanelX + 42, scaledPanelY + 174, 76, color);
                        }
                        if (AscendantArcana.config.disable_xp) {
                            font.drawInBatch8xOutline(Component.literal(String.valueOf(AArcanaEnchantmentHelper.getEnchantmentCost(recipe.enchantment))).getVisualOrderText(), scaledPanelX + 14, scaledPanelY + 216, 16733525, 0, context.pose().last().pose(), context.bufferSource(), 15728880);
                        } else {
                            font.drawInBatch8xOutline(Component.literal(String.valueOf(recipe.levelCost)).getVisualOrderText(), scaledPanelX + 12, scaledPanelY + 216, 5635925, 0, context.pose().last().pose(), context.bufferSource(), 15728880);
                            font.drawInBatch8xOutline(Component.literal(String.valueOf(AArcanaEnchantmentHelper.getEnchantmentCost(recipe.enchantment))).getVisualOrderText(), scaledPanelX + 14, scaledPanelY + 232, 16733525, 0, context.pose().last().pose(), context.bufferSource(), 15728880);
                        }
                        context.pose().popPose();

                        boolean buttonEnabled = AscendantArcana.config.disable_xp || recipe.levelCost <= getMenu().player.experienceLevel;
                        if (recipe.magicalScrapCost > 0) {
                            if (!scrapStack.is(AArcanaItems.ENCHANTED_SCRAP.get()) || recipe.magicalScrapCost > scrapStack.getCount()) buttonEnabled = false;
                        }
                        if (recipe.primaryIngredientStack != null) {
                            if (!recipe.primaryIngredientStack.getIngredient().test(primaryItemStack)) buttonEnabled = false;
                            else if (recipe.primaryIngredientStack.getCount() > primaryItemStack.getCount()) buttonEnabled = false;
                        }
                        if (recipe.secondaryIngredientStack != null) {
                            if (!recipe.secondaryIngredientStack.getIngredient().test(secondaryItemStack)) buttonEnabled = false;
                            else if (recipe.secondaryIngredientStack.getCount() > secondaryItemStack.getCount()) buttonEnabled = false;
                        }

                        if (update || enchantingButtonEnabled != buttonEnabled) {
                            removeWidget(enchantingButton);
                            enchantingButton = null;
                        }
                        if (enchantingButton == null) {
                            enchantingButtonEnabled = buttonEnabled;
                            enchantingButton = new LetsGoEnchantingButton(leftPos + 193, topPos + 116, buttonEnabled);
                            addRenderableWidget(enchantingButton);
                        }
                    } else {
                        removeWidget(enchantingButton);
                        enchantingButton = null;
                        context.pose().popPose();
                    }
                } else {
                    removeWidget(enchantingButton);
                    enchantingButton = null;
                    context.drawCenteredString(font, Component.translatable(itemStack.getDescriptionId()).withStyle(ChatFormatting.UNDERLINE), scaledPanelX + 60, scaledPanelY + 2, 16777215);
                    context.drawWordWrap(font, Component.translatable("gui.enchanting.no_selection_body"), scaledPanelX + 2, scaledPanelY + 14, scaledPanelWidth, 5592405);
                    context.pose().popPose();
                }
            }
        } else {
            removeWidget(enchantingButton);
            enchantingButton = null;
            clearEnchantments();
            context.blit(OVERLAYS, panelX + 2, panelY + 47, 191, 0, 56, 57);
            context.pose().pushPose();
            context.pose().last().pose().scale(0.5F, 0.5F, 0.5F);
            context.drawCenteredString(font, Component.translatable("gui.enchanting.no_item_title").withStyle(ChatFormatting.UNDERLINE), scaledPanelX + 60, scaledPanelY + 2, 16777215);
            context.drawWordWrap(font, Component.translatable("gui.enchanting.no_item_body"), scaledPanelX + 2, scaledPanelY + 14, scaledPanelWidth, 5592405);
            context.pose().popPose();
        }
    }

    public void addEnchantment(EnchantmentRecipe recipe, int buttonX, int buttonY, int i) {
        boolean locked = false;
        int power = getMenu().enchantmentPower[0];
        int requiredPower = AArcanaEnchantmentHelper.getRequiredEnchantmentPower(recipe.enchantment);
        if (power < requiredPower) locked = true;
        if (recipe.enchantment.isTreasureOnly() && !getMenu().unlockedTreasures.contains(recipe.enchantment)) locked = true;
        Map<Enchantment, Integer> appliedEnchantments = EnchantmentHelper.getEnchantments(lastItem);
        boolean incompatible = !EnchantmentHelper.isEnchantmentCompatible(appliedEnchantments.keySet(), recipe.enchantment);
        if (appliedEnchantments.containsKey(recipe.enchantment)) incompatible = false;
        EnchantmentTile tile = new EnchantmentTile(recipe, buttonX, buttonY, i, locked, incompatible);
        enchantments.add(tile);
        addRenderableWidget(tile);
    }

    public void partialClearEnchantments() {
        for (EnchantmentTile tile : enchantments) {
            removeWidget(tile);
        }
        enchantments.clear();
    }

    public void clearEnchantments() {
        partialClearEnchantments();
        selectedTile = 0;
        anySelected = false;
    }

    @Override
    public void render(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        renderBackground(drawContext);
        super.render(drawContext, mouseX, mouseY, delta);
        renderTooltip(drawContext, mouseX, mouseY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        scrollerClicked = false;
        if (recipes != null && recipes.size() > 6) {
            int i = this.leftPos + 152;
            int j = this.topPos + 8;

            if (mouseX >= (double)i && mouseX < (double)(i + 9) && mouseY >= (double)j && mouseY < (double)(j + 123)) {
                this.scrollerClicked = true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.scrollerClicked && recipes != null && !recipes.isEmpty() && recipes.size() > 6) {
            int j = this.topPos + 9;
            int k = j + 121;
            this.scrollPosition = ((float)mouseY - (float)j - 7.5F) / ((float)(k - j) - 15.0F);
            this.scrollPosition = Mth.clamp(this.scrollPosition, 0.0F, 1.0F);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (recipes != null && !recipes.isEmpty() && recipes.size() > 6) {
            int recipeSize = recipes.size();
            float f = (float)amount / (float)-recipeSize;
            this.scrollPosition = Mth.clamp(this.scrollPosition - f, 0.0F, 1.0F);
        }

        return true;
    }

    private class EnchantmentTile extends AbstractButton {
        private final EnchantmentRecipe recipe;
        private final int i;
        protected boolean locked;
        protected boolean maxLevel = false;
        protected boolean incompatible = false;
        private int offset = 0;

        public EnchantmentTile(EnchantmentRecipe recipe, int x, int y, int i, boolean locked, boolean incompatible) {
            super(x, y, 0, 27, Component.translatable(recipe.enchantment.getDescriptionId()));
            this.locked = locked;
            this.incompatible = incompatible;
            this.i = i;
            this.recipe = recipe;
            this.width = 84;
            this.height = 19;
            if (lastItem.isEnchanted()) {
                Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(lastItem);
                if (enchants.containsKey(recipe.enchantment) && enchants.get(recipe.enchantment) == recipe.enchantment.getMaxLevel()) this.maxLevel = true;
            }
        }

        @Override
        protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
            int v = 27;
            if (selectedTile == i && anySelected) v += 57;
            if (locked || incompatible) v += 19;
            else if (maxLevel) v += 38;


            context.blit(OVERLAYS, getX(), getY(), 0, v + offset, width, getHeight());

            if (getHeight() == 0) return;
            if (height == getHeight() && !locked && !maxLevel) {
                if (AscendantArcana.config.disable_xp) {
                    context.blit(OVERLAYS, getX() + 74, getY() + 11, 12, 8, 9, 7);
                } else {
                    if (recipe.levelCost > getMenu().player.experienceLevel) {
                        context.blit(OVERLAYS, getX() + 76, getY() + 11, 19, 0, 6, 7);
                    } else {
                        context.blit(OVERLAYS, getX() + 76, getY() + 11, 12, 0, 7, 7);
                    }
                    context.blit(OVERLAYS, getX() + 66, getY() + 11, 12, 8, 9, 7);
                }
            }
            context.pose().pushPose();
            context.pose().last().pose().scale(0.5F, 0.5F, 0.5F);
            Matrix4f positionMatrix = context.pose().last().pose();
            int scaledX = getX() * 2;
            int scaledY = getY() * 2;
            MutableComponent enchantComponent = Component.translatable(recipe.enchantment.getDescriptionId());
            if (enchantComponent.getString().length() > 15) enchantComponent = Component.literal(enchantComponent.getString(14)).append("...");
            if (locked) enchantComponent.withStyle(ChatFormatting.OBFUSCATED);
            if (getHeight() > 6) font.drawInBatch(enchantComponent, scaledX + 12, scaledY + 4, 5592405, false, positionMatrix, context.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
            MutableComponent levelText = null;
            if (maxLevel && !locked && !incompatible) levelText = Component.translatable("gui.enchanting.max_level");
            else if (!locked && height == getHeight()) {
                int level = 1;
                if (lastItem.isEnchanted() && EnchantmentHelper.getEnchantments(lastItem).containsKey(recipe.enchantment)) level = EnchantmentHelper.getEnchantments(lastItem).get(recipe.enchantment) + 1;
                levelText = Component.translatable("gui.enchanting.level", Component.translatable("enchantment.level." + level), Component.translatable("enchantment.level." + recipe.enchantment.getMaxLevel()));
            }
            if (levelText != null) font.drawInBatch(levelText, scaledX + 12, scaledY + 14, 5592405, false, positionMatrix, context.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);

            ItemStack magicalScraps = new ItemStack(AArcanaItems.ENCHANTED_SCRAP.get(), recipe.magicalScrapCost);

            if (!maxLevel && !locked && !incompatible && (getHeight() > 10 && offset == 0)) {
                int itemX = scaledX + 108;
                int itemY = scaledY + 4;
                context.renderItem(magicalScraps, itemX, itemY);
                context.renderItemDecorations(font, magicalScraps, itemX, itemY);
                if (recipe.primaryIngredientStack != null) {
                    Item primaryIngredientItem = recipe.primaryIngredientStack.getIngredient().getItems()[0].getItem();
                    ItemStack primaryIngredient = new ItemStack(primaryIngredientItem, recipe.primaryIngredientStack.getCount());
                    context.renderItem(primaryIngredient, itemX + 20, itemY);
                    context.renderItemDecorations(font, primaryIngredient, itemX + 20, itemY);
                }
                if (recipe.secondaryIngredientStack != null) {
                    Item secondaryIngredientItem = recipe.secondaryIngredientStack.getIngredient().getItems()[0].getItem();
                    ItemStack secondaryIngredient = new ItemStack(secondaryIngredientItem, recipe.secondaryIngredientStack.getCount());
                    context.renderItem(secondaryIngredient, itemX + 40, itemY);
                    context.renderItemDecorations(font, secondaryIngredient, itemX + 40, itemY);
                }
                if (getHeight() == height) {
                    if (AscendantArcana.config.disable_xp) {
                        font.drawInBatch8xOutline(Component.literal(String.valueOf(AArcanaEnchantmentHelper.getEnchantmentCost(recipe.enchantment))).getVisualOrderText(), scaledX + 160, scaledY + 28, 16733525, 0, positionMatrix, context.bufferSource(), 15728880);
                    } else {
                        if (recipe.levelCost < 10) {
                            font.drawInBatch8xOutline(Component.literal(String.valueOf(recipe.levelCost)).getVisualOrderText(), scaledX + 160, scaledY + 28, 5635925, 0, positionMatrix, context.bufferSource(), 15728880);
                        } else {
                            font.drawInBatch8xOutline(Component.literal(String.valueOf(recipe.levelCost)).getVisualOrderText(), scaledX + 154, scaledY + 28, 5635925, 0, positionMatrix, context.bufferSource(), 15728880);
                        }
                        font.drawInBatch8xOutline(Component.literal(String.valueOf(AArcanaEnchantmentHelper.getEnchantmentCost(recipe.enchantment))).getVisualOrderText(), scaledX + 144, scaledY + 28, 16733525, 0, positionMatrix, context.bufferSource(), 15728880);
                    }
                }
            }

            context.pose().popPose();
        }

        @Override
        public int getHeight() {
            if (recipes.size() < 6) return super.getHeight();
            int height = super.getHeight();
            int absolutePositionTop = i * height;
            int absolutePositionBottom = i * height + height;
            int absoluteMax = recipes.size() * height;
            int absoluteScrollPositionTop = (int) ((absoluteMax * scrollPosition) - (122 * scrollPosition));
            int absoluteScrollPositionBottom = absoluteScrollPositionTop + 122;
            if (absoluteScrollPositionTop > absolutePositionTop) {
                int difference = absoluteScrollPositionTop - absolutePositionTop;
                height -= difference;
                offset = difference;
            } else if (absoluteScrollPositionBottom < absolutePositionBottom) {
                int difference = absolutePositionBottom - absoluteScrollPositionBottom;
                height -= difference;
                offset = 0;
            } else {
                offset = 0;
            }
            return Math.max(height, 0);
        }

        @Override
        public int getY() {
            int absoluteMax = recipes.size() * height;
            int absoluteScrollPositionTop = (int) ((absoluteMax * scrollPosition) - (122 * scrollPosition));
            return super.getY() + offset - absoluteScrollPositionTop;
        }


        @Override
        protected boolean clicked(double mouseX, double mouseY) {
            return this.active && this.visible && mouseX >= (double)this.getX() && mouseY >= (double)this.getY() && mouseX < (double)(this.getX() + this.width) && mouseY < (double)(this.getY() + this.getHeight());
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }

        @Override
        public void onPress() {
            if (getHeight() == 0) return;
            if (selectedTile == i && anySelected) {
                selectedTile = 0;
                anySelected = false;
                NetworkManager.sendToServer(EnchantingScreenRemoveRecipe.Id, new EnchantingScreenRemoveRecipe(getMenu().containerId).write());
            } else {
                selectedTile = i;
                anySelected = true;
                NetworkManager.sendToServer(EnchantingScreenSendRecipe.Id, new EnchantingScreenSendRecipe(getMenu().containerId, recipe).write());
            }
        }
    }

    private class LetsGoEnchantingButton extends AbstractButton {
        private final boolean enabled;

        public LetsGoEnchantingButton(int x, int y, boolean enabled) {
            super(x, y, 26, 12, Component.translatable("gui.enchanting.enchant"));
            this.enabled = enabled;
        }

        @Override
        protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
            int u = 83;
            if (!enabled) u = 109;
            context.blit(OVERLAYS, getX(), getY(), u, 0, getWidth(), getHeight());
            context.pose().pushPose();
            context.pose().last().pose().scale(0.5F);
            context.drawCenteredString(font, getTitle(), (getX() + 13) * 2, (getY() + 4) * 2, 16777215);
            context.pose().popPose();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }

        @Override
        public void onPress() {
            if (!enabled) return;
            assert minecraft != null;
            assert minecraft.gameMode != null;
            updateEnchantments = true;
            minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, 0);
        }
    }
}
