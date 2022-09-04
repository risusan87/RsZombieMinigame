package jp.risu87.hzp.gamerule.gun;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import jp.risu87.hzp.HypixelZombiesProject;
import net.minecraft.server.v1_12_R1.Item;
import net.minecraft.server.v1_12_R1.NBTTagCompound;

/*
 * ver 0.0.20 
 * - EntityFinder追加
 * 
 * ver 0.0.21 
 * - クリップ弾数満タンの時にリロード出来てしまうバグを改善
 * - パーティクルが謎の挙動になってしまうバグを改善
 * - JavaDocほぼ完成
 */
public abstract class GunBase {

	protected final Item rawItem;
	public final String gunName;

	/*=GUN STATUS=*/

	/** Time in second that this gun takes to reload */
	protected float[] reloadTime;

	/** Time in second of cool down to trigger a shoot */
	protected float[] interval;

	/** Maximum amount of ammo that this gun carries */
	protected int[] maxAmmo;

	/** Maximum amount of clip ammo that this gun carries */
	protected int[] maxClip;

	/** Time in second of cool down between each shoot when burst mode */
	protected float[] burstInterval;

	/** Number of burst for this gun. Set of 1 means no burst */
	protected int[] burstCount;

	/*============*/

	protected int currentAmmo;
	protected int currentClip;
	protected final int maxUlt;
	protected int ultLv;

	protected boolean isReloading = false;
	private long lastShoot = -1L;
	private int reloadTaskID = -1;

	/**
	 * Inherited classes MUST set their gun status 
	 * for each level of ultimate in their constructors.
	 * 
	 * @param gunName - The global identifier used to differentiate from other guns.
	 * @param maxUlt - Maximum Level of ultimate that this gun could be.
	 * @param rawItem - Vanilla item that represents as this particular gun.
	 */
	protected GunBase(String gunName, int maxUlt, Item rawItem) {

		this.gunName = gunName;
		this.maxUlt = maxUlt;
		this.rawItem = rawItem;

		this.interval = new float[maxUlt];
		this.reloadTime = new float[maxUlt];
		this.maxAmmo = new int[maxUlt];
		this.maxClip = new int[maxUlt];
		this.burstInterval = new float[maxUlt];
		this.burstCount = new int[maxUlt];

	}

	public final void shoot(Player holder) {

		if (lastShoot != -1L) {
			long timeLeap = System.currentTimeMillis() - lastShoot;
			long defInt = (long) Math.floor(this.interval[this.ultLv] * 1e3F);
			if (timeLeap < defInt) return;
		}

		if (this.isReloading) 
			return;

		lastShoot = System.currentTimeMillis();

		this.onShoot(holder);

	}

	public final void reload(Player holder) {
		
		if (this.isReloading) 
			return;
		
		if (this.currentClip == this.maxClip[this.ultLv])
			return;
		
		this.isReloading = true;
		
		final int slotID = holder.getInventory().getHeldItemSlot();
		final GunBase gun = this;
		final int goalCount = (int)Math.ceil(GunBase.this.reloadTime[GunBase.this.ultLv] * 20.f);
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
						gun.currentClip = gun.maxClip[gun.ultLv];
						ItemStack newGun = holder.getInventory().getItem(slotID);
						holder.getInventory().setItem(slotID, gun.updateGun(newGun));
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

	/**
	 * returns true if this shoot was last in clip
	 */
	protected abstract boolean eachShot(Player holder);

	public ItemStack getGun() {
		net.minecraft.server.v1_12_R1.ItemStack nmsGun = new net.minecraft.server.v1_12_R1.ItemStack(rawItem);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("gunType", this.gunName);
		nbt.setString("gunID", UUID.randomUUID().toString());
		nmsGun.setTag(nbt);
		ItemStack bGun = CraftItemStack.asBukkitCopy(nmsGun);
		bGun.setAmount(this.currentClip);
		System.out.println(this.currentClip);
		return bGun;
	}

	public void setUltLv(int lv) {
		this.setUltLv(lv);
	}

	protected ItemStack updateGun(ItemStack prevGun) {
		prevGun.setAmount(this.currentClip);
		return prevGun.clone();
	}

	protected void spawnParticle(Particle p, Player holder) {

		Location currentLoc = holder.getEyeLocation();
		Vector lookVec = holder.getLocation().getDirection();

		for (float f = 1f; f < 6f; f++) {
			Location l = currentLoc.clone().add(lookVec.clone().multiply(f));
			double x = l.getX();
			double y = l.getY();
			double z = l.getZ();
			holder.getWorld().spawnParticle(Particle.CRIT, x, y, z, 1, 0f, 0f, 0f, 0f, null);
		}
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

	protected abstract void onShoot(Player holder);

}
