package net.syobonoon.plugin.miclosinquirer;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InquirerCommand implements TabExecutor {

	@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		boolean isSuccess = false;
		if (command.getName().equalsIgnoreCase("dinq")) {
			isSuccess = dinq(sender, args);
		}else if (command.getName().equalsIgnoreCase("dainaiinqreload")) {
			isSuccess = dainaiinqreload(sender, args);
		}
		return isSuccess;
	}

	@Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return null;
	}

	private boolean dinq(CommandSender sender, String[] args) {
		if (!sender.hasPermission("miclosinquirer.dinq")) return false;
		if (!(sender instanceof Player)) return false;

		Player p = (Player) sender;
		if (args.length != 0) {
			p.sendMessage(ChatColor.RED + "parameter error");
			return false;
		}

		ItemStack inquirer_item = MicLoSInquirer.config.getInquirer();

		p.getInventory().addItem(inquirer_item);
		p.sendMessage(ChatColor.GRAY + "You get a inquirer.");
		return true;
	}

	//dainaiinqreload:configをreloadする
	private boolean dainaiinqreload(CommandSender sender, String[] args) {
		if (!sender.hasPermission("miclosinquirer.reload")) return false;
		if (args.length != 0) return false;
		MicLoSInquirer.config.load_config();
		return true;
	}
}
