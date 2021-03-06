package net.syobonoon.plugin.miclosfurniture;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import jp.jyn.jecon.Jecon;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

public class ItemPlace implements Listener{
	private Map<String, ItemStack> furniture_item_map = new HashMap<String, ItemStack>();
	private static HashMap<Player, Long> lastPress = new HashMap<>();
	private Plugin plugin;
	private FunctionalFurniture ff;
	private Jecon jecon;

	public ItemPlace(Plugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
		Plugin plugin_Jecon = Bukkit.getPluginManager().getPlugin("Jecon");
        if(plugin_Jecon == null || !plugin_Jecon.isEnabled()) {
            plugin.getLogger().warning("Jecon is not available.");
        }

        this.jecon = (Jecon) plugin_Jecon;
        this.ff = new FunctionalFurniture(plugin, this.jecon);
	}

	//?????????????????????
	@EventHandler
	public void furnitureplace(PlayerInteractEvent e) {

		if (e.getHand().equals(EquipmentSlot.OFF_HAND)) return;

		//????????????????????????????????????
		if (!(e.getAction().equals(Action.RIGHT_CLICK_BLOCK))) return;

		Player p = e.getPlayer();
		//?????????????????????????????????????????????????????????????????????
		ItemStack user_item = p.getInventory().getItemInMainHand();

		//???????????????????????????
		if(!(judgeFurniture(user_item))) return;

		//???????????????????????????????????????
		Block target_block = getClickedBlock(p);
		if (target_block == null) return;

		Location loc = target_block.getLocation();

		if (p.isInsideVehicle()) return;

		if (isVehicle(user_item)){ //??????????????????
			placeVehicle(loc, Config.FURNITURE_POSITION, 1, Config.FURNITURE_POSITION, user_item, p);
			return;
		}

		//?????????????????????????????????
		if (!canBuild(p, loc) || !iscanBuildGriefPrevention(p, loc)) {
			p.sendMessage(ChatColor.RED + "You can't put furniture here.");
			return;
		}

		if(user_item.getItemMeta().getDisplayName().startsWith("laptop_") || user_item.getItemMeta().getDisplayName().startsWith("pc_") || user_item.getItemMeta().getDisplayName().startsWith("book_") || user_item.getItemMeta().getDisplayName().startsWith("digital_") || user_item.getItemMeta().getDisplayName().startsWith("beer")) { //?????????????????????????????????
			placeArmorStand(loc, Config.FURNITURE_POSITION, Config.ACCESSORY_ONBLOCK_HEIGHT, Config.FURNITURE_POSITION, user_item, p); //???????????????????????????
		} else if (isWallFurniture(user_item)) { //???????????????
			placeWallFurniture(loc, 0, Config.WALL_FURNITURE_HEIGHT, 0, user_item, p);
		} else if (user_item.getItemMeta().getDisplayName().startsWith("chair_")) {
			placeArmorStand(loc, Config.FURNITURE_POSITION, Config.CHAIR_HEIGHT, Config.FURNITURE_POSITION, user_item, p);
		} else { //?????????????????????
			placeArmorStand(loc, Config.FURNITURE_POSITION, Config.FURNITURE_HEIGHT, Config.FURNITURE_POSITION, user_item, p);
		}
		return;
	}

	//???????????????????????????????????????????????????????????????
	@EventHandler
	public void detectArmorEvent(PlayerArmorStandManipulateEvent e) {
		ArmorStand stand = e.getRightClicked();

		//????????????????????????????????????????????????????????????????????????return
		ItemStack furniture_helmet = stand.getEquipment().getHelmet();
		if (!(judgeFurniture(furniture_helmet))) return;

		//?????????????????????????????????(?????????????????????)???????????????????????????
		if (!(e.getPlayerItem().getType().equals(Material.AIR)) || (!(e.getArmorStandItem().getType().equals(Material.AIR)))) {
			e.setCancelled(true);
		}
		return;
	}

