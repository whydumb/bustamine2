package me.sat7.bustaMine;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GraphBoard implements Listener {

    private static final int MAP_SIZE = 128;
    private static final DecimalFormat MULTIPLIER_FORMAT = new DecimalFormat("0.00");
    private static final DecimalFormat PROFIT_FORMAT = new DecimalFormat("0.00");

    private static final Color BACKGROUND = new Color(39, 36, 63);
    private static final Color PANEL = new Color(45, 41, 74);
    private static final Color AXIS = new Color(235, 235, 245);
    private static final Color MUTED = new Color(179, 171, 222);
    private static final Color LINE = new Color(242, 136, 10);
    private static final Color GREEN = new Color(82, 207, 146);
    private static final Color RED = new Color(255, 105, 105);
    private static final Color DARK_RED = new Color(65, 34, 55);
    private static final Color DARK_GREEN = new Color(28, 57, 66);

    private enum Phase {
        BETTING,
        RUNNING,
        BUSTED
    }

    private static final Map<UUID, ItemFrame> firstSelections = new HashMap<>();
    private static final List<MapView> mapViews = new ArrayList<>();
    private static final List<GraphMapRenderer> renderers = new ArrayList<>();
    private static final List<Integer> multipliers = new ArrayList<>();
    private static final List<Integer> history = new ArrayList<>();
    private static final List<Location> boardLocations = new ArrayList<>();
    private static final Set<UUID> boardFrameIds = new HashSet<>();

    private static GraphBoard listener;
    private static Phase phase = Phase.BETTING;
    private static int width;
    private static int height;
    private static int countdown;
    private static int currentMultiplier = 100;
    private static int bustedMultiplier = 100;
    private static byte[][] tileColors = new byte[0][];
    private static long imageVersion;

    public static void setup() {
        if (listener == null) {
            listener = new GraphBoard();
            Bukkit.getPluginManager().registerEvents(listener, BustaMine.plugin);
        }
        loadBoard();
        rebuildImage();
    }

    public static boolean isConfigured() {
        return width > 0 && height > 0 && mapViews.size() == width * height;
    }

    public static void beginSelection(Player player) {
        firstSelections.put(player.getUniqueId(), null);
        player.sendMessage(BustaMine.prefix + ChatColor.YELLOW + "차트 선택 시작. 왼쪽 위 액자, 오른쪽 아래 액자를 차례대로 우클릭하세요.");
    }

    public static void clear(Player player) {
        FileConfiguration config = BustaMine.ccConfig.get();
        config.set("Graph.Enabled", false);
        config.set("Graph.Width", null);
        config.set("Graph.Height", null);
        config.set("Graph.MapIds", null);
        config.set("Graph.FrameUUIDs", null);
        BustaMine.ccConfig.save();

        mapViews.clear();
        renderers.clear();
        boardLocations.clear();
        boardFrameIds.clear();
        width = 0;
        height = 0;
        tileColors = new byte[0][];
        imageVersion++;

        player.sendMessage(BustaMine.prefix + ChatColor.YELLOW + "밈장 차트를 지웠습니다.");
    }

    public static void info(Player player) {
        if (!isConfigured()) {
            player.sendMessage(BustaMine.prefix + ChatColor.YELLOW + "밈장 차트가 아직 설정되지 않았습니다.");
            return;
        }
        player.sendMessage(BustaMine.prefix + ChatColor.GREEN + "밈장 차트: " + width + "x" + height + " 액자 (" + mapViews.size() + " 지도)");
    }

    public static void startBetting(int seconds) {
        phase = Phase.BETTING;
        countdown = Math.max(0, seconds);
        currentMultiplier = 100;
        bustedMultiplier = 100;
        multipliers.clear();
        rebuildImage();
        sendMapsToNearbyPlayers();
    }

    public static void updateBettingCountdown(int seconds) {
        if (phase != Phase.BETTING || countdown == seconds) {
            return;
        }
        countdown = Math.max(0, seconds);
        rebuildImage();
        sendMapsToNearbyPlayers();
    }

    public static void startRun() {
        phase = Phase.RUNNING;
        currentMultiplier = 100;
        bustedMultiplier = 100;
        multipliers.clear();
        multipliers.add(currentMultiplier);
        rebuildImage();
        sendMapsToNearbyPlayers();
    }

    public static void updateMultiplier(int multiplier) {
        if (phase != Phase.RUNNING) {
            return;
        }
        currentMultiplier = Math.max(100, multiplier);
        multipliers.add(currentMultiplier);
        int maxPoints = Math.max(80, getCanvasWidth());
        while (multipliers.size() > maxPoints) {
            multipliers.remove(0);
        }
        rebuildImage();
        sendMapsToNearbyPlayers();
    }

    public static void bust(int multiplier) {
        phase = Phase.BUSTED;
        bustedMultiplier = Math.max(100, multiplier);
        currentMultiplier = bustedMultiplier;
        if (multipliers.isEmpty() || multipliers.get(multipliers.size() - 1) != bustedMultiplier) {
            multipliers.add(bustedMultiplier);
        }
        history.add(bustedMultiplier);
        while (history.size() > 9) {
            history.remove(0);
        }
        rebuildImage();
        sendMapsToNearbyPlayers();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Entity entity = event.getRightClicked();
        if (isBoardFrame(entity) && !firstSelections.containsKey(uuid)) {
            if (player.hasPermission("bm.admin") && player.isSneaking() && entity instanceof ItemFrame) {
                ((ItemFrame) entity).setRotation(org.bukkit.Rotation.NONE);
            }
            event.setCancelled(true);
            return;
        }

        if (!firstSelections.containsKey(uuid)) {
            return;
        }

        if (!(entity instanceof ItemFrame)) {
            return;
        }

        event.setCancelled(true);
        ItemFrame frame = (ItemFrame) entity;
        ItemFrame first = firstSelections.get(uuid);
        if (first == null) {
            firstSelections.put(uuid, frame);
            player.sendMessage(BustaMine.prefix + ChatColor.GREEN + "첫 모서리 선택 완료. 반대쪽 모서리를 우클릭하세요.");
            return;
        }

        firstSelections.remove(uuid);
        SelectedFrames selected = selectFrames(first, frame);
        if (selected == null) {
            player.sendMessage(BustaMine.prefix + ChatColor.RED + "선택이 잘못됐습니다. 같은 벽의 액자를 왼쪽 위부터 오른쪽 아래 순서로 선택하세요.");
            return;
        }

        configureBoard(player, selected);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFrameDamage(EntityDamageByEntityEvent event) {
        if (isBoardFrame(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFrameBreak(HangingBreakEvent event) {
        if (isBoardFrame(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    private static void configureBoard(Player player, SelectedFrames selected) {
        World world = selected.frames.get(0).getWorld();
        List<Integer> mapIds = new ArrayList<>();
        List<String> frameUUIDs = new ArrayList<>();

        mapViews.clear();
        renderers.clear();
        boardLocations.clear();
        boardFrameIds.clear();
        width = selected.width;
        height = selected.height;

        for (int i = 0; i < selected.frames.size(); i++) {
            MapView mapView = Bukkit.createMap(world);
            clearRenderers(mapView);
            GraphMapRenderer renderer = new GraphMapRenderer(i);
            mapView.addRenderer(renderer);
            mapViews.add(mapView);
            renderers.add(renderer);
            mapIds.add(mapView.getId());

            ItemFrame frame = selected.frames.get(i);
            boardLocations.add(frame.getLocation());
            boardFrameIds.add(frame.getUniqueId());
            frameUUIDs.add(frame.getUniqueId().toString());
            frame.setItem(createMapItem(mapView));
        }

        FileConfiguration config = BustaMine.ccConfig.get();
        config.set("Graph.Enabled", true);
        config.set("Graph.Width", width);
        config.set("Graph.Height", height);
        config.set("Graph.World", world.getName());
        config.set("Graph.MapIds", mapIds);
        config.set("Graph.FrameUUIDs", frameUUIDs);
        BustaMine.ccConfig.save();

        rebuildImage();
        sendMapsToNearbyPlayers();
        player.sendMessage(BustaMine.prefix + ChatColor.GREEN + "밈장 차트 생성: " + width + "x" + height + " 액자.");
    }

    private static void loadBoard() {
        FileConfiguration config = BustaMine.ccConfig.get();
        if (!config.getBoolean("Graph.Enabled", false)) {
            width = 0;
            height = 0;
            mapViews.clear();
            renderers.clear();
            boardLocations.clear();
            boardFrameIds.clear();
            return;
        }

        int loadedWidth = config.getInt("Graph.Width", 0);
        int loadedHeight = config.getInt("Graph.Height", 0);
        List<Integer> mapIds = config.getIntegerList("Graph.MapIds");
        if (loadedWidth <= 0 || loadedHeight <= 0 || mapIds.size() != loadedWidth * loadedHeight) {
            Bukkit.getConsoleSender().sendMessage(BustaMine.consolePrefix + "Graph board config is invalid.");
            return;
        }

        mapViews.clear();
        renderers.clear();
        boardLocations.clear();
        boardFrameIds.clear();
        width = loadedWidth;
        height = loadedHeight;
        boolean mapIdsChanged = false;

        for (int i = 0; i < mapIds.size(); i++) {
            MapView mapView = Bukkit.getMap(mapIds.get(i));
            if (mapView == null) {
                World world = Bukkit.getWorld(config.getString("Graph.World", ""));
                if (world == null) {
                    world = Bukkit.getWorlds().get(0);
                }
                mapView = Bukkit.createMap(world);
                mapIds.set(i, mapView.getId());
                mapIdsChanged = true;
            }
            clearRenderers(mapView);
            GraphMapRenderer renderer = new GraphMapRenderer(i);
            mapView.addRenderer(renderer);
            mapViews.add(mapView);
            renderers.add(renderer);
        }
        if (mapIdsChanged) {
            config.set("Graph.MapIds", mapIds);
            BustaMine.ccConfig.save();
        }

        restoreFrames(config.getStringList("Graph.FrameUUIDs"));
    }

    private static void restoreFrames(List<String> frameUUIDs) {
        if (frameUUIDs.size() != mapViews.size()) {
            return;
        }
        boardLocations.clear();
        boardFrameIds.clear();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClass(ItemFrame.class)) {
                String uuid = entity.getUniqueId().toString();
                int index = frameUUIDs.indexOf(uuid);
                if (index >= 0 && index < mapViews.size()) {
                    boardLocations.add(entity.getLocation());
                    boardFrameIds.add(entity.getUniqueId());
                    ((ItemFrame) entity).setItem(createMapItem(mapViews.get(index)));
                }
            }
        }
    }

    private static SelectedFrames selectFrames(ItemFrame first, ItemFrame second) {
        if (!first.getWorld().equals(second.getWorld()) || !first.getFacing().equals(second.getFacing())) {
            return null;
        }

        BlockFace facing = first.getFacing();
        boolean useX;
        int plane;
        if (facing == BlockFace.NORTH || facing == BlockFace.SOUTH) {
            useX = true;
            plane = first.getLocation().getBlockZ();
            if (plane != second.getLocation().getBlockZ()) {
                return null;
            }
        } else if (facing == BlockFace.EAST || facing == BlockFace.WEST) {
            useX = false;
            plane = first.getLocation().getBlockX();
            if (plane != second.getLocation().getBlockX()) {
                return null;
            }
        } else {
            return null;
        }

        int firstH = useX ? first.getLocation().getBlockX() : first.getLocation().getBlockZ();
        int secondH = useX ? second.getLocation().getBlockX() : second.getLocation().getBlockZ();
        int firstY = first.getLocation().getBlockY();
        int secondY = second.getLocation().getBlockY();
        int horizontalStep = Integer.compare(secondH, firstH);
        int verticalStep = Integer.compare(secondY, firstY);
        if (horizontalStep == 0) {
            horizontalStep = 1;
        }
        if (verticalStep == 0) {
            verticalStep = -1;
        }

        int selectedWidth = Math.abs(secondH - firstH) + 1;
        int selectedHeight = Math.abs(secondY - firstY) + 1;
        Map<String, ItemFrame> byPosition = new HashMap<>();

        for (ItemFrame frame : first.getWorld().getEntitiesByClass(ItemFrame.class)) {
            if (!frame.getFacing().equals(facing)) {
                continue;
            }
            Location location = frame.getLocation();
            int framePlane = useX ? location.getBlockZ() : location.getBlockX();
            if (framePlane != plane) {
                continue;
            }
            int h = useX ? location.getBlockX() : location.getBlockZ();
            int y = location.getBlockY();
            if (between(h, firstH, secondH) && between(y, firstY, secondY)) {
                byPosition.put(h + ":" + y, frame);
            }
        }

        List<ItemFrame> frames = new ArrayList<>();
        for (int row = 0; row < selectedHeight; row++) {
            int y = firstY + (row * verticalStep);
            for (int col = 0; col < selectedWidth; col++) {
                int h = firstH + (col * horizontalStep);
                ItemFrame frame = byPosition.get(h + ":" + y);
                if (frame == null) {
                    return null;
                }
                frames.add(frame);
            }
        }

        return new SelectedFrames(frames, selectedWidth, selectedHeight);
    }

    private static boolean between(int value, int a, int b) {
        return value >= Math.min(a, b) && value <= Math.max(a, b);
    }

    private static boolean isBoardFrame(Entity entity) {
        if (!BustaMine.ccConfig.get().getBoolean("Graph.ProtectFrames", true)) {
            return false;
        }
        if (!(entity instanceof ItemFrame)) {
            return false;
        }
        if (boardFrameIds.contains(entity.getUniqueId())) {
            return true;
        }
        return BustaMine.ccConfig.get().getStringList("Graph.FrameUUIDs").contains(entity.getUniqueId().toString());
    }

    private static ItemStack createMapItem(MapView mapView) {
        ItemStack item = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) item.getItemMeta();
        meta.setMapView(mapView);
        meta.setDisplayName(ChatColor.GOLD + "밈장 차트");
        item.setItemMeta(meta);
        return item;
    }

    private static void clearRenderers(MapView mapView) {
        for (MapRenderer renderer : new ArrayList<>(mapView.getRenderers())) {
            mapView.removeRenderer(renderer);
        }
    }

    private static void rebuildImage() {
        if (!isConfigured()) {
            return;
        }

        BufferedImage image = new BufferedImage(getCanvasWidth(), getCanvasHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBackground(g, image.getWidth(), image.getHeight());
        drawChart(g, image.getWidth(), image.getHeight());
        drawHeader(g, image.getWidth());
        drawCenterText(g, image.getWidth(), image.getHeight());
        drawHistory(g, image.getWidth(), image.getHeight());

        g.dispose();
        tileColors = splitToMapTiles(image);
        imageVersion++;
        for (GraphMapRenderer renderer : renderers) {
            renderer.markDirty();
        }
    }

    private static void drawBackground(Graphics2D g, int canvasWidth, int canvasHeight) {
        g.setColor(BACKGROUND);
        g.fillRect(0, 0, canvasWidth, canvasHeight);
        g.setColor(PANEL);
        g.fill(new RoundRectangle2D.Double(0, 0, canvasWidth, canvasHeight, 18, 18));
    }

    private static void drawHeader(Graphics2D g, int canvasWidth) {
        g.setColor(LINE);
        g.fillOval(canvasWidth - 59, 21, 30, 30);
        g.setColor(BACKGROUND);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        drawStringCentered(g, "B", canvasWidth - 44, 43);

        g.setColor(MUTED);
        g.setStroke(new BasicStroke(5F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int iconX = 20;
        int iconY = 34;
        g.drawLine(iconX, iconY, iconX + 8, iconY + 8);
        g.drawLine(iconX + 8, iconY + 8, iconX + 16, iconY - 2);
        g.drawLine(iconX + 16, iconY - 2, iconX + 26, iconY + 6);

        g.setColor(LINE);
        g.setFont(new Font("SansSerif", Font.BOLD, Math.max(18, canvasWidth / 30)));
        g.drawString(MarketTheme.getCurrentTicker() + " 코인", 58, 43);
    }

    private static void drawChart(Graphics2D g, int canvasWidth, int canvasHeight) {
        int left = Math.max(70, canvasWidth / 12);
        int right = canvasWidth - Math.max(22, canvasWidth / 38);
        int top = Math.max(52, canvasHeight / 8);
        int bottom = canvasHeight - Math.max(52, canvasHeight / 6);

        g.setColor(AXIS);
        g.setStroke(new BasicStroke(Math.max(2F, canvasWidth / 260F)));
        g.drawLine(left, top, left, bottom);
        g.drawLine(left, bottom, right, bottom);

        g.setFont(new Font("SansSerif", Font.BOLD, Math.max(15, canvasWidth / 38)));
        g.drawString("2x", 23, valueToY(200, top, bottom, getDisplayMax()) + 8);

        int ticks = Math.max(4, width * 3);
        for (int i = 0; i <= ticks; i++) {
            int x = left + ((right - left) * i / ticks);
            g.drawLine(x, bottom, x, bottom - 10);
            if (i % 2 == 0) {
                drawStringCentered(g, String.valueOf(i * 2), x, bottom + 30);
            }
        }
        int y2x = valueToY(200, top, bottom, getDisplayMax());
        g.drawLine(left, y2x, left + 10, y2x);

        drawCurve(g, left, right, top, bottom);
    }

    private static void drawCurve(Graphics2D g, int left, int right, int top, int bottom) {
        List<Integer> points = multipliers.isEmpty() ? Collections.singletonList(currentMultiplier) : multipliers;
        if (points.size() < 2) {
            return;
        }

        g.setColor(phase == Phase.BUSTED ? RED : LINE);
        g.setStroke(new BasicStroke(Math.max(5F, getCanvasWidth() / 110F), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int previousX = left;
        int previousY = valueToY(points.get(0), top, bottom, getDisplayMax());
        for (int i = 1; i < points.size(); i++) {
            int x = left + ((right - left) * i / Math.max(1, points.size() - 1));
            int y = valueToY(points.get(i), top, bottom, getDisplayMax());
            g.drawLine(previousX, previousY, x, y);
            previousX = x;
            previousY = y;
        }
    }

    private static void drawCenterText(Graphics2D g, int canvasWidth, int canvasHeight) {
        String text;
        Color color;
        if (phase == Phase.BETTING) {
            text = "개장 대기 " + countdown + "초";
            color = AXIS;
        } else if (phase == Phase.BUSTED) {
            text = "상폐 " + MULTIPLIER_FORMAT.format(bustedMultiplier / 100.0) + "x";
            color = RED;
        } else {
            text = MULTIPLIER_FORMAT.format(currentMultiplier / 100.0) + "x";
            color = AXIS;
        }

        int fontSize = phase == Phase.BETTING ? canvasWidth / 12 : canvasWidth / 5;
        if (phase == Phase.BUSTED) {
            fontSize = canvasWidth / 10;
        }
        g.setFont(new Font("SansSerif", Font.BOLD, Math.max(26, fontSize)));
        g.setColor(color);
        drawStringCentered(g, text, canvasWidth / 2, (canvasHeight / 2) + (g.getFontMetrics().getAscent() / 4));
    }

    private static void drawHistory(Graphics2D g, int canvasWidth, int canvasHeight) {
        int boxCount = Math.min(9, history.size());
        if (boxCount == 0) {
            return;
        }

        int gap = 4;
        int boxHeight = Math.max(34, canvasHeight / 10);
        int y = canvasHeight - boxHeight - 2;
        int boxWidth = (canvasWidth - ((boxCount - 1) * gap)) / boxCount;

        g.setFont(new Font("SansSerif", Font.BOLD, Math.max(14, boxHeight / 2)));
        for (int i = 0; i < boxCount; i++) {
            int value = history.get(history.size() - boxCount + i);
            boolean good = value >= 200;
            int x = i * (boxWidth + gap);
            g.setColor(good ? DARK_GREEN : DARK_RED);
            g.fill(new RoundRectangle2D.Double(x, y, boxWidth, boxHeight, 8, 8));
            g.setColor(good ? GREEN : RED);
            drawStringCentered(g, MULTIPLIER_FORMAT.format(value / 100.0) + "x", x + boxWidth / 2, y + (boxHeight / 2) + (g.getFontMetrics().getAscent() / 3));
        }
    }

    private static double getMaxProfit() {
        double maxProfit = 0.0;
        double multiplier = Math.max(1.0, currentMultiplier / 100.0);
        for (int amount : Game.activePlayerMap.values()) {
            maxProfit = Math.max(maxProfit, amount * multiplier);
        }
        return maxProfit;
    }

    private static int getDisplayMax() {
        int max = Math.max(250, currentMultiplier);
        for (int point : multipliers) {
            max = Math.max(max, point);
        }
        return Math.min(Math.max(max + 60, 300), Math.max(300, BustaMine.ccConfig.get().getInt("MultiplierMax", 120) * 100));
    }

    private static int valueToY(int value, int top, int bottom, int max) {
        double minValue = Math.log(1.0);
        double maxValue = Math.log(Math.max(2.5, max / 100.0));
        double current = Math.log(Math.max(1.0, value / 100.0));
        double ratio = (current - minValue) / Math.max(0.0001, maxValue - minValue);
        ratio = Math.max(0.0, Math.min(1.0, ratio));
        return bottom - (int) Math.round((bottom - top) * ratio);
    }

    private static byte[][] splitToMapTiles(BufferedImage image) {
        byte[][] tiles = new byte[width * height][];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                BufferedImage tile = image.getSubimage(x * MAP_SIZE, y * MAP_SIZE, MAP_SIZE, MAP_SIZE);
                tiles[y * width + x] = MapPalette.imageToBytes(tile);
            }
        }
        return tiles;
    }

    private static void sendMapsToNearbyPlayers() {
        if (!isConfigured()) {
            return;
        }
        double distance = BustaMine.ccConfig.get().getDouble("Graph.ViewDistance", 64.0);
        double distanceSquared = distance * distance;
        List<Location> locations = getBoardLocations();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!locations.isEmpty()) {
                boolean nearby = false;
                for (Location location : locations) {
                    if (player.getWorld().equals(location.getWorld()) && player.getLocation().distanceSquared(location) <= distanceSquared) {
                        nearby = true;
                        break;
                    }
                }
                if (!nearby) {
                    continue;
                }
            }
            for (MapView mapView : mapViews) {
                player.sendMap(mapView);
            }
        }
    }

    private static List<Location> getBoardLocations() {
        if (!boardLocations.isEmpty()) {
            return new ArrayList<>(boardLocations);
        }

        List<String> frameUUIDs = BustaMine.ccConfig.get().getStringList("Graph.FrameUUIDs");
        if (frameUUIDs.isEmpty()) {
            return Collections.emptyList();
        }
        List<Location> locations = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClass(ItemFrame.class)) {
                if (frameUUIDs.contains(entity.getUniqueId().toString())) {
                    locations.add(entity.getLocation());
                }
            }
        }
        return locations;
    }

    private static int getCanvasWidth() {
        return Math.max(MAP_SIZE, width * MAP_SIZE);
    }

    private static int getCanvasHeight() {
        return Math.max(MAP_SIZE, height * MAP_SIZE);
    }

    private static void drawStringCentered(Graphics2D g, String text, int centerX, int baselineY) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, centerX - (metrics.stringWidth(text) / 2), baselineY);
    }

    private static void drawStringRight(Graphics2D g, String text, int rightX, int baselineY) {
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, rightX - metrics.stringWidth(text), baselineY);
    }

    private static class GraphMapRenderer extends MapRenderer {

        private final int index;
        private long lastRenderedVersion = -1;

        private GraphMapRenderer(int index) {
            super(false);
            this.index = index;
        }

        private void markDirty() {
            lastRenderedVersion = -1;
        }

        @Override
        public void render(MapView map, MapCanvas canvas, Player player) {
            if (lastRenderedVersion == imageVersion || tileColors.length <= index) {
                return;
            }
            byte[] colors = tileColors[index];
            for (int i = 0; i < colors.length; i++) {
                canvas.setPixel(i % MAP_SIZE, i / MAP_SIZE, colors[i]);
            }
            lastRenderedVersion = imageVersion;
        }
    }

    private static class SelectedFrames {

        private final List<ItemFrame> frames;
        private final int width;
        private final int height;

        private SelectedFrames(List<ItemFrame> frames, int width, int height) {
            this.frames = frames;
            this.width = width;
            this.height = height;
        }
    }
}
