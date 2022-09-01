package jp.risu87.hzp;

import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import jp.risu87.hzp.command.CommandHZP;
import jp.risu87.hzp.gamerule.CollisionRule;
import jp.risu87.hzp.gamerule.PermissionRule;
import jp.risu87.hzp.zombie.Zombie;
import net.minecraft.server.v1_12_R1.EntityTypes;
import net.minecraft.server.v1_12_R1.MinecraftKey;

/**
 * Spigot APIによるゾンビ再現プロジェクト。
 * 
 * 
 * ver note: Major.Minor.Build
 * @author risu87
 */
public class HypixelZombiesProject extends JavaPlugin {
	
	/**
	 * Called once when the plugin is enabled.
	 */
	@Override
	public void onEnable() {
		EventListener listener = new EventListener();
		this.getServer().getPluginManager().registerEvents(listener, this);
		@SuppressWarnings("unused")
		int tickEventSchedule = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, 
			() -> {
				List<? extends Player> playerList = (List<? extends Player>)this.getServer().getOnlinePlayers();
				if (playerList.size() == 0) return;
				World world = playerList.get(0).getWorld();
				listener.onWorldTick(world);
			}, 0, 1);
		
		CollisionRule.setupCollisionRule(false);
		PermissionRule.setupPermissionRule(this);
		
		MinecraftKey key = new MinecraftKey("zombiebasic");
		EntityTypes.b.a(54, key, Zombie.class);
		EntityTypes.d.add(key);
		try {
			Field gField = EntityTypes.class.getDeclaredField("g");
			gField.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<String> g = (List<String>)gField.get(null);
			while(g.size() <= 54) g.add(null);
			g.set(54, "zombiebasic");
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		CommandHZP hzp = new CommandHZP();
		this.getCommand("hzp").setExecutor(hzp);
		this.getCommand("hzp").setTabCompleter(hzp);
		
	}
	
	/**
	 * Called once when the plugin is disabled.
	 */
	@Override
	public void onDisable() {
		CollisionRule.disableCollisionRule();
	}
}
