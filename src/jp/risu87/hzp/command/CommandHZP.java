package jp.risu87.hzp.command;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import jp.risu87.hzp.HypixelZombiesProject;
import jp.risu87.hzp.entity.DummyPlayer;
import jp.risu87.hzp.gamerule.CollisionRule;
import jp.risu87.hzp.gamerule.PermissionRule;
import jp.risu87.hzp.gamerule.gun.GunBase;
import jp.risu87.hzp.gamerule.gun.GunPistol;
import jp.risu87.hzp.gamerule.gun.GunRule;
import jp.risu87.hzp.gamerule.gun.GunType;
import jp.risu87.hzp.gamerule.zombies.VisibleBoard;
import jp.risu87.hzp.util.ActionBarConstructor;
import jp.risu87.hzp.util.ChatJsonBuilder;

public class CommandHZP implements CommandExecutor, TabCompleter {
	
 	private static List<CommandBase> commands = new ArrayList<>();
	static {
		commands.add(new CommandJoin());
		commands.add(new CommandSaveLocation());
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (!(sender instanceof Player)) {
			HypixelZombiesProject.getPlugin().logInfo("Command hzp is only compatible from player use.");
			HypixelZombiesProject.getPlugin().logInfo("Note that only OP and permitted players can use hzp command.");
			return false;
		}

		Player pSender = (Player) sender;
		
		if (!pSender.isOp() && !PermissionRule.getPermissionRule().hasPermission(pSender, PermissionRule.HZP_PERM_ALL)) {
			ChatJsonBuilder jb = new ChatJsonBuilder("You do not have permission to use this command.").withColor(ChatColor.RED);
			jb.sendJson(pSender);
			return false;
		}
		
		if (args.length == 0) {
			ChatJsonBuilder jb = new ChatJsonBuilder().withText("Use \"/hzp help\" to list commands.").withColor(ChatColor.RED);
			System.out.println(jb);
			jb.sendJson(pSender);
			return true;
		}
		
		for (CommandBase c : commands.toArray(new CommandBase[commands.size()])) {
			if (args[0].equals(c.getCommand())) {
				return c.onCommand(pSender, args);
			}
		}
		
		if (args[0].equals("pistol")) {
			GunRule.getGunRule().registerGun(pSender, pSender.getInventory().getHeldItemSlot(), GunType.PISTOL);
			return true;
		}
		
		if (args[0].equals("npc")) {
			
			DummyPlayer p = DummyPlayer.createNPC("Quint1220", pSender.getWorld(), pSender.getLocation());
			//p.setInvisible(true);
			Player dummy = p.getBukkitEntity();
			CollisionRule.getCollisionRule().addPlayer(dummy, CollisionRule.TEAM_IN_GAME_NON_PLAYERS);
			
			ProtocolManager manager = HypixelZombiesProject.getPlugin().getProtocolManager();
			final PacketContainer bedPacket = manager.createPacket(PacketType.Play.Server.BED, false);
	        final Location loc = dummy.getLocation();

	        bedPacket.getIntegers().
            write(0, dummy.getEntityId());
	        bedPacket.getBlockPositionModifier().
	        write(0, new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
	        
	        for (Player observer : HypixelZombiesProject.getPlugin().getServer().getOnlinePlayers()) {
	        	
	        	try {
					manager.sendServerPacket(observer, bedPacket);
					
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	
	        }
	        return true;
	        
		}
		
		if (args[0].equals("addPlayers")) {
			pSender.getWorld().getPlayers().forEach(p -> {
				CollisionRule rule = CollisionRule.getCollisionRule();
				if (!rule.containsPlayer(p, CollisionRule.TEAM_IN_GAME_PLAYERS))
					rule.addPlayer(p, CollisionRule.TEAM_IN_GAME_PLAYERS);
			});
			return true;
		}
		
		if (args[0].equals("ult")) {
			GunBase gun = GunRule.getGunRule().getGunObj(pSender.getInventory().getItemInMainHand());
			if (gun != null && gun.getUltLv() + 1 <= gun.maxUlt - 1)
				gun.setUltLv(gun.getUltLv() + 1);
			return true;
		}
		if (args[0].equals("deUlt")) {
			GunBase gun = GunRule.getGunRule().getGunObj(pSender.getInventory().getItemInMainHand());
			if (gun != null && gun.getUltLv() - 1 >= 0)
				gun.setUltLv(gun.getUltLv() - 1);
			return true;
		}
		if (args[0].equals("maxAmmo")) {
			GunBase gun = GunRule.getGunRule().getGunObj(pSender.getInventory().getItemInMainHand());
			if (gun != null)
				gun.refill();
			return true;
		}
		
		ChatJsonBuilder jb = new ChatJsonBuilder("Unknown command. Use \"/hzp help\" to list commands.");
		jb.withColor(ChatColor.RED);
		jb.sendJson(pSender);
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		
		if (!command.getName().equals("hzp")) return null;
		
		List<String> l = new ArrayList<String>();
		
		if (args.length == 1) {
			l.add("perm");
			l.add("give");
			l.add("help");
		}
		
		if (args.length == 2 && args[0].equals("perm")) {
			return null;
		}
		
		return l;
	}

}
