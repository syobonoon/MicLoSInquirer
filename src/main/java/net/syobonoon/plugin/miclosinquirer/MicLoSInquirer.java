package net.syobonoon.plugin.miclosinquirer;

import org.bukkit.plugin.java.JavaPlugin;

public class MicLoSInquirer extends JavaPlugin {
	public static Config config;

	@Override
	public void onEnable() {
		config = new Config(this);
		new InquirerEvent(this);
		getCommand("dinq").setExecutor(new InquirerCommand());
		getCommand("dainaiinqreload").setExecutor(new InquirerCommand());
		getLogger().info("onEnable");
	}
}