	//???????????????????????????????????????????????????????????????????????????????????????
	@EventHandler
	public void preventBreakRide(PlayerInteractEvent e) {
		if (e.getHand().equals(EquipmentSlot.OFF_HAND)) return;

		Player p = e.getPlayer();
		if (!p.isInsideVehicle()) return;
		if (e.getAction().equals(Action.LEFT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) e.setCancelled(true);
		return;
	}

	//??????????????????????????????????????????
	@EventHandler
	public void furnitureCatalogGUI(PlayerInteractEvent e) {
		Player p = e.getPlayer();

		if (e.getHand().equals(EquipmentSlot.OFF_HAND)) return;

		//????????????????????????????????????
		if (!(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR))) return;

		if (!judgePhone(p.getInventory().getItemInMainHand())) return; //????????????????????????
		e.setCancelled(true);

		Inventory inv = Bukkit.createInventory(null, Config.MAX_CATALOG_GUI_NUM+1, Config.ADMIN_CATALOG_NAME);

		//???????????????????????????????????????????????????
		int i = 0;
		for (ItemStack furniture_itemstack : MicLoSFurniture.config.getFutnitureItemStack().values()) {

			if (i > Config.MAX_CATALOG_GUI_NUM) break;
			inv.setItem(i, furniture_itemstack);//???????????????hashmap?????????????????????
			i++;
		}

		p.openInventory(inv);
		return;
	}

	//??????????????????????????????GUI?????????????????????????????????
	@EventHandler
	public void addFurnitureInventory(InventoryClickEvent e) {
		Player p = (Player)e.getWhoClicked();

		if (!(e.isLeftClick() || e.isRightClick())) return;

		//????????????GUI????????????????????????
		if (!e.getView().getTitle().equals(Config.ADMIN_CATALOG_NAME)) return;

		e.setCancelled(true);//????????????GUI???????????????????????????????????????????????????

		//????????????GUI????????????????????????????????????????????????
		if (!(0 <= e.getRawSlot() && e.getRawSlot() <= Config.MAX_CATALOG_GUI_NUM)) return;

		//???????????????????????????????????????????????????????????????
		ItemStack clicked_item = e.getCurrentItem();

		if (!judgeFurniture(clicked_item)) return;

		//??????????????????????????????????????????????????????????????????
		p.getInventory().addItem(clicked_item);
		p.sendMessage(ChatColor.GRAY+"You get " + clicked_item.getItemMeta().getDisplayName());
		return;
	}

	//??????????????????????????????
	@EventHandler
	public void functionalFurniture(PlayerInteractAtEntityEvent e) {
		if (!(e.getRightClicked() instanceof ArmorStand)) return;
		ArmorStand stand = (ArmorStand)e.getRightClicked();

		ItemStack furniture_helmet = stand.getEquipment().getHelmet();//??????????????????????????????
		if (!(judgeFurniture(furniture_helmet))) return;

		Player p = e.getPlayer();
		if (p.isSneaking()) return;

		//???????????????????????????
		if (furniture_helmet.getItemMeta().getDisplayName().startsWith("vending_machine")) {
			ff.use_vending(p);
			return;
		}

		//???????????????????????????
		if (furniture_helmet.getItemMeta().getDisplayName().startsWith("doll_fox_snow")) {
			ff.use_doll_fox_snow(p);
			return;
		}

		//????????????????????????
		if (furniture_helmet.getItemMeta().getDisplayName().startsWith("chair_")) {
			ff.ride_furniture(p, stand);
			return;
		}

		//??????????????????????????????
		if (furniture_helmet.getItemMeta().getDisplayName().startsWith("slot")) {
			ff.use_slot(p, stand);
			return;
		}

		//???????????????????????????
		if (furniture_helmet.getItemMeta().getDisplayName().equals("beer_mug") || furniture_helmet.getItemMeta().getDisplayName().equals("coffee_cup_houston")) {
			Location loc_stand = stand.getLocation();
			if (!canBuild(p, loc_stand) || !iscanBuildGriefPrevention(p, loc_stand)) {
				p.sendMessage(ChatColor.RED + "???????????????????????????????????????????????????");
				return;
			}
			ff.use_drinks(p, furniture_helmet, stand);
			return;
		}

		//?????????????????????????????????????????????
		ItemStack user_item_hand = p.getInventory().getItemInMainHand();
		if (!judgeFurniture(user_item_hand)) return;

		if ((!user_item_hand.getType().equals(Material.AIR))) {
			if (furniture_helmet.getItemMeta().getDisplayName().startsWith("desk_") || furniture_helmet.getItemMeta().getDisplayName().startsWith("table")) {
			//????????????????????????
				placeArmorStand(stand.getLocation(), Config.ACCESSORY_POSITION, Config.ACCESSORY_HEIGHT, Config.ACCESSORY_POSITION, user_item_hand, p);
				return;
			}
		}
		return;
	}

