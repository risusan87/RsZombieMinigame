package jp.risu87.hzp;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

import jp.risu87.hzp.gamerule.CollisionRule;
import jp.risu87.hzp.zombie.Zombie;
import net.minecraft.server.v1_12_R1.EntityDamageSource;
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
			world.addEntity(zombie);
			
			p.getWorld().getPlayers().forEach(player -> {
				if (!CollisionRule.getCollisionRule().containsPlayer(player)) {
					CollisionRule.getCollisionRule().addPlayer(player);
					p.sendMessage("Added to no collision");
				}
			});
		}
		
		
	}
	
	@EventHandler
	public void onZombieMeleeAttacked(EntityDamageEvent event) {
		
		if (event.getEntityType() != EntityType.ZOMBIE) return;
		event.getEntity().setVelocity(new Vector(0, 0.3, 0));
		
	}

	public void onWorldTick(World world) {
		
	}
	
}
