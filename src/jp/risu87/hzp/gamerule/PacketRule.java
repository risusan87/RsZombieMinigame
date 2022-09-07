package jp.risu87.hzp.gamerule;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

import jp.risu87.hzp.HypixelZombiesProject;

public class PacketRule {
	
	private static PacketRule rule;
	
	private final PacketAdapter packetParticle;
	private final PacketAdapter packetSound;
	
	public PacketRule() {
		
		this.packetParticle = new PacketAdapter(
			    HypixelZombiesProject.getPlugin(),
			    ListenerPriority.NORMAL,
			    PacketType.Play.Server.WORLD_PARTICLES
			) {
			    @Override
			    public void onPacketSending(PacketEvent event) {
			    	boolean sweep = event.getPacket().getParticles().read(0).getName().equals("sweepAttack");
			        event.setCancelled(sweep);
			    }
		};
		
		this.packetSound = new PacketAdapter(
				HypixelZombiesProject.getPlugin(),
				ListenerPriority.NORMAL,
				PacketType.Play.Server.NAMED_SOUND_EFFECT
			) {
				@Override
				public void onPacketSending(PacketEvent event) {
					String soundName = event.getPacket().getSoundEffects().read(0).name();
					PermissionRule rule = PermissionRule.getPermissionRule();
					event.setCancelled(
							soundName.contains("ATTACK_SWEEP") || 
							soundName.contains("ATTACK_CRIT") ||
							soundName.contains("ATTACK_STRONG") ||
							soundName.contains("ATTACK_KNOCKBACK") ||
							(
								rule.hasPermission(event.getPlayer(), PermissionRule.HZP_FLAG_SHOULD_NOT_HEAR_XP_SOUND)
								&& 
								soundName.contains("ENTITY_PLAYER_LEVELUP")
							)
							);
				}
		};
		
		HypixelZombiesProject.getPlugin().getProtocolManager().addPacketListener(this.packetParticle);
		HypixelZombiesProject.getPlugin().getProtocolManager().addPacketListener(this.packetSound);
		
	}
	
	public static PacketRule getPacketRule() {
		return rule == null ? (rule = new PacketRule()) : rule;
	}
	
	public static void disablePacketRule() {
		HypixelZombiesProject plugin = HypixelZombiesProject.getPlugin();
		plugin.getProtocolManager().removePacketListeners(plugin);
	}
	
}