	//????????????????????????????????????????????????
	@EventHandler(priority = EventPriority.LOWEST)
	public void controlVehicle(PlayerInteractEvent e) {
		if (e.getHand().equals(EquipmentSlot.OFF_HAND)) return;

		Player p = e.getPlayer();
		if (!p.isInsideVehicle()) return; //?????????????????????????????????????????????

		if(!(p.getVehicle() instanceof ArmorStand)) return; //?????????????????????????????????????????????

		ArmorStand vehicle_stand = (ArmorStand)p.getVehicle();

		if(!isVehicle(vehicle_stand.getEquipment().getHelmet())) return; //??????????????????????????????

		//??????or????????????
		if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			lastPress.put(p, System.currentTimeMillis());
			moveVehicle(p, vehicle_stand, 1);
		}
		else if (e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK)) moveVehicle(p, vehicle_stand, 0);

		return;
	}

	//????????????????????????????????????????????????????????????????????????????????????????????????
	@EventHandler
    public static void onLeaveDeletePressHashmap(PlayerQuitEvent e){
        lastPress.remove(e.getPlayer());
    }

	//????????????????????????????????????????????????
    public static boolean isPressed(Player p){
        Long last = lastPress.get(p);
        return last != null && System.currentTimeMillis() - last < 220;
    }

	//???????????????????????????
	private void moveVehicle(Player p, ArmorStand vehicle_stand, int move) {
		BukkitRunnable task = new BukkitRunnable() {
			Vector loc_p_eye;
			double y = 0;

			public void run() {

				//???????????????????????????
				if(Bukkit.getServer().getPlayer(p.getName()) == null || p.getVehicle() == null) {
					vehicle_stand.remove();
					this.cancel();
				}
				loc_p_eye = p.getLocation().getDirection().setY(-1).normalize();

				if (isnearWall(0.5, vehicle_stand)) loc_p_eye = p.getLocation().getDirection().setY(1).normalize();
				if (isPressed(p)) vehicle_stand.setVelocity(loc_p_eye.multiply(2.0));
				else if(move == 0) vehicle_stand.setVelocity(loc_p_eye.multiply(0));

				//????????????????????????????????????
				vehicle_stand.setHeadPose(new EulerAngle(0, y, 0));
				y = Math.toRadians(p.getLocation().getYaw());

			}
		};
		task.runTaskTimer(plugin, 0, 1);
	}

	//???????????????????????????????????????
	public boolean isnearWall(double dist, ArmorStand vehicle_stand) {
		Vector locale = vehicle_stand.getLocation().toVector();
		int y = locale.getBlockY();
		double x = locale.getX(), z = locale.getZ();
		World world = vehicle_stand.getWorld();
		Block b1 = world.getBlockAt(new Location(world, x + dist, y, z));
		Block b2 = world.getBlockAt(new Location(world, x - dist, y, z));
		Block b3 = world.getBlockAt(new Location(world, x, y, z + dist));
		Block b4 = world.getBlockAt(new Location(world, x, y, z - dist));

		Block bu1 = world.getBlockAt(new Location(world, x + dist, y+1, z));
		Block bu2 = world.getBlockAt(new Location(world, x - dist, y+1, z));
		Block bu3 = world.getBlockAt(new Location(world, x, y+1, z + dist));
		Block bu4 = world.getBlockAt(new Location(world, x, y+1, z - dist));
		if ((b1.getType() != Material.AIR) && (bu1.getType() == Material.AIR) || (b2.getType() != Material.AIR) && (bu2.getType() == Material.AIR) || (b3.getType() != Material.AIR) && (bu3.getType() == Material.AIR) || (b4.getType() != Material.AIR) && (bu4.getType() == Material.AIR))
			return true;
		return false;
	}

	//???????????????????????????????????????
    @EventHandler
    public void removeFurniture(EntityDamageByEntityEvent e){
    	if(!(e.getDamager() instanceof Player)) return;
        if(!(e.getEntity() instanceof ArmorStand)) return;

        ArmorStand stand = (ArmorStand)e.getEntity();
        Player p = (Player)e.getDamager();

        //?????????????????????????????????????????????????????????
		EntityEquipment stand_equipment = stand.getEquipment();
		ItemStack furniture_helmet = (ItemStack)stand_equipment.getHelmet();
		if(!(judgeFurniture(furniture_helmet))) return;

		//???????????????????????????
		Location stand_loc = stand.getLocation();
		Location stand_loc_up = stand_loc.clone();
		stand_loc_up.setY(stand_loc_up.getY()+1);
		if (!canBuild(p, stand_loc_up) || !iscanBuildGriefPrevention(p, stand_loc_up)) {
			p.sendMessage(ChatColor.RED + "You can't remove furniture");
			return;
		}

		//?????????????????????
		World world =  p.getWorld();
		world.dropItem(stand_loc, furniture_helmet);

        stand.remove();
        p.playSound(p.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1F, 1F);
    }

    //????????????????????????????????????????????????????????????????????????????????????
    public boolean canBuild(Player p, Location l) {
    	if (p.isOp()) return true;
    	try {
    		RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(l);

            return query.testState(loc, WorldGuardPlugin.inst().wrapPlayer(p), Flags.BUILD);
    	} catch(NoClassDefFoundError e) {
    		return true;
    	}

    }

    //griefprevention?????????????????????????????????????????????????????????????????????????????????????????????
    public boolean iscanBuildGriefPrevention(Player p, Location l) {
    	if (p.isOp()) return true;
    	Claim claim = GriefPrevention.instance.dataStore.getClaimAt(p.getLocation(), false, null);
    	if (claim == null || !claim.getOwnerName().equalsIgnoreCase(p.getName())) return false;
    	return true;
    }

	//??????????????????????????????????????????????????????
	private void placeArmorStand(Location loc, double x, double y, double z, ItemStack user_item, Player p) {
		loc.setYaw(getCardinalDirection8(p) + 180);
		loc.setX(loc.getX() + x);
		loc.setY(loc.getY() + y);
		loc.setZ(loc.getZ() + z);

		Location loc_up = loc.clone();//??????????????????????????????????????????????????????
		loc_up.setY(loc_up.getY() + 2);
		if (loc_up.getBlock().getType() != Material.AIR) return;//??????????????????????????????

		ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class);
		stand.setVisible(false);
		stand.setGravity(false);
		p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);

		//??????????????????amount???1???????????????clone?????????
		EntityEquipment stand_equipment = stand.getEquipment();
		ItemStack user_item_clone = user_item.clone();
		user_item_clone.setAmount(1);
		stand_equipment.setHelmet(user_item_clone);

		//??????????????????????????????????????????????????????
		int user_item_amount = user_item.getAmount();
		if(user_item_amount > 1) {
			user_item.setAmount(user_item_amount - 1);
		}else{
			user_item.setAmount(0);
		}
		return;
	}

	//???????????????????????????????????????????????????????????????
	private void placeWallFurniture(Location loc, double x, double y, double z, ItemStack user_item, Player p) {
		Location loc_c = loc.clone();//??????????????????????????????????????????????????????

		float player_yaw = getCardinalDirection4(p);
		loc.setYaw(player_yaw + 180);


		loc.setX(loc.getX() + x);
		loc.setY(loc.getY() + y);
		loc.setZ(loc.getZ() + z);

		loc = getDirectionDistance(loc, player_yaw);
		if (loc == null) return;

		ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class);
		stand.setVisible(false);
		stand.setGravity(false);
		stand.setSilent(true);
		p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);

		//??????????????????amount???1???????????????clone?????????
		EntityEquipment stand_equipment = stand.getEquipment();
		ItemStack user_item_clone = user_item.clone();
		user_item_clone.setAmount(1);
		stand_equipment.setHelmet(user_item_clone);

		//??????????????????????????????????????????????????????
		int user_item_amount = user_item.getAmount();
		if(user_item_amount > 1) {
			user_item.setAmount(user_item_amount - 1);
		}else{
			user_item.setAmount(0);
		}
		return;
	}

	//??????????????????????????????????????????????????????Location?????????
	private Location getDirectionDistance(Location loc, float yaw) {
		if (yaw == 0.0 || yaw == 360.0) {
			loc.setX(loc.getX() + 0.5);
			loc.setZ(loc.getZ() - 0.5);
		}
		else if (yaw == 180.0) {
			loc.setX(loc.getX() + 0.5);
			loc.setZ(loc.getZ() + 1.5);
		}
		else if (yaw == 90.0) { //W
			loc.setX(loc.getX() + 1.5);
			loc.setZ(loc.getZ() + 0.5);
		}
		else if (yaw == 270.0) {
			loc.setX(loc.getX() - 0.5);
			loc.setZ(loc.getZ() + 0.5);
		}
		else return null;
		return loc;
	}

	//????????????????????????????????????????????????????????????????????????
	private void placeVehicle(Location loc, double x, double y, double z, ItemStack user_item, Player p) {
		loc.setX(loc.getX() + x);
		loc.setY(loc.getY() + y);
		loc.setZ(loc.getZ() + z);

		ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class);
		stand.setSmall(true);
		stand.setVisible(false);
		stand.setGravity(true);

		p.playSound(p.getLocation(), Sound.ENTITY_MINECART_RIDING, 1F, 1F);
		ff.ride_furniture(p, stand);
		p.sendMessage(ChatColor.AQUA+"???????????????????????????????????????????????????");
		moveVehicle(p, stand, 0);

		//?????????????????????amount???1???????????????clone?????????
		EntityEquipment stand_equipment = stand.getEquipment();
		ItemStack user_item_clone = user_item.clone();
		user_item_clone.setAmount(1);
		stand_equipment.setHelmet(user_item_clone);
		return;
	}

    //?????????8????????????
	public static float getCardinalDirection8(Player player) {
		double rotation = (player.getLocation().getYaw() - 90.0F) % 360.0F;
		if (rotation < 0.0D) {
			rotation += 360.0D;
		}
		if ((0.0D <= rotation) && (rotation < 22.5D)) {
			return 90;//N
		}
		if ((22.5D <= rotation) && (rotation < 67.5D)) {
			return 135;//NE
		}
		if ((67.5D <= rotation) && (rotation < 112.5D)) {
			return 180;//E
		}
		if ((112.5D <= rotation) && (rotation < 157.5D)) {
			return 225;//SE
		}
		if ((157.5D <= rotation) && (rotation < 202.5D)) {
			return 270;//S
		}
		if ((202.5D <= rotation) && (rotation < 247.5D)) {
			return 315;//SW
		}
		if ((247.5D <= rotation) && (rotation < 292.5D)) {
			return 0;//W
		}
		if ((292.5D <= rotation) && (rotation < 337.5D)) {
			return 45;//NW
		}
		if ((337.5D <= rotation) && (rotation < 360.0D)) {
			return 90;//N
		}
		return 90;
	}

    //?????????4????????????
	public static float getCardinalDirection4(Player player) {
		double rotation = (player.getLocation().getYaw() - 90.0F) % 360.0F;
		if (rotation < 0.0D) {
			rotation += 360.0D;
		}
		if ((0.0D <= rotation) && (rotation < 45.0D)) {
			return 90;//N
		}
		if ((45.0D <= rotation) && (rotation < 135.0D)) {
			return 180;//E
		}
		if ((135.0D <= rotation) && (rotation < 225.0D)) {
			return 270;//S
		}
		if ((225.0D <= rotation) && (rotation < 315.0D)) {
			return 0;//W
		}
		if ((315.0D <= rotation) && (rotation < 360.0D)) {
			return 90;//N
		}
		return 90;
	}

	//ItemStack??????????????????????????????????????????
	private boolean judgeFurniture(ItemStack user_item) {

		if (user_item.getType().equals(Material.AIR)) return false;

		furniture_item_map = MicLoSFurniture.config.getFutnitureItemStack();
		String user_item_name = user_item.getItemMeta().getDisplayName();

		if (!furniture_item_map.containsKey(user_item_name)) return false; //?????????????????????????????????
		if (!user_item.getItemMeta().hasCustomModelData()) return false; //CustomModelData???????????????????????????
		if (!user_item.getType().equals(furniture_item_map.get(user_item_name).getType())) return false; //Material?????????????????????

		return true;
	}

	//ItemStack?????????????????????????????????????????????
	private boolean judgePhone(ItemStack user_item) {

		if (user_item.getType().equals(Material.AIR)) return false;

		String user_item_name = user_item.getItemMeta().getDisplayName();
		if (!user_item_name.equals(Config.ADMIN_CATALOG_NAME)) return false; //?????????????????????????????????
		if (!user_item.getItemMeta().hasCustomModelData()) return false; //CustomModelData???????????????????????????
		if (!user_item.getType().equals(Config.ADMIN_CATALOG_MATERIAL)) return false; //Material?????????????????????

		return true;
	}

	//ItemStack?????????????????????????????????????????????
	private boolean isVehicle(ItemStack item) {
		if (item.getType().equals(Config.VEHICLE_MATERIAL)) return true;
		return false;
	}

	//ItemStack???????????????????????????????????????????????????
	private boolean isWallFurniture(ItemStack item) {
		if (item.getType().equals(Config.ACCESSORY_MATERIAL)) return true;
		return false;
	}

	//??????????????????????????????
	private Block getClickedBlock(Player p) {
		BlockIterator it = new BlockIterator(p, 5);

		while (it.hasNext()) {
			Block block = it.next();
			if (block.getType() != Material.AIR) return block;
		}

		return null;
	}
}