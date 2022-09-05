package jp.risu87.hzp.gamerule.gun;

import jp.risu87.hzp.HypixelZombiesProject;
import jp.risu87.hzp.gamerule.PermissionRule;
import jp.risu87.hzp.util.ActionBarConstructor;
import jp.risu87.hzp.util.ChatJsonBuilder;
import net.minecraft.server.v1_12_R1.Item;
import net.minecraft.server.v1_12_R1.NBTTagCompound;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Abstract class of all gun
 * @author risu87
 */
public abstract class GunBase {

	public final String gunName;
	public final String gunID; 
	protected final Item rawItem;
	protected final Map<String, Float[]> gunAttribute;
	protected final Player holder;

	/** Time in second that this gun takes to reload */
	public static final String RELOAD_TIME = "reloadTime";

	/** Time in second of cool down to trigger a shoot */
	public static final String SHOOT_INTERVAL = "interval";

	/** Maximum amount of ammo that this gun carries */
	public static final String MAX_AMMO = "maxAmmo";

	/** Maximum amount of clip ammo that this gun carries */
	public static final String MAX_CLIP_AMMO = "maxClipAmmo";

	/** Time in second of cool down between each shoot when burst mode */
	public static final String BURST_INTERVAL = "burstInterval";

	/** Number of burst for this gun. Set of 1 means no burst */
	public static final String BURST_COUNT = "burstCount";

	public static final String BASE_DAMAGE = "baseDamage";

	public static final String GOLD_PER_HIT = "goldPerHit";
	
	public static final String KNOCKBACK_POWER = "knockbackPower";

	protected int currentAmmo;
	protected int currentClip;
	public final int maxUlt;
	protected int ultLv;

	protected boolean isReloading = false;
	protected boolean onCooldown = false;
	
	private long lastShot = -1l;
	private int reloadTaskID = -1;
	private int intervalTaskID = -1;
	private float xpBarProgress = 1.0f;
	private ActionBarConstructor actionBarText = null;
	private int toleratedTime;

	/**
	 * Inherited classes MUST set their gun status 
	 * for each level of ultimate in their constructors.
	 * 
	 * Override {@link GunBase#setGunAttribute()} to set these values.
	 * 
	 * @param gunName - The global identifier used to differentiate from other guns.
	 * @param maxUlt - Maximum Level of ultimate that this gun could be.
	 * @param rawItem - Vanilla item that represents as this particular gun.
	 */
	protected GunBase(Player owner, String gunName, int maxUlt, Item rawItem) {
		
		this.toleratedTime = this.getToleratedTime();
		this.gunID = UUID.randomUUID().toString();
		this.holder = owner;
		
		this.gunName = gunName;
		this.maxUlt = maxUlt;
		this.rawItem = rawItem;

		this.gunAttribute = this.setGunAttribute();

	}

	/**
	 * returns true if this shoot was last in clip
	 */
	protected abstract boolean eachShot(int slotID);

	protected abstract void onShoot(int slotID);
	
	protected abstract Vector getKnockbackVec();
	
	/**
	 * This is the most important method to override in child classes.
	 * The returned map of this method will contain everything needed to characterize each guns.
	 * 
	 * The default map will return an attribute of Pistol, to prevent null pointers in default.
	 * 
	 * @return Map object contains an attribute of Pistol.
	 */
	protected Map<String, Float[]> setGunAttribute() {
		Map<String, Float[]> att = new HashMap<String, Float[]>(8);
		att.put(RELOAD_TIME, new Float[] {1.5f, 1.0f});
		att.put(SHOOT_INTERVAL, new Float[] {0.5f, 0.4f});
		att.put(MAX_AMMO, new Float[] {300.f, 450.f});
		att.put(MAX_CLIP_AMMO, new Float[] {10.f, 14.f});
		att.put(BURST_COUNT, new Float[] {1f, 2f});
		att.put(BURST_INTERVAL, new Float[] {0.f, 0.1f});
		att.put(BASE_DAMAGE, new Float[] {3.f, 3.f});
		att.put(GOLD_PER_HIT, new Float[] {10.f, 10.f});
		att.put(KNOCKBACK_POWER, new Float[] {0.2f, 0.3f});
		return att;
	};

