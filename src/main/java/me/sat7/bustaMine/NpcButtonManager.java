package me.sat7.bustaMine;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NpcButtonManager implements Listener {

    public static final String DEFAULT_BITCOIN_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmNjN2Y2NDQxYmQ3MWZjOTc0ZTk5NzdiY2IyMmVmYmM0YjYxMjc3YzQ5ZWZiMjQyM2FiOTE1NDg5NWJlIn19fQ==";

    private static NpcButtonManager listener;
    private static NamespacedKey actionKey;
    private static NamespacedKey idKey;

    public static void setup() {
        actionKey = new NamespacedKey(BustaMine.plugin, "npc_action");
        idKey = new NamespacedKey(BustaMine.plugin, "npc_id");

        if (listener == null) {
            listener = new NpcButtonManager();
            Bukkit.getPluginManager().registerEvents(listener, BustaMine.plugin);
        }

        restoreNpcs();
    }

    public static void create(Player player, String action) {
        NpcAction npcAction = NpcAction.from(action);
        if (npcAction == null) {
            player.sendMessage(BustaMine.prefix + ChatColor.YELLOW + "동작: menu(밈장), cashout(익절/탈출), bet-small(진입), bet-medium(물타기), bet-big(풀진입)");
            return;
        }

        Location location = player.getLocation();
        location.setPitch(0F);
        ArmorStand stand = spawnNpc(location, npcAction);
        saveNpc(stand.getUniqueId().toString(), stand.getLocation(), npcAction);

        player.sendMessage(BustaMine.prefix + ChatColor.GREEN + "NPC 버튼 생성: " + ChatColor.stripColor(npcAction.displayName));
    }

    public static void removeNearest(Player player) {
        ArmorStand nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
            if (!(entity instanceof ArmorStand) || !isNpc(entity)) {
                continue;
            }
            double distance = entity.getLocation().distanceSquared(player.getLocation());
            if (distance < nearestDistance) {
                nearest = (ArmorStand) entity;
                nearestDistance = distance;
            }
        }

        if (nearest == null) {
            player.sendMessage(BustaMine.prefix + ChatColor.YELLOW + "5블록 안에 밈장 NPC 버튼이 없습니다.");
            return;
        }

        removeNpc(nearest.getPersistentDataContainer().get(idKey, PersistentDataType.STRING));
        nearest.remove();
        player.sendMessage(BustaMine.prefix + ChatColor.GREEN + "가장 가까운 NPC 버튼을 삭제했습니다.");
    }

    public static void clear(Player player) {
        int removed = 0;
        for (World world : Bukkit.getWorlds()) {
            for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
                if (isNpc(stand)) {
                    stand.remove();
                    removed++;
                }
            }
        }
        BustaMine.ccConfig.get().set("Npc.Buttons", null);
        BustaMine.ccConfig.save();
        player.sendMessage(BustaMine.prefix + ChatColor.GREEN + "NPC 버튼 " + removed + "개를 삭제했습니다.");
    }

    public static void list(Player player) {
        ConfigurationSection section = BustaMine.ccConfig.get().getConfigurationSection("Npc.Buttons");
        if (section == null || section.getKeys(false).isEmpty()) {
            player.sendMessage(BustaMine.prefix + ChatColor.YELLOW + "설정된 NPC 버튼이 없습니다.");
            return;
        }

        player.sendMessage(BustaMine.prefix + ChatColor.GOLD + "NPC 버튼:");
        for (String id : section.getKeys(false)) {
            String path = "Npc.Buttons." + id;
            player.sendMessage(ChatColor.YELLOW + "- " + BustaMine.ccConfig.get().getString(path + ".Action") + ChatColor.GRAY
                    + " @ " + BustaMine.ccConfig.get().getString(path + ".World")
                    + " " + BustaMine.ccConfig.get().getDouble(path + ".X")
                    + ", " + BustaMine.ccConfig.get().getDouble(path + ".Y")
                    + ", " + BustaMine.ccConfig.get().getDouble(path + ".Z"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (!isNpc(entity)) {
            return;
        }
        event.setCancelled(true);
        runAction(event.getPlayer(), entity);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!isNpc(event.getEntity())) {
            return;
        }
        event.setCancelled(true);
        if (event.getDamager() instanceof Player) {
            runAction((Player) event.getDamager(), event.getEntity());
        }
    }

    private static void restoreNpcs() {
        removeUntrackedDuplicates();

        ConfigurationSection section = BustaMine.ccConfig.get().getConfigurationSection("Npc.Buttons");
        if (section == null) {
            return;
        }

        Set<String> liveIds = new HashSet<>();
        for (World world : Bukkit.getWorlds()) {
            for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
                if (isNpc(stand)) {
                    String id = stand.getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
                    if (id != null) {
                        liveIds.add(id);
                    }
                }
            }
        }

        for (String id : section.getKeys(false)) {
            if (liveIds.contains(id)) {
                continue;
            }

            String path = "Npc.Buttons." + id;
            World world = Bukkit.getWorld(BustaMine.ccConfig.get().getString(path + ".World", ""));
            NpcAction action = NpcAction.from(BustaMine.ccConfig.get().getString(path + ".Action", ""));
            if (world == null || action == null) {
                continue;
            }

            Location location = new Location(
                    world,
                    BustaMine.ccConfig.get().getDouble(path + ".X"),
                    BustaMine.ccConfig.get().getDouble(path + ".Y"),
                    BustaMine.ccConfig.get().getDouble(path + ".Z"),
                    (float) BustaMine.ccConfig.get().getDouble(path + ".Yaw"),
                    0F
            );
            ArmorStand stand = spawnNpc(location, action);
            stand.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, id);
        }
    }

    private static void removeUntrackedDuplicates() {
        for (World world : Bukkit.getWorlds()) {
            for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
                if (!isNpc(stand)) {
                    continue;
                }

                String id = stand.getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
                if (id == null || !BustaMine.ccConfig.get().contains("Npc.Buttons." + id)) {
                    stand.remove();
                }
            }
        }
    }

    private static ArmorStand spawnNpc(Location location, NpcAction action) {
        ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
        stand.setInvisible(true);
        stand.setInvulnerable(true);
        stand.setGravity(false);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setSmall(false);
        stand.setCanPickupItems(false);
        stand.setRemoveWhenFarAway(false);
        stand.setCustomNameVisible(true);
        stand.setCustomName(action.displayName);
        stand.setHelmet(createBitcoinHead());
        stand.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action.configName);
        stand.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, stand.getUniqueId().toString());
        return stand;
    }

    private static void runAction(Player player, Entity entity) {
        String actionName = entity.getPersistentDataContainer().get(actionKey, PersistentDataType.STRING);
        NpcAction action = NpcAction.from(actionName);
        if (action == null) {
            player.sendMessage(BustaMine.prefix + ChatColor.RED + "이 NPC 버튼의 동작 설정이 잘못되었습니다.");
            return;
        }

        switch (action) {
            case MENU:
                Game.OpenGameInven(player, Game.bustaType.money);
                break;
            case CASHOUT:
                Game.CashOut(player);
                break;
            case BET_SMALL:
                Game.Bet(player, Game.bustaType.money, BustaMine.ccConfig.get().getInt("Bet.Small"));
                break;
            case BET_MEDIUM:
                Game.Bet(player, Game.bustaType.money, BustaMine.ccConfig.get().getInt("Bet.Medium"));
                break;
            case BET_BIG:
                Game.Bet(player, Game.bustaType.money, BustaMine.ccConfig.get().getInt("Bet.Big"));
                break;
            case EXP_SMALL:
                Game.Bet(player, Game.bustaType.exp, BustaMine.ccConfig.get().getInt("Bet.ExpSmall"));
                break;
            case EXP_MEDIUM:
                Game.Bet(player, Game.bustaType.exp, BustaMine.ccConfig.get().getInt("Bet.ExpMedium"));
                break;
            case EXP_BIG:
                Game.Bet(player, Game.bustaType.exp, BustaMine.ccConfig.get().getInt("Bet.ExpBig"));
                break;
        }
    }

    private static ItemStack createBitcoinHead() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        try {
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.nameUUIDFromBytes(getHeadTexture().getBytes(StandardCharsets.UTF_8)), "BustaBTC");
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URL(extractSkinUrl(getHeadTexture())));
            profile.setTextures(textures);
            meta.setOwnerProfile(profile);
        } catch (Exception e) {
            BustaMine.console.sendMessage(BustaMine.consolePrefix + "Failed to apply NPC bitcoin head texture: " + e.getMessage());
        }
        meta.setDisplayName(ChatColor.GOLD + "Bitcoin");
        item.setItemMeta(meta);
        return item;
    }

    private static String getHeadTexture() {
        return BustaMine.ccConfig.get().getString("Npc.BitcoinHeadTexture", DEFAULT_BITCOIN_TEXTURE);
    }

    private static String extractSkinUrl(String base64) {
        String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
        String marker = "\"url\":\"";
        int start = decoded.indexOf(marker);
        if (start < 0) {
            return "http://textures.minecraft.net/texture/fcc7f6441bd71fc974e9977bcb22efbc4b61277c49efb2423ab9154895be";
        }
        start += marker.length();
        int end = decoded.indexOf("\"", start);
        return decoded.substring(start, end);
    }

    private static boolean isNpc(Entity entity) {
        if (!(entity instanceof ArmorStand) || actionKey == null) {
            return false;
        }
        return entity.getPersistentDataContainer().has(actionKey, PersistentDataType.STRING);
    }

    private static void saveNpc(String id, Location location, NpcAction action) {
        String path = "Npc.Buttons." + id;
        FileConfiguration config = BustaMine.ccConfig.get();
        config.set(path + ".World", location.getWorld().getName());
        config.set(path + ".X", location.getX());
        config.set(path + ".Y", location.getY());
        config.set(path + ".Z", location.getZ());
        config.set(path + ".Yaw", location.getYaw());
        config.set(path + ".Action", action.configName);
        BustaMine.ccConfig.save();
    }

    private static void removeNpc(String id) {
        if (id != null) {
            BustaMine.ccConfig.get().set("Npc.Buttons." + id, null);
            BustaMine.ccConfig.save();
        }
    }

    private enum NpcAction {
        MENU("menu", ChatColor.GOLD + "밈장 열기", "밈장", "메뉴"),
        CASHOUT("cashout", ChatColor.GREEN + "익절 / 탈출", "익절", "탈출"),
        BET_SMALL("bet-small", ChatColor.YELLOW + "진입", "진입", "소액진입"),
        BET_MEDIUM("bet-medium", ChatColor.YELLOW + "물타기", "물타기", "중액진입"),
        BET_BIG("bet-big", ChatColor.YELLOW + "풀진입", "풀진입", "몰빵"),
        EXP_SMALL("exp-small", ChatColor.AQUA + "경험치 진입", "경험치진입", "경험치소액"),
        EXP_MEDIUM("exp-medium", ChatColor.AQUA + "경험치 물타기", "경험치물타기", "경험치중액"),
        EXP_BIG("exp-big", ChatColor.AQUA + "경험치 풀진입", "경험치풀진입", "경험치몰빵");

        private final String configName;
        private final String displayName;
        private final String[] aliases;

        NpcAction(String configName, String displayName, String... aliases) {
            this.configName = configName;
            this.displayName = displayName;
            this.aliases = aliases;
        }

        private static NpcAction from(String text) {
            if (text == null) {
                return null;
            }
            String normalized = text.toLowerCase();
            for (NpcAction action : values()) {
                if (action.configName.equals(normalized)) {
                    return action;
                }
                for (String alias : action.aliases) {
                    if (alias.equalsIgnoreCase(text)) {
                        return action;
                    }
                }
            }
            return null;
        }
    }
}
