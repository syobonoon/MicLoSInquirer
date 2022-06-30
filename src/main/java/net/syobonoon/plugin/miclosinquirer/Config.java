package net.syobonoon.plugin.miclosinquirer;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class Config {
	private Plugin plugin;
	private FileConfiguration config = null;
	public static final Material INQUIRER_BASEITEM = Material.ARMOR_STAND;
	public static final int INQUIRER_CUSTOM_NUM = 1;
	public static final int MAX_GUI = 53;
	public static final String INQUIRER_GUI_MESSAGE = "INQUIRER GUI";
	public static final String EMPTY_USER_MESSAGE = "登録されていません";
	private ItemStack inquirer_item = null;

	public Config(Plugin plugin) {
		this.plugin = plugin;
		load_inquire();
		load_config();
	}

	public void load_config() {
		plugin.saveDefaultConfig();
		if (config != null) {
			plugin.reloadConfig();
			plugin.getServer().broadcastMessage(ChatColor.GREEN+"MicLoSInquirer reload completed");
		}
		config = plugin.getConfig();
	}

	private void load_inquire() {
		inquirer_item = new ItemStack(INQUIRER_BASEITEM);
		ItemMeta inquirer_itemMeta = inquirer_item.getItemMeta();

		inquirer_itemMeta.setDisplayName(ChatColor.WHITE + "妖精");

		List<String> inquirer_itemLore = new ArrayList<>();
		inquirer_itemLore.add(ChatColor.WHITE + "FキーでGUIを開いて探したい人を登録してね。");
		inquirer_itemLore.add(ChatColor.WHITE + "登録したら右クリックで探したい人に向かって飛んでいくよ!");
		inquirer_itemLore.add(ChatColor.WHITE + "登録:" + EMPTY_USER_MESSAGE);
		inquirer_itemMeta.setLore(inquirer_itemLore);
		inquirer_itemMeta.setCustomModelData(INQUIRER_CUSTOM_NUM);
		inquirer_item.setItemMeta(inquirer_itemMeta);
	}

	public ItemStack getInquirer() {
		return this.inquirer_item;
	}

	public int getInt(String key) {
		return config.getInt(key);
	}

	public boolean getBoolean(String key) {
		return config.getBoolean(key);
	}

	public String getString(String key) {
		return config.getString(key);
	}
}
