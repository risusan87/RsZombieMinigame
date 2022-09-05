package jp.risu87.hzp.gamerule.gun;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import jp.risu87.hzp.HypixelZombiesProject;
import jp.risu87.hzp.gamerule.PermissionRule;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.PacketPlayOutNamedSoundEffect;

/*
 * ver 0.0.21
 *  - 残り弾数とリロードの表示バグを修正
 */
public class GunTrackLoop implements Listener {
	
	@EventHandler
	public void onPlayerShootGun1(PlayerInteractEvent event) {
		
		if (event.getHand() == EquipmentSlot.HAND) {
			
			Player player = event.getPlayer();
			GunBase heldGunType = getGunHeld(player, player.getInventory().getHeldItemSlot());
			if (heldGunType == null) 
				return;
			int gunSlotID = player.getInventory().getHeldItemSlot();
			
			if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				heldGunType.shoot(gunSlotID);
			} 
			else if ((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
				heldGunType.reload(player, gunSlotID);
			}
		}
		
	}
	
	@EventHandler
	public void onSwitchItem(PlayerItemHeldEvent event) {
		
		Player p = event.getPlayer();
		
		ItemStack newItem = p.getInventory().getItem(event.getNewSlot());
		ItemStack oldItem = p.getInventory().getItem(event.getPreviousSlot());
		
		GunBase newGun = GunRule.getGunRule().getGunObj(newItem);
		GunBase oldGun = GunRule.getGunRule().getGunObj(oldItem);
		
		if (oldGun != null) oldGun.onGunNotHeld();
		if (newGun != null) newGun.onGunHeld();
		
	}
	
	public void onWorldTick(World world) {}
	
	public static GunBase getGunHeld(Player player, int slotID) {
		
		ItemStack bukkitItem = player.getInventory().getItem(slotID);
		if (bukkitItem == null) 
			return null;
		NBTTagCompound tag = CraftItemStack.asNMSCopy(bukkitItem).getTag();
		if (tag == null || !tag.hasKey("gunType")) 
			return null;
		return GunRule.getGunRule().getGunObj(bukkitItem);
		
	}
	
}
