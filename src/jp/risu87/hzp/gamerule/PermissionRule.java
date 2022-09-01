package jp.risu87.hzp.gamerule;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

public class PermissionRule {
	
	private final JavaPlugin plugin;
	private static PermissionRule rule = null;
	
	public static final String HZP_PERM_ALL = "hzp.all";
	
	private PermissionRule(JavaPlugin plugin) {
		this.plugin = plugin;
	}
	
	public static void setupPermissionRule(JavaPlugin plugin) {
		if (rule == null) rule = new PermissionRule(plugin);
	}
	
	public static void disablePermissionRule() {
		
	}
	
	public static PermissionRule getPermissionRule() {
		return rule;
	}
	
	public boolean hasPermission(Player player, String permission) {
		
		PermissionAttachment pa = player.addAttachment(this.plugin);
		if (!pa.getPermissions().containsKey(permission)) return false;
		return pa.getPermissions().get(permission);
		
	}
	
	public void addPermissionAll(Player player) {
		PermissionAttachment pa = player.addAttachment(this.plugin);
		pa.setPermission(HZP_PERM_ALL, true);
	}
	
}
