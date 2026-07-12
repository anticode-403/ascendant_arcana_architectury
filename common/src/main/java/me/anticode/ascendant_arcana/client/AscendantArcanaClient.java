package me.anticode.ascendant_arcana.client;

import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import me.anticode.ascendant_arcana.client.render.entity.BlazeboltEntityRenderer;
import me.anticode.ascendant_arcana.init.AArcanaEntities;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.api.EnchantedTrident;
import me.anticode.ascendant_arcana.networking.EnchantingScreenSync;
import me.anticode.ascendant_arcana.networking.ForgeTridentSync;
import me.anticode.ascendant_arcana.screenhandler.AArcanaEnchantingMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;

@Environment(EnvType.CLIENT)
public class AscendantArcanaClient {
    public static void initialize() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, EnchantingScreenSync.Id, (buf, context) -> {
            EnchantingScreenSync packet = EnchantingScreenSync.read(buf);
            Player player = context.getPlayer();
            if (player.containerMenu.containerId != packet.syncId()) return;
            AArcanaEnchantingMenu menu = (AArcanaEnchantingMenu) player.containerMenu;
            menu.unlockedTreasures = packet.treasures();
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, ForgeTridentSync.Id, (buf, context) -> {
            ForgeTridentSync packet = ForgeTridentSync.read(buf);
            ThrownTrident trident;
            try {
                trident = (ThrownTrident) context.getPlayer().level().getEntity(packet.tridentEntityId());
            } catch (ClassCastException e) {
                AscendantArcana.LOGGER.warn("Thrown Trident ID not recognized!");
                return;
            }
            EnchantedTrident enchantedTrident = (EnchantedTrident) trident;
            enchantedTrident.ascendant_arcana$setClientStuckEntity(packet.stuckEntityId());
        });

        EntityRendererRegistry.register(AArcanaEntities.BLAZEBOLT_ENTITY, BlazeboltEntityRenderer::new);
    }
}
