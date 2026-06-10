package me.anticode.ascendant_arcana.client;

import dev.architectury.networking.NetworkManager;
import me.anticode.ascendant_arcana.networking.EnchantingScreenSync;
import me.anticode.ascendant_arcana.screenhandler.AArcanaEnchantingMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;

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
    }
}
