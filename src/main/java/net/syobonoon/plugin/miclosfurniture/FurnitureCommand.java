package net.syobonoon.plugin.miclosfurniture;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FurnitureCommand implements TabExecutor {
	private Map<String, ItemStack> furniture_item_map = new HashMap<String, ItemStack>();

	@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		boolean isSuccess = false;
		if (command.getName().equalsIgnoreCase("dainaif")) {
			isSuccess = dainaif(sender, args);
		}
		else if (command.getName().equalsIgnoreCase("dainaifreload")) {
			isSuccess = dainaifreload(sender, args);
		}
		else if (command.getName().equalsIgnoreCase("dp")) {
			isSuccess = dp(sender, args);
		}
		return isSuccess;
	}

	@Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1) return MicLoSFurniture.config.getFurnitureList();
        return null;
    }

	//dainaf:家具を出す
	private boolean dainaif(CommandSender sender, String[] args) {
		if (!sender.hasPermission("miclosfurniture.dainaif")) return false;
		if (!(sender instanceof Player)) return false;
		if (args.length == 0 || args.length >= 3) {
	        sender.sendMessage(ChatColor.RED + "parameter error");
	        return false;
	    }

		Player p = (Player) sender;
		furniture_item_map = MicLoSFurniture.config.getFutnitureItemStack();
		if (!furniture_item_map.containsKey(args[0])) {
			p.sendMessage(ChatColor.RED + args[0] + " is not exist.");
			return false;
		}

		ItemStack furniture = furniture_item_map.get(args[0]);

		int amount_furniture = 1;
		if (args.length == 2) amount_furniture = Integer.parseInt(args[1]);

		//ユーザーのインベントリに家具を加える
		for (int i = 0; i < amount_furniture; i++) p.getInventory().addItem(furniture);
		p.sendMessage(ChatColor.GRAY + "You get " + args[0]);

		return true;
	}

	//dainaifreload
	private boolean dainaifreload(CommandSender sender, String[] args) {
		if (!sender.hasPermission("miclosfurniture.dainaifreload")) return false;
		if (args.length != 0) return false;
		MicLoSFurniture.config.load_config();
		return true;
	}

	//dp:スマホを出す
	private boolean dp(CommandSender sender, String[] args) {
		if (!sender.hasPermission("miclosfurniture.dp")) return false;
		if (!(sender instanceof Player)) return false;
		if (!(args.length == 0)) {
	        sender.sendMessage(ChatColor.RED + "parameter error");
	        return false;
	    }

		Player p = (Player) sender;

		p.getInventory().addItem(MicLoSFurniture.config.getAdminCatalog());
		p.sendMessage(ChatColor.AQUA + "You get a phone.");

		return true;
	}

}