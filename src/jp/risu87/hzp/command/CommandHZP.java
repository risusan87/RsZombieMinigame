package jp.risu87.hzp.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import jp.risu87.hzp.gamerule.PermissionRule;

public class CommandHZP implements CommandExecutor, TabCompleter {
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (!(sender instanceof Player)) {
			System.out.println("[Zombies] Command hzp is only compatible from player use.");
			System.out.println("[Zombies] Note that only OP and permitted players can use hzp command.");
			return false;
		}
		Player p = (Player) sender;
		if (args.length == 3 && args[0].equals("perm")) {
			
		}
		if (!PermissionRule.getPermissionRule().hasPermission((Player)sender, PermissionRule.HZP_PERM_ALL)) {
			((Player)sender).sendMessage("[Zombies] You do not have permission to use this command.");
			return false;
		}
		p.sendMessage("[Zombies] Type \"/hzp help\" to see list of commands.");
		
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		
		if (!command.getName().equals("hzp")) return null;
		
		List<String> l = new ArrayList<String>();
		if (args.length == 1) {
			l.add("perm");
			l.add("help");
		}
		
		if (args.length == 2 && args[0].equals("perm")) {
			return null;
		}
		
		return l;
	}

}