	public final void shoot(int slotID) {
		
		if (this.isReloading) 
			return;
		
		HypixelZombiesProject plugin = HypixelZombiesProject.getPlugin();
		BukkitScheduler schedular = plugin.getServer().getScheduler();
		if (this.onCooldown) {
			long remaining = (long)(this.getAttribute(SHOOT_INTERVAL) * 1000f) - (System.currentTimeMillis() - this.lastShot);
			
			
			if (remaining < this.toleratedTime && remaining > -this.toleratedTime) {
				System.out.println("remaining " + remaining + " millisec tolerated");
				schedular.cancelTask(this.intervalTaskID);
			} else
				return;
		}
		
		this.onCooldown = true;
		this.lastShot = System.currentTimeMillis();
		
		final GunBase gun = this;
		final int goalCount = (int)Math.ceil(gun.getAttribute(SHOOT_INTERVAL) * 20.f);
		Runnable r = new Runnable() {

			int count = 0;

			@Override
			public void run() {
				try {
					if (count >= goalCount) {
						schedular.cancelTask(gun.intervalTaskID);
						gun.onCooldown = false;
						return;
					}
					count++;
					gun.xpBarProgress = (float)count / (float)goalCount;
					if (gun.holder.isDead()) 
						return;
					ItemStack itemGun = gun.holder.getInventory().getItemInMainHand();
					NBTTagCompound tag = CraftItemStack.asNMSCopy(itemGun).getTag();
					if (tag != null && tag.hasKey("gunType") && tag.getString("gunID").equals(gun.gunID)) {
						gun.holder.setExp(gun.xpBarProgress);
					}
				} catch (Exception e) {
					e.printStackTrace();
					schedular.cancelTask(gun.reloadTaskID);
				}

			}

		};
		this.intervalTaskID = schedular.scheduleSyncRepeatingTask(plugin, r, 0, 1);

		this.onShoot(slotID);

	}

	public final void reload(Player holder, int slotID) {
		
		if (this.isReloading) 
			return;

		if (this.currentClip == (int)this.getAttribute(MAX_CLIP_AMMO))
			return;

		this.isReloading = true;
		
		ChatJsonBuilder cjb = new ChatJsonBuilder();
		cjb.withText("RELOADING");
		cjb.withColor(ChatColor.RED);
		this.actionBarText = ActionBarConstructor.constractActionBarText(cjb);
		this.actionBarText.addViewers(holder);
		this.actionBarText.setTextVisible(true);
		
		final GunBase gun = this;
		final int goalCount = (int)Math.ceil(gun.getAttribute(RELOAD_TIME) * 20.f);
		final short maxDurability = holder.getInventory().getItemInMainHand().getType().getMaxDurability();
		HypixelZombiesProject plugin = HypixelZombiesProject.getPlugin();
		BukkitScheduler schedular = plugin.getServer().getScheduler();
		Runnable r = new Runnable() {

			int count = 0;

			@Override
			public void run() {
				try {
					if (count >= goalCount) {
						schedular.cancelTask(gun.reloadTaskID);
						gun.currentClip = (int)gun.getAttribute(MAX_CLIP_AMMO);
						
						gun.updateGun();
						
						gun.actionBarText.setTextVisible(false);
						gun.actionBarText = null;
						
						gun.isReloading = false;
						return;
					}
					count++;
					float progress = (float)count / (float)goalCount;
					holder.getInventory().getItem(slotID).setDurability((short)(maxDurability * (1f - progress)));
				} catch (Exception e) {
					// リロードバグ
					e.printStackTrace();
					schedular.cancelTask(gun.reloadTaskID);
				}

			}

		};
		holder.playSound(holder.getLocation(), Sound.ENTITY_HORSE_GALLOP, 0.5f, 0.5f);
		this.reloadTaskID = schedular.scheduleSyncRepeatingTask(plugin, r, 0, 1);
	}

	public float getAttribute(String value) {
		return this.gunAttribute.get(value)[this.ultLv];
	}
	
	/**
	 * +/- tolerated time in millisec.
	 * @return
	 */
	protected int getToleratedTime() {
		return 25;
	}

	private ItemStack getGun() {
		net.minecraft.server.v1_12_R1.ItemStack nmsGun = new net.minecraft.server.v1_12_R1.ItemStack(rawItem);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("gunType", this.gunName);
		nbt.setString("gunID", this.gunID);
		nmsGun.setTag(nbt);
		return CraftItemStack.asBukkitCopy(nmsGun);
	}

