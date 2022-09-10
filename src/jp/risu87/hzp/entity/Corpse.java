package jp.risu87.hzp.entity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.plugin.npc.NPC;

import jp.risu87.hzp.HypixelZombiesProject;
import jp.risu87.hzp.gamerule.GameRunningRule;
import jp.risu87.hzp.gamerule.ScoreboardRule;
import jp.risu87.hzp.util.DummyNetworkManager;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import net.minecraft.server.v1_12_R1.EnumProtocolDirection;
import net.minecraft.server.v1_12_R1.Items;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.MobEffect;
import net.minecraft.server.v1_12_R1.MobEffectList;
import net.minecraft.server.v1_12_R1.MobEffects;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_12_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.PlayerInteractManager;
import net.minecraft.server.v1_12_R1.WorldServer;

public class Corpse extends NPC {
	
	public ItemStack[] savedInventory;
	public final Player owner;
	public static Corpse corpse;
	private int corpseTrackingID = -1;
	
	private static List<Corpse> corpses = new ArrayList<Corpse>();
	
	@SuppressWarnings("rawtypes")
	public Corpse(Player owner) {
		super("", owner.getLocation(), HypixelZombiesProject.getPlugin());
		
		this.owner = owner;
		String rawJson = "";
		try {
			String str_url = "https://sessionserver.mojang.com/session/minecraft/profile/" + owner.getUniqueId().toString() + "?unsigned=false";
			URL url = new URL(str_url);
			URLConnection connection = url.openConnection();
	        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String line;
	        while ((line = br.readLine()) != null) 
	            rawJson += line;
	        br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        if (!rawJson.equals("")) {
        	Gson gson = new Gson();
            Map jsonMap = gson.fromJson(rawJson, Map.class);
			ArrayList list = (ArrayList) jsonMap.get("properties");
			LinkedTreeMap map = (LinkedTreeMap)list.get(0);
            String texture = (String) map.get("value");
    		String signature = (String) map.get("signature");
            this.setSkin(texture, signature);
        }
		
		this.spawn(false, true);
		this.setInvisible(true);
		this.setSleep(true);
	}
	
	public Entity getEntity() {
		for (Entity e : GameRunningRule.getZombies().getWorld().getEntities()) {
			if (e.getEntityId() == this.getEntityId()) {
				return e;
			}
		}
		return null;
	}
	
	/**
	 * toggles to be seen as dead player
	 * @param flag
	 */
	public void setInvisible(boolean flag) {
		
		((CraftEntity)getEntity()).getHandle().setInvisible(flag);
		if (flag) {
			this.corpseTrackingID = HypixelZombiesProject.getSchedular().scheduleSyncRepeatingTask(
					getPlugin(), () -> {
						this.teleport(owner.getLocation().toVector().toLocation(owner.getWorld()), true);
					}, 0, 5);	
			this.corpseTrackingID = -1;
		} else {
			if (this.corpseTrackingID != -1)
				HypixelZombiesProject.getSchedular().cancelTask(this.corpseTrackingID);
		}
		
	}
	
	public static void reloadCorpsesFor(Player player) {
		corpses.forEach(c -> {
			c.addRecipient(player);
			c.reloadNpc();
			c.removeRecipient(player);
		});
	}
	
	public void removeCorpse() {
		this.destroy();
	}
	
	public void update() {
		this.reloadNpc();
	}
}
