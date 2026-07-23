package me.anticode.ascendant_arcana.client;

import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import me.anticode.ascendant_arcana.client.model.entity.SingularityModel;
import me.anticode.ascendant_arcana.client.render.entity.BlazeboltEntityRenderer;
import me.anticode.ascendant_arcana.client.render.entity.SingularityEntityRenderer;
import me.anticode.ascendant_arcana.init.AArcanaEntities;
import me.anticode.ascendant_arcana.AscendantArcana;
import me.anticode.ascendant_arcana.api.EnchantedTrident;
import me.anticode.ascendant_arcana.networking.AddParticlesPacket;
import me.anticode.ascendant_arcana.networking.EnchantingScreenSync;
import me.anticode.ascendant_arcana.networking.ForgeTridentSync;
import me.anticode.ascendant_arcana.screenhandler.AArcanaEnchantingMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, AddParticlesPacket.Id, (buf, context) -> {
            AddParticlesPacket packet = AddParticlesPacket.read(buf);
            RandomSource random = context.getPlayer().getRandom();
            for (int i = 0; i < packet.count(); i++) {
                double x = packet.pos().x + Mth.randomBetween(random, -packet.posVariance(), packet.posVariance());
                double y = packet.pos().y + Mth.randomBetween(random, -packet.posVariance(), packet.posVariance());
                double z = packet.pos().z + Mth.randomBetween(random, -packet.posVariance(), packet.posVariance());
                double velX = (packet.vel().x + Mth.randomBetween(random, -packet.velVariance(), packet.velVariance())) * packet.speed();
                double velY = (packet.vel().y + Mth.randomBetween(random, -packet.velVariance(), packet.velVariance())) * packet.speed();
                double velZ = (packet.vel().z + Mth.randomBetween(random, -packet.velVariance(), packet.velVariance())) * packet.speed();
                context.getPlayer().level().addParticle(packet.particleOptions(), x, y, z, velX, velY, velZ);
            }
        });

        EntityRendererRegistry.register(AArcanaEntities.BLAZEBOLT_ENTITY, BlazeboltEntityRenderer::new);
        EntityModelLayerRegistry.register(SingularityModel.LAYER_LOCATION, SingularityModel::createBodyLayer);
        EntityRendererRegistry.register(AArcanaEntities.SINGULARITY_ENTITY, SingularityEntityRenderer::new);
    }
}
