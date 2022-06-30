package net.syobonoon.plugin.miclosfurniture;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class Config {
	private final Plugin plugin;
	private FileConfiguration config = null;
	private List<String> furniture_name_all = new ArrayList<String>();
	private Map<String, ItemStack> furniture_item_map = new LinkedHashMap<String, ItemStack>();
	private Map<String, ItemStack> foods_item_map = new LinkedHashMap<String, ItemStack>();
	private ItemStack admin_catalog = null;
	public final static double ACCESSORY_HEIGHT = 1;//机の上に乗せる場合の高さ
	public final static double ACCESSORY_POSITION = 0;//机の上に乗せる場合の位置XZ座標
	public final static double ACCESSORY_ONBLOCK_HEIGHT = 1;//ブロックの上に乗せる場合の高さ
	public final static double FURNITURE_HEIGHT = 1;//家具の高さ
	public final static double FURNITURE_POSITION = 0.5;//家具の位置XZ座標
	public final static Material ADMIN_CATALOG_MATERIAL = Material.SHULKER_SHELL; //スマホ用のMaterial
	public final static Material VEHICLE_MATERIAL = Material.LEATHER; //乗り物のMaterial
	public final static String ADMIN_CATALOG_NAME = "phone";
	public final static int ADMIN_CATALOG_CUSTOM_NUM = 1;
	public final static int MAX_CATALOG_GUI_NUM = 53;
	public final static double WALL_FURNITURE_HEIGHT = -1.19;
	public final static double WALL_FURNITURE_DIST = 0.6;
	public final static double CHAIR_HEIGHT = -0.11;
	public final static Material FOODS_MATERIAL = Material.APPLE;
	public final static Material ACCESSORY_MATERIAL = Material.CLAY_BALL;
	private String [] drinks_list = {"drink_coffeeboss","drink_coke","drink_gomipepushi","drink_gozentea","drink_mazai","drink_orange","drink_yaisotya"};
	public Sound [] fox_sound_list = {Sound.ENTITY_FOX_AGGRO, Sound.ENTITY_FOX_AMBIENT, Sound.ENTITY_FOX_BITE, Sound.ENTITY_FOX_DEATH, Sound.ENTITY_FOX_EAT, Sound.ENTITY_FOX_HURT, Sound.ENTITY_FOX_SCREECH, Sound.ENTITY_FOX_SLEEP, Sound.ENTITY_FOX_SNIFF, Sound.ENTITY_FOX_SPIT};
	public int num_fox_sound = fox_sound_list.length;

	public Config(Plugin plugin) {
		this.plugin = plugin;
		load_config();
		load_AdminCatalog();
		load_drinks();
	}

	public void load_config() {
		plugin.saveDefaultConfig();
		if (config != null) {
			plugin.reloadConfig();
			plugin.getServer().broadcastMessage(ChatColor.GREEN+"MicLoSFurniture reload completed");
		}
		config = plugin.getConfig();

		for (String basename : config.getKeys(false)) {

			for (String furniturename : config.getConfigurationSection(basename).getKeys(false)) {
				furniture_name_all.add(furniturename); //家具の名前を追加していく

				ItemStack furniture = new ItemStack(Material.matchMaterial(basename), 1);
				ItemMeta metafurniture = furniture.getItemMeta();
				metafurniture.setDisplayName(furniturename);
				metafurniture.setCustomModelData(config.getInt(basename +"."+ furniturename));
				furniture.setItemMeta(metafurniture);

				furniture_item_map.put(furniturename, furniture);
			}
		}
	}

	//飲み物食べ物をロードする関数
	public void load_drinks() {
		for (int i = 0; i < drinks_list.length; i++) {
			ItemStack food = new ItemStack(FOODS_MATERIAL, 1);
			ItemMeta metafood = food.getItemMeta();
			metafood.setDisplayName(drinks_list[i]);
			metafood.setCustomModelData(i+1);
			food.setItemMeta(metafood);

			foods_item_map.put(drinks_list[i], food);
		}
	}

	//管理者専用のカタログを作成する関数
	public void load_AdminCatalog() {
		admin_catalog = new ItemStack(ADMIN_CATALOG_MATERIAL, 1);
		ItemMeta metafurniture = admin_catalog.getItemMeta();
		metafurniture.setDisplayName(ADMIN_CATALOG_NAME);
		metafurniture.setCustomModelData(ADMIN_CATALOG_CUSTOM_NUM);
		admin_catalog.setItemMeta(metafurniture);
	}

	//家具の名前のリストを取得する関数
	public List<String> getFurnitureList() {
		return this.furniture_name_all;
	}

	//家具の名前と家具のItemStackのhashmapを取得する関数
	public Map<String, ItemStack> getFutnitureItemStack() {
		return this.furniture_item_map;
	}

	//管理者専用のカタログを取得する関数
	public ItemStack getAdminCatalog() {
		return this.admin_catalog;
	}

	//食べ物の名前と家具のItemStackのhashmapを取得する関数
	public Map<String, ItemStack> getFoodsItemStackMap() {
		return this.foods_item_map;
	}

	//テストメッセージを送る関数
	public void sendTestMessage(String test_str) {
		plugin.getServer().broadcastMessage(ChatColor.AQUA+test_str);
	}

	//全体メッセージを送る関数
	public void entireMessage(String str, ChatColor color) {
		plugin.getServer().broadcastMessage(color+str);
	}
}
