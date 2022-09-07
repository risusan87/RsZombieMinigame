package jp.risu87.hzp.gamerule;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.util.Vector;

import jp.risu87.hzp.entity.Zombie;
import net.minecraft.server.v1_12_R1.EntityZombie;
import net.minecraft.server.v1_12_R1.WorldServer;


public class EventListener implements Listener {

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		
		if (
			event.getAction() == Action.RIGHT_CLICK_AIR && 
			event.getHand() == EquipmentSlot.HAND &&
			event.getItem().getType() == Material.STICK
		) {
			event.getPlayer().sendMessage("Interact");
			WorldServer world = ((CraftWorld)event.getPlayer().getWorld()).getHandle();
			EntityZombie zombie = new Zombie(world);
			Location pl = event.getPlayer().getLocation();
			zombie.setLocation(pl.getX(), pl.getY(), pl.getZ(), pl.getYaw(), pl.getPitch());
			zombie.setSize(2f, 2f);
			LivingEntity bZombie = (LivingEntity)zombie.getBukkitEntity();
			bZombie.setMaximumNoDamageTicks(0);
			world.addEntity(((CraftEntity)bZombie).getHandle());
			
			AttributeInstance attribute = p.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
		    if (attribute == null)
		      return; 
		    attribute.setBaseValue(24f);
		    p.saveData();
		}
	}
	
	@EventHandler
	public void d(PlayerJoinEvent event) {
		event.setJoinMessage(null);
		
	}
	
	public void onPlayerMove(PlayerMoveEvent event) {
		
	}
	
}
