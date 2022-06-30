package net.syobonoon.plugin.miclosfurniture;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import jp.jyn.jecon.Jecon;

public class FunctionalFurniture {
	private Plugin plugin;
	Random random = new Random();
	private Jecon jecon;

	public FunctionalFurniture(Plugin plugin, Jecon jecon) {
		this.plugin = plugin;
		this.jecon = jecon;
	}

	//自動販売機を使う
	public void use_vending(Player p) {

		List<String> keyfoods = new ArrayList<>(MicLoSFurniture.config.getFoodsItemStackMap().keySet());
		String drink_name = keyfoods.get(random.nextInt(keyfoods.size()));
		ItemStack drink_item = MicLoSFurniture.config.getFoodsItemStackMap().get(drink_name);

		//購入処理
		double price_drinks = 100;
		double user_balance = jecon.getRepository().getDouble(p.getUniqueId()).getAsDouble();
		double user_buy_balance = user_balance - price_drinks;

		if (user_buy_balance < 0) {
			p.sendMessage(ChatColor.RED+"所持金が足りません");
			return;
		}

		jecon.getRepository().set(p.getUniqueId(), user_buy_balance);

		p.getInventory().addItem(drink_item);
		p.sendMessage(ChatColor.AQUA+drink_name+ChatColor.GRAY + "を"+price_drinks+"円で購入しました");
		p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);
		return;
	}

	//椅子を使う
	public void ride_furniture(Player p, ArmorStand stand) {
		stand.addPassenger(p);
		return;
	}

	//白い狐を使う
	public void use_doll_fox_snow(Player p) {
		int ran_sound = random.nextInt(MicLoSFurniture.config.num_fox_sound);
		p.playSound(p.getLocation(), MicLoSFurniture.config.fox_sound_list[ran_sound], 1F, 1F);
		return;
	}

	//飲み物を飲む
	public void use_drinks(Player p, ItemStack furniture_helmet, ArmorStand stand) {
		if (furniture_helmet.getItemMeta().getDisplayName().equals("beer_mug")) {
			ItemStack beer_mug_empty = MicLoSFurniture.config.getFutnitureItemStack().get("beer_mug_empty");
			stand.getEquipment().setHelmet(beer_mug_empty);

			p.getLocation().getWorld().spawnParticle(Particle.VILLAGER_HAPPY, p.getLocation(), 30, 1, 1, 1, 1);
			p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_DRINK_POTION, 1F, 1F);
			p.getPlayer().setHealth(20.0);
		} else if(furniture_helmet.getItemMeta().getDisplayName().equals("coffee_cup_houston")) {
			ItemStack beer_mug_empty = MicLoSFurniture.config.getFutnitureItemStack().get("empty_cup_houston");
			stand.getEquipment().setHelmet(beer_mug_empty);

			p.getLocation().getWorld().spawnParticle(Particle.VILLAGER_HAPPY, p.getLocation(), 30, 1, 1, 1, 1);
			p.playSound(p.getLocation(), Sound.ENTITY_WANDERING_TRADER_DRINK_POTION, 1F, 1F);
			p.getPlayer().setHealth(20.0);
		}
		return;
	}

	//スロットを使う
	public void use_slot(Player p, ArmorStand stand) {
		int ran_slot = random.nextInt(10000);
		double user_balance = jecon.getRepository().getDouble(p.getUniqueId()).getAsDouble();
		double user_balance_afterslot = user_balance - 1000.0; //1回スロット
		double getMoney = 0;

		if (user_balance_afterslot < 0) {
			p.sendMessage(ChatColor.RED+"所持金が足りません");
			return;
		}

		if (ran_slot == 16) {
			MicLoSFurniture.config.entireMessage(p.getName()+"さんがスロットで100万当てました。おめでとうございます！！", ChatColor.GREEN);
			p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
			p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1F, 1F);
			getMoney = 1000000.0;
		}else if (1 <= ran_slot && ran_slot < 6) {
			p.sendMessage(ChatColor.BLUE+"10万円当たり！");
			p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
			getMoney = 100000.0;
		}else if (6 <= ran_slot && ran_slot < 16) {
			p.sendMessage(ChatColor.YELLOW+"1万円当たり！");
			p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
			getMoney = 10000.0;
		}else if (17 <= ran_slot && ran_slot < 100) {
			p.sendMessage(ChatColor.AQUA+"3000円当たり！");
			p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
			getMoney = 3000.0;
		}else if (100 <= ran_slot && ran_slot < 2100) {
			p.sendMessage(ChatColor.WHITE+"1000円当たり！");
			p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
			getMoney = 1000.0;
		}else{
			p.sendMessage(ChatColor.DARK_GRAY+"はずれ...");
		}

		jecon.getRepository().set(p.getUniqueId(), user_balance_afterslot+getMoney);
		return;
	}

}