	public void setUltLv(int lv) {
		this.ultLv = lv;
		int slot = -1;
		for (ItemStack item : holder.getInventory().getContents()) {
			NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getTag();
			if (tag != null && tag.hasKey("gunID") && tag.getString("gunID").equals(this.gunID)) {
				slot = holder.getInventory().first(item);
			}
		}
		this.currentAmmo = (int)this.getAttribute(MAX_AMMO);
		this.currentClip = (int)this.getAttribute(MAX_CLIP_AMMO);
		PermissionRule.getPermissionRule().removePermission(holder, PermissionRule.HZP_FLAG_SHOULD_NOT_HEAR_XP_SOUND);
		holder.playSound(holder.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
		PermissionRule.getPermissionRule().addPermission(holder, PermissionRule.HZP_FLAG_SHOULD_NOT_HEAR_XP_SOUND);
		holder.getInventory().setItem(slot, this.updateGun());
	}
	
	public int getUltLv() {
		return this.ultLv;
	}
	
	public void onGunHeld() {
		
		if (this.actionBarText != null) 
			this.actionBarText.setTextVisible(true);
		
		int xp = holder.getLevel();
		int delta = this.currentAmmo - xp;
		holder.giveExpLevels(delta);
		holder.setExp(this.xpBarProgress);
		
	}
	
	public void onGunNotHeld() {
		
		if (this.actionBarText != null) 
			this.actionBarText.setTextVisible(false);
		
		int xp = holder.getExpToLevel();
		holder.giveExpLevels(-xp);
		holder.setExp(1f);
		
		
	}

	public final ItemStack updateGun() {
		
		ItemStack gun = this.getGun();
		
		gun.setAmount(this.currentClip);
		if (this.ultLv > 0) {
			gun.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
			ItemMeta meta = gun.getItemMeta();
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			gun.setItemMeta(meta);
		} else {
			gun.removeEnchantment(Enchantment.ARROW_INFINITE);
		}
		int xp = holder.getLevel();
		int delta = this.currentAmmo - xp;
		holder.giveExpLevels(delta);
		return gun;
	}

	protected void spawnParticle(Particle p, Player holder) {

		Location currentLoc = holder.getEyeLocation();
		Vector lookVec = holder.getLocation().getDirection();

		for (float f = 1f; f < 6f; f++) {
			Location l = currentLoc.clone().add(lookVec.clone().multiply(f));
			
			holder.getWorld().spawnParticle(p, l, 0);
		}
	}

	/* TODO
	 * ヘッショ計算
	 */
	/**
	 * Called when a player's shot hit an enemy.
	 * 
	 * @param playerLoc - Current eye location of the player.
	 * @param enemyLoc - Current eye location of the enemy.
	 * @return isHeadShot
	 */
	protected boolean checkHeadShot(Location playerLoc, Location enemyLoc) {
		boolean headShot = false;
		
		double distance = playerLoc.distance(enemyLoc);
		
		Location l = playerLoc.clone();
		l.add(playerLoc.getDirection().multiply(distance));
		
		headShot = l.toVector().isInSphere(enemyLoc.toVector(), 0.35D);
		
		float pitch = headShot ? 1.5f : 2f;
		holder.playSound(holder.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, pitch);
		
		return headShot;
	}

	protected static class EntityFinder {

		private final float range;
		private final Location origin;
		private final Vector direction;

		private Function<Entity, Boolean> entityFiler = null;
		private Function<Block, Boolean> blockFiler = null;

		private EntityFinder(Location origin, Vector direction, float range) {
			this.origin = origin;
			this.direction = direction;
			this.range = range;
		}


		public static EntityFinder inATrajectoryOf(Location origin, Vector direction, float range) {
			return new EntityFinder(origin, direction, range);
		}


		public EntityFinder setEntityFilter(Function<Entity, Boolean> filter) {
			this.entityFiler = filter;
			return this;
		}

		public EntityFinder setBlockFilter(Function<Block, Boolean> filter) {
			this.blockFiler = filter;
			return this;
		}

		public Entity find() {

			World world = origin.getWorld();
			Location currentLoc = this.origin;
			Vector lookVec = this.direction;
			boolean hit = false;

			for (float f = 0f; !hit && f < range; f += 0.1f) {
				Location evalLoc = currentLoc.clone().add(lookVec.clone().multiply(f));
				Collection<Entity> victim = world.getNearbyEntities(evalLoc, .25f, .25f, .25f);
				for (Entity e : victim.toArray(new Entity[victim.size()])) {
					if (this.entityFiler.apply(e)) {
						hit = true;
						return e;
					}
				}
				Block currentBlock = world.getBlockAt(evalLoc);
				if (this.blockFiler.apply(currentBlock)) {
					hit = true;
					return null;
				}
			}
			return null;
		}

	}

	protected static class BurstSchedular {

		private final int taskID;

		public BurstSchedular(GunBase controller, Player holder, int gunSlot) {

			HypixelZombiesProject plugin = HypixelZombiesProject.getPlugin();
			BukkitScheduler schedular = plugin.getServer().getScheduler();
			final BurstSchedular bs = this;
			final int burstCount = (int)controller.getAttribute(BURST_COUNT);
			final float burstInterval = controller.getAttribute(BURST_INTERVAL);
			Runnable r = new Runnable() {

				int count = 0;
				int burst = 0;

				@Override
				public void run() {

					if (controller.isReloading) {
						schedular.cancelTask(bs.taskID);
						return;
					}
					int i = (int)(burstInterval * 20f);
					if (count % (i == 0 ? 1 : i) == 0) {

						if (controller.eachShot(gunSlot)) {
							controller.reload(holder, gunSlot);
							schedular.cancelTask(bs.taskID);
							return;
						}
						burst++;
						holder.getInventory().setItem(gunSlot, controller.updateGun());
					}

					if (burst >= burstCount) {
						schedular.cancelTask(bs.taskID);
						return;
					}
					count++;
				}
			};
			this.taskID = schedular.scheduleSyncRepeatingTask(plugin, r, 0, 1);

		}
	}
}
