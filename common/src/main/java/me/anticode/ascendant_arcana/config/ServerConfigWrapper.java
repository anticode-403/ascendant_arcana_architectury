package me.anticode.ascendant_arcana.config;

import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Excluded;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;

@Config(name = "ascendant_arcana")
public class ServerConfigWrapper extends PartitioningSerializer.GlobalData {
    @Category("server")
    @Excluded
    public ServerConfig server = new ServerConfig();
}
