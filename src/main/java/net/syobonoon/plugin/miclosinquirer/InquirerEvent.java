package net.syobonoon.plugin.miclosinquirer;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class InquirerEvent implements Listener{
	private Plugin plugin;

	public InquirerEvent(Plugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
	}

	//GUIを開く関数
	@EventHandler
	public void inquirerGUI(PlayerSwapHandItemsEvent e) {
		Player p = e.getPlayer();

		if (!isInquirerItem(e.getOffHandItem())) return;
		e.setCancelled(true);
		Inventory inv = Bukkit.createInventory(null, Config.MAX_GUI+1, Config.INQUIRER_GUI_MESSAGE);

		//サーバーにいるユーザーをGUIに追加していく
		int i = 0;
		List<Player> online_players = new ArrayList<>(Bukkit.getOnlinePlayers());
		for (Player online_player : online_players) {
			if (i > Config.MAX_GUI) break;

			if (online_player.equals(p)) continue;
			ItemStack skull_item = playerHeadItemStack(online_player);
			inv.setItem(i, skull_item);
			i++;
		}

		p.openInventory(inv);
		return;
	}

	//尋ね人アイテムにユーザーを登録する関数
	@EventHandler
    public void onInventoryEvent(InventoryClickEvent e) {
		Player p = (Player)e.getWhoClicked();

		if (!(e.isLeftClick() || e.isRightClick())) return;

		//設定GUIではなかった場合
		if (!e.getView().getTitle().equals(Config.INQUIRER_GUI_MESSAGE)) return;

		e.setCancelled(true);

		//設定GUIではないところをクリックした場合
		if (!(0 <= e.getRawSlot() && e.getRawSlot() <= Config.MAX_GUI)) return;

		//手に持っているアイテムが尋ね人ではなかった場合
		ItemStack inquirer_item = p.getInventory().getItemInMainHand();
		if (!isInquirerItem(inquirer_item)) return;
		ItemMeta inquirer_itemMeta = inquirer_item.getItemMeta();

		ItemStack clicked_item = e.getCurrentItem();
		String clicked_username = clicked_item.getItemMeta().getDisplayName();
		List<String> lore_update = inquirer_itemMeta.getLore();

		//クリックで登録
		if (clicked_username.equals("登録解除")) {
			lore_update.set(2, ChatColor.WHITE+"登録:"+Config.EMPTY_USER_MESSAGE);
			p.sendMessage(ChatColor.GRAY+"登録を解除しました");
		}else {
			lore_update.set(2, ChatColor.WHITE+"登録:"+ChatColor.AQUA+clicked_username);
			p.sendMessage(ChatColor.GRAY+"右クリックで"+ChatColor.AQUA+clicked_username+ChatColor.GRAY+"に向かって飛びます");
		}

		inquirer_itemMeta.setLore(lore_update);
		inquirer_item.setItemMeta(inquirer_itemMeta);
		p.closeInventory();
		return;
	}

	//右クリックで尋ね人を使用する関数
	@EventHandler
	public void useInquirer(PlayerInteractEvent e) {
		if (e.getHand().equals(EquipmentSlot.OFF_HAND)) return;

		if (!(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR))) return;

		Player p = e.getPlayer();
		ItemStack user_inquirer = p.getInventory().getItemInMainHand();
		if (!isInquirerItem(user_inquirer)) return;

		ItemMeta user_inquirer_itemMeta = user_inquirer.getItemMeta();
		List<String> lore_str = user_inquirer_itemMeta.getLore();
		String user_inquirer_name = ChatColor.stripColor(lore_str.get(2)).replace("登録:", "");//目的のユーザー

		//プレイヤーを登録してなかったら
		if (user_inquirer_name.equals(Config.EMPTY_USER_MESSAGE)) {
			p.sendMessage(ChatColor.GRAY+Config.EMPTY_USER_MESSAGE);
			e.setCancelled(true);
			return;
		}

		//プレイヤーがサーバーに存在しなかったら
		Player target = Bukkit.getServer().getPlayer(user_inquirer_name);
		if (target == null) {
			p.sendMessage(ChatColor.RED + user_inquirer_name+"がサーバーに存在しません");
			e.setCancelled(true);
			return;
		}

		ItemStack user_inquirer_clone = user_inquirer.clone();

		int user_item_amount = user_inquirer.getAmount();
		if(user_item_amount > 1) {
			user_inquirer.setAmount(user_item_amount - 1);
		}else{
			user_inquirer.setAmount(0);
		}

		ArmorStand inquirer = placeInquirer(p);
		flyingToPlayer(p, target, inquirer, user_inquirer_clone);//プレイヤーに向かって飛んでいく
		return;
	}

	//尋ね人をワールドにスポーンする関数
	private ArmorStand placeInquirer(Player p) {
		Location loc_p = p.getLocation();
		ArmorStand inquirer = loc_p.getWorld().spawn(loc_p, ArmorStand.class);
		inquirer.setSmall(true);
		inquirer.setVisible(false);
		return inquirer;
	}

	//プレイヤーに向かって飛んでいく関数loc_p.getDirection().multiply(projectiles_velocity)
	private void flyingToPlayer(Player p, Player target, ArmorStand inquirer, ItemStack user_inquirer_clone) {
		BukkitRunnable task = new BukkitRunnable() {
			Vector v;
			Location loc_inquirer;
			Location loc_target;
			int loop_cnt;

			public void run() {
				loc_inquirer = inquirer.getLocation();
				loc_target = target.getLocation();

				//尋ね人を使った人がログアウトしたら
				if(Bukkit.getServer().getPlayer(p.getName()) == null) {
					inquirer.remove();
					this.cancel();
				}

				//尋ね人が倒れた、距離があいた、時間がたった、ターゲットがログアウトした
				if (inquirer.isDead() || p.getLocation().distance(loc_inquirer) > 60 || loop_cnt > 20*300 || Bukkit.getServer().getPlayer(target.getName()) == null) {
					p.sendMessage(ChatColor.AQUA+target.getName()+ChatColor.GRAY+"さんが見つかりませんでした");

					inquirer.remove();
					p.getInventory().addItem(user_inquirer_clone);
					this.cancel();
				}else if(loc_target.distance(loc_inquirer) < 2) {
					p.sendMessage(ChatColor.AQUA+target.getName()+ChatColor.GRAY+"さんが見つかりました");
					inquirer.getWorld().playSound(loc_inquirer, Sound.ENTITY_CAT_AMBIENT, 2F, 1.5F);

					inquirer.remove();
					p.getInventory().addItem(user_inquirer_clone);
					this.cancel();
				}

				v = loc_target.toVector().subtract(loc_inquirer.toVector());
				inquirer.setVelocity(v.normalize().multiply(0.5));
				loc_inquirer.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc_inquirer, 30, 0.5, 0.5, 0.5, 1);
				loop_cnt++;
			}
		};
		task.runTaskTimer(plugin, 0, 1);
	}

	//プレイヤーからプレイヤーの頭を返す関数
	private ItemStack playerHeadItemStack(Player online_player) {
		ItemStack skull_item = new ItemStack(Material.PLAYER_HEAD, 1);
		SkullMeta skull_itemMeta = (SkullMeta) skull_item.getItemMeta();
		skull_itemMeta.setDisplayName(online_player.getName());
		skull_itemMeta.setOwningPlayer(online_player);
		skull_item.setItemMeta(skull_itemMeta);

		return skull_item;
	}

	//尋ね人アイテムかどうか確認する関数
	private boolean isInquirerItem(ItemStack inquirer_item) {
		if(inquirer_item == null || inquirer_item.getType() == Material.AIR) return false;

		if (!inquirer_item.getType().equals(Material.ARMOR_STAND) || !inquirer_item.getItemMeta().hasCustomModelData()) return false;
		return true;
	}
}
