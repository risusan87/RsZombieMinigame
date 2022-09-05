package jp.risu87.hzp.gamerule.gun;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_12_R1.NBTTagCompound;

public class GunRule {
	
	private static GunRule rule;
	private final Map<UUID, GunBase> guns = new HashMap<UUID, GunBase>();
	public static final String GUN_PISTOL = "pistol";
	
	private GunRule() {
		
	}
	
	public static void setupGunRule() {
		if (rule == null) rule = new GunRule();
	}
	
	public static GunRule getGunRule() {
		return rule;
	}
	
	public static void disableGunRule() {
		
	}
	
	public void registerGun(GunBase gun) {
		ItemStack bukkitGun = gun.updateGun();
		String uuid = CraftItemStack.asNMSCopy(bukkitGun).getTag().getString("gunID");
		this.guns.put(UUID.fromString(uuid), gun);
		gun.holder.getInventory().setItemInMainHand(bukkitGun);
	}
	
	public GunBase getGunObj(ItemStack itemGun) {
		NBTTagCompound nbt = CraftItemStack.asNMSCopy(itemGun).getTag();
		if (nbt == null || !nbt.hasKey("gunType")) 
			return null;
		GunBase gun = this.guns.get(UUID.fromString(nbt.getString("gunID")));
		switch (nbt.getString("gunType")) {
			case GUN_PISTOL: return (GunPistol)gun;
		}
		return null;
	}
	
}
