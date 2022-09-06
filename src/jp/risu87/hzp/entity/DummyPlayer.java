package jp.risu87.hzp.entity;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import jp.risu87.hzp.util.DummyNetworkManager;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EnumProtocolDirection;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_12_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.PlayerInteractManager;
import net.minecraft.server.v1_12_R1.WorldServer;

public class DummyPlayer extends EntityPlayer {

	public DummyPlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile,
			PlayerInteractManager playerinteractmanager) {
		super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
		// TODO Auto-generated constructor stub
	}

	public void setSkin(Player p, String texture, String signature) {
		EntityPlayer player = ((CraftPlayer) p).getHandle();
		GameProfile playerProfile = player.getProfile();

		playerProfile.getProperties().clear();
		texture = "ewogICJ0aW1lc3RhbXAiIDogMTU4OTA1MzMyMjY3NiwKICAicHJvZmlsZUlkIiA6ICI3MmNiMDYyMWU1MTA0MDdjOWRlMDA1OTRmNjAxNTIyZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNb3M5OTAiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmM4OTBjN2U1OGYyM2U5N2ZmNGRkYWMwNDhiYmZhMDJkNjYxMTEwMTNkMmYxNzdjNWY4ZjYyYThiMWIxYWZkZCIKICAgIH0KICB9Cn0=";
		signature = "Skp8QvxYFa4YUhw+XHgicva/8j3gnCcN9u0EDLpkSlVCWuYqUeETbgA3LAit4ftjDjNpUA40UPTvlebBsnjcUvEkiu765BvZE61yms2IcNeK7vDoLoeNfx+UqTAquMI6uOzBBNZi6yBeMSghRX2hwVGsiKuzoFb67o1HfFcPLfCOVR3QRd2D84VfduhQmc+MVSFrUhZFGluzOvslUzR8/tbi2ZarkURlLOlgT0UoT1yEX/pHM7GogtnQiJL7xOqfEnU0Ex+OKZYkjawatbD/L5bjbL1pV2QeuxZLrnvRoQFTVAnONhvPfd9f8WF0kdR8DaDe4Knq+SPJ357HOun9ZRci3RobXcyQaRsw5JezSrgUbBccoUr7SiSgdM4VBhtzGGZ8TYUBz5pocHYCaOALG71bZZ4aVjQKfw5Rtalj+q2Wqbub20IQd/7/z9NUvPB0d7zHBLqr8a1UtZoSKLbaVJZJaYqt0ygxff68MKKQlE0L4fupBHEIXdNgza8tp472rsB+o45IZ/xmFltH1jhRsYvV973ki0l4S6U/O6gWu699sUyHn4a3DnVNN0GIyNAIP9KpHhvQzvxPxJq0Z2gXw2rzRGDxt+fe8gYZJ+UF4t/i39IP9RBgryocdu0L0lzeQA0b7vrr1khvAHyBVuZJ0t2S/RHTnlcAcAxoDENP1Gk=";
		playerProfile.getProperties().put("textures", new Property("textures", texture, signature));

		for (Player pl : Bukkit.getOnlinePlayers()) {
			pl.hidePlayer(p);
			pl.showPlayer(p);
		}
	}

	public static DummyPlayer createNPC(String name, World world, Location location) {


		MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
		WorldServer nmsWorld = ((CraftWorld) world).getHandle();
		GameProfile profile = new GameProfile(UUID.randomUUID(), name);
		PlayerInteractManager interactManager = new PlayerInteractManager(nmsWorld);
		DummyPlayer entityPlayer = new DummyPlayer(nmsServer, nmsWorld, profile, interactManager);
		entityPlayer.playerConnection = new PlayerConnection(nmsServer, new DummyNetworkManager(EnumProtocolDirection.CLIENTBOUND), entityPlayer);

		entityPlayer.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(),
				location.getPitch());

		nmsWorld.addEntity(entityPlayer);

		PacketPlayOutPlayerInfo playerInfoAdd = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);
		PacketPlayOutNamedEntitySpawn namedEntitySpawn = new PacketPlayOutNamedEntitySpawn(entityPlayer);
		PacketPlayOutEntityHeadRotation headRotation = new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) ((location.getYaw() * 256f) / 360f));
		PacketPlayOutPlayerInfo playerInfoRemove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer);

		for (Player player : Bukkit.getOnlinePlayers()) {
			PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
			connection.sendPacket(playerInfoAdd);
			connection.sendPacket(namedEntitySpawn);
			connection.sendPacket(headRotation);
			connection.sendPacket(playerInfoRemove);
		}
		return entityPlayer;
	}
}
