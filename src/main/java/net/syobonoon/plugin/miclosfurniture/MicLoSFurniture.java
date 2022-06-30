package net.syobonoon.plugin.miclosfurniture;

import org.bukkit.plugin.java.JavaPlugin;

public class MicLoSFurniture extends JavaPlugin {
	public static Config config;

    @Override
    public void onEnable() {
    	config = new Config(this);
    	new ItemPlace(this);
    	getCommand("dainaif").setExecutor(new FurnitureCommand());
    	getCommand("dainaifreload").setExecutor(new FurnitureCommand());
    	getCommand("dp").setExecutor(new FurnitureCommand());
    	getLogger().info("onEnable");
    }

}
