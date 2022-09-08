package jp.risu87.hzp.command;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import jp.risu87.hzp.HypixelZombiesProject;
import jp.risu87.hzp.entity.DummyPlayer;
import jp.risu87.hzp.gamerule.CollisionRule;
import jp.risu87.hzp.gamerule.GameRunningRule;
import jp.risu87.hzp.gamerule.PermissionRule;
import jp.risu87.hzp.gamerule.VisibleBoard;
import jp.risu87.hzp.gamerule.VisibleBoard.BoardType;
import jp.risu87.hzp.gamerule.gun.GunBase;
import jp.risu87.hzp.gamerule.gun.GunPistol;
import jp.risu87.hzp.gamerule.gun.GunRule;
import jp.risu87.hzp.gamerule.gun.GunType;
import jp.risu87.hzp.util.ActionBarConstructor;
import jp.risu87.hzp.util.ChatJsonBuilder;

public class CommandHZP implements CommandExecutor, TabCompleter {
	
 	private static List<CommandBase> commands = new ArrayList<>();
	static {
		commands.add(new CommandJoin());
		commands.add(new CommandSaveLocation());
	}
	
	static Entity en;
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
			
			en = DummyPlayer.spawnAt(pSender.getLocation());
			CollisionRule.getCollisionRule().addPlayer((Player)en, CollisionRule.TEAM_IN_GAME_CORPSE);
			
	        return true;
	        
		}
		
		if (args[0].equals("tp")) {
			
			en.teleport(pSender);
			((CraftEntity)en).getHandle().setInvisible(false);
	        return true;
	        
		}
		
		if (args[0].equals("team")) {
			
			HypixelZombiesProject.getPlugin().getServer().getOnlinePlayers().forEach((uuid) -> {
	        	CollisionRule.getCollisionRule().addPlayer(uuid, CollisionRule.TEAM_IN_GAME_PLAYERS);
	        	//VisibleBoard.setupBoard().setVisibleBoard(BoardType.INGAME);
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
		if (args[0].equals("startTimer")) {
			VisibleBoard.getBoard().ingameBoardTimerStart();
			return true;
		}
		if (args[0].equals("stopTimer")) {
			VisibleBoard.getBoard().ingameBoardTimerStop();
		}
		if (args[0].equals("resetTimer")) {
			VisibleBoard.getBoard().ingameBoardTimerReset();
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
