package me.sat7.bustaMine;

import java.util.Arrays;

public class ConfigSetup {

    public static void setupAll() {
        setupConfig();
        setupBank();
        setupUser();
        setupLang();
        setupSound();
        updateConfig();
    }

    public static void setupConfig() {
        // 컨픽
        BustaMine.ccConfig.setup("Config");
        // 주석
        BustaMine.ccConfig.get().options().header("Changes will take effect from the next round." +
                "\nRoundInterval: 3~ (real time second) / Default: 5" +
                "\nMultiplierMax: 30~150 / Default: 120" +
                "\nProbabilityOfInstaBust: 0.8~20.0 / Default: 1.0 (%) / The final value may vary depending on the MultiplierMax." +
                "\n" +
                "\nCommand.WhenRoundStart: placeholder: n/a" +
                "\nCommand.WhenPlayerBet: placeholder: {player} {amount}" +
                "\nCommand.WhenPlayerCashOut: placeholder: {player} {amount} {multiplier} {prize}" +
                "\nCommand.WhenRoundEnd: placeholder: {multiplier}"
        );
        BustaMine.ccConfig.get().addDefault("CurrencySymbol", "$");
        BustaMine.ccConfig.get().addDefault("RoundInterval", 5);
        BustaMine.ccConfig.get().addDefault("MultiplierMax", 120);
        BustaMine.ccConfig.get().addDefault("ProbabilityOfInstaBust", 2.0);
        BustaMine.ccConfig.get().addDefault("ShowWinChance", true);
        BustaMine.ccConfig.get().addDefault("ShowBankroll", true);
        BustaMine.ccConfig.get().addDefault("LoadPlayerSkin", true);
        BustaMine.ccConfig.get().addDefault("UIForceUpdate", false);

        BustaMine.ccConfig.get().addDefault("Bet.Small", 10);
        BustaMine.ccConfig.get().addDefault("Bet.Medium", 100);
        BustaMine.ccConfig.get().addDefault("Bet.Big", 1000);
        BustaMine.ccConfig.get().addDefault("Bet.Max", 0);
        BustaMine.ccConfig.get().addDefault("Bet.ExpSmall", 10);
        BustaMine.ccConfig.get().addDefault("Bet.ExpMedium", 100);
        BustaMine.ccConfig.get().addDefault("Bet.ExpBig", 1000);
        BustaMine.ccConfig.get().addDefault("Bet.ExpMax", 0);

        BustaMine.ccConfig.get().addDefault("Broadcast.Jackpot", 30);
        BustaMine.ccConfig.get().addDefault("Broadcast.InstaBust", true);

        BustaMine.ccConfig.get().addDefault("Graph.Enabled", false);
        BustaMine.ccConfig.get().addDefault("Graph.ViewDistance", 64);
        BustaMine.ccConfig.get().addDefault("Graph.ProtectFrames", true);

        BustaMine.ccConfig.get().addDefault("Npc.BitcoinHeadTexture", NpcButtonManager.DEFAULT_BITCOIN_TEXTURE);

        BustaMine.ccConfig.get().addDefault("Market.Tickers", Arrays.asList(
                "DOGE2", "MINE", "CREEPER", "PONZI", "TNTCOIN", "VILLAGER_AI", "BEDROCK", "RUGPULL", "MOON",
                "NPCOIN", "DIAMOND_HANDS", "POTATO_AI", "EMERALD_BANK", "SLIMEFI", "GOLEM", "ENDERMAN",
                "FROG_SWAP", "AXOLOTL", "BLAZE", "GHAST", "WARDEN", "MACE", "BREEZE", "HONEY", "MUSHROOM",
                "SHULKER", "ELYTRA", "TRIDENT", "LAVA", "WATER", "COOKIE", "CAKE", "CARROT", "MELON",
                "PIGLIN", "SQUID2", "SNOWBALL", "REDSTONE", "LAPIS", "QUARTZ", "NETHER", "TOTEM", "BEACON",
                "COWBANK", "SHEEP", "CHICKEN", "WHEAT", "BAMBOO", "KELP", "DRIP", "AMETHYST", "COPPER",
                "IRON", "GOLD", "NETHERITE", "OBSIDIAN", "SAND", "GRAVEL", "ANVIL", "RAIL", "MINECART",
                "BOAT", "FISH", "PUFFER", "SALMON", "COD", "BREAD", "APPLE", "BERRY", "CHORUS", "PHANTOM",
                "RABBIT", "TURTLE", "GOAT", "WOLF", "CAT", "PARROT", "PANDA", "FOX", "CAMEL", "SNIFFER",
                "ARMADILLO", "RAVAGER", "RAID", "PORTAL", "ENDER", "ECHO", "SCULK", "VAULT", "TRIAL",
                "OMINOUS", "SPAWNER", "MUSIC", "RELIC", "CREATOR", "PRECIPICE", "WIND", "CHARGE", "DUMP",
                "HODL"
        ));
        BustaMine.ccConfig.get().addDefault("Market.Icons", Arrays.asList(
                "DIAMOND", "EMERALD", "GOLD_INGOT", "GOLD_NUGGET", "GOLD_BLOCK", "RAW_GOLD", "COPPER_INGOT", "RAW_COPPER",
                "IRON_INGOT", "RAW_IRON", "NETHERITE_INGOT", "NETHERITE_SCRAP", "AMETHYST_SHARD", "QUARTZ", "REDSTONE",
                "LAPIS_LAZULI", "COAL", "CHARCOAL", "NETHER_STAR", "EXPERIENCE_BOTTLE", "ENDER_PEARL", "ENDER_EYE",
                "BLAZE_ROD", "BLAZE_POWDER", "GHAST_TEAR", "SLIME_BALL", "MAGMA_CREAM", "FIRE_CHARGE", "TNT",
                "CREEPER_HEAD", "DRAGON_HEAD", "PLAYER_HEAD", "SKELETON_SKULL", "WITHER_SKELETON_SKULL", "ZOMBIE_HEAD",
                "PIGLIN_HEAD", "GOAT_HORN", "TOTEM_OF_UNDYING", "HEART_OF_THE_SEA", "NAUTILUS_SHELL", "ELYTRA", "TRIDENT",
                "BOW", "CROSSBOW", "FISHING_ROD", "SHEARS", "SPYGLASS", "CLOCK", "COMPASS", "RECOVERY_COMPASS", "NAME_TAG",
                "LEAD", "SADDLE", "MINECART", "CHEST_MINECART", "TNT_MINECART", "HOPPER_MINECART", "OAK_BOAT", "CHEST",
                "ENDER_CHEST", "SHULKER_BOX", "WHITE_SHULKER_BOX", "ORANGE_SHULKER_BOX", "MAGENTA_SHULKER_BOX",
                "LIGHT_BLUE_SHULKER_BOX", "YELLOW_SHULKER_BOX", "LIME_SHULKER_BOX", "PINK_SHULKER_BOX", "GRAY_SHULKER_BOX",
                "LIGHT_GRAY_SHULKER_BOX", "CYAN_SHULKER_BOX", "PURPLE_SHULKER_BOX", "BLUE_SHULKER_BOX", "BROWN_SHULKER_BOX",
                "GREEN_SHULKER_BOX", "RED_SHULKER_BOX", "BLACK_SHULKER_BOX", "CAKE", "COOKIE", "BREAD", "APPLE",
                "GOLDEN_APPLE", "ENCHANTED_GOLDEN_APPLE", "CARROT", "GOLDEN_CARROT", "POTATO", "POISONOUS_POTATO",
                "BEETROOT", "MELON_SLICE", "SWEET_BERRIES", "GLOW_BERRIES", "CHORUS_FRUIT", "HONEY_BOTTLE", "MILK_BUCKET",
                "LAVA_BUCKET", "WATER_BUCKET", "POWDER_SNOW_BUCKET", "AXOLOTL_BUCKET", "TROPICAL_FISH_BUCKET",
                "PUFFERFISH_BUCKET", "SALMON_BUCKET", "COD_BUCKET", "MUSIC_DISC_13", "MUSIC_DISC_CAT", "MUSIC_DISC_OTHERSIDE",
                "MUSIC_DISC_RELIC", "WIND_CHARGE", "BREEZE_ROD", "MACE", "OMINOUS_BOTTLE"
        ));

        BustaMine.ccConfig.get().addDefault("Command.WhenRoundStart", "");
        BustaMine.ccConfig.get().addDefault("Command.WhenPlayerBet", "");
        BustaMine.ccConfig.get().addDefault("Command.WhenPlayerCashOut", "");
        BustaMine.ccConfig.get().addDefault("Command.WhenRoundEnd", "");

        BustaMine.ccConfig.get().addDefault("BtnIcon.Bankroll", "DIAMOND");
        BustaMine.ccConfig.get().addDefault("BtnIcon.WinChance", "PAPER");
        BustaMine.ccConfig.get().addDefault("BtnIcon.MyState", "PAPER");
        BustaMine.ccConfig.get().addDefault("BtnIcon.History", "PAPER");
        BustaMine.ccConfig.get().addDefault("BtnIcon.CashOut", "EMERALD");
        BustaMine.ccConfig.get().addDefault("BtnIcon.CashOutSetting", "PAPER");
        BustaMine.ccConfig.get().addDefault("BtnIcon.BetSmall", "GOLD_NUGGET");
        BustaMine.ccConfig.get().addDefault("BtnIcon.BetMedium", "GOLD_INGOT");
        BustaMine.ccConfig.get().addDefault("BtnIcon.BetBig", "GOLD_BLOCK");

        BustaMine.ccConfig.get().options().copyDefaults(true);
        BustaMine.ccConfig.save();
    }

    public static void setupBank() {
        // 컨픽
        BustaMine.ccBank.setup("Bank");
        BustaMine.ccBank.get().addDefault("Bankroll.Money", 500000);
        BustaMine.ccBank.get().addDefault("Bankroll.Exp", 500000);
        BustaMine.ccBank.get().addDefault("Statistics.Income.Money", 0);
        BustaMine.ccBank.get().addDefault("Statistics.Expense.Money", 0);
        BustaMine.ccBank.get().addDefault("Statistics.Income.Exp", 0);
        BustaMine.ccBank.get().addDefault("Statistics.Expense.Exp", 0);

        BustaMine.ccBank.get().options().copyDefaults(true);
        BustaMine.ccBank.save();
    }

    public static void setupUser() {
        BustaMine.ccUser.setup("User");
        BustaMine.ccUser.get().options().copyDefaults(true);
        BustaMine.ccUser.save();
    }

    public static void setupLang() {
        BustaMine.ccLang.setup("Lang");
        BustaMine.ccLang.get().addDefault("Message.Prefix", "§6§l[밈장]§r ");
        BustaMine.ccLang.get().addDefault("Message.Instabust", "§4상장 즉시 상폐!");
        BustaMine.ccLang.get().addDefault("Message.Start", "§f밈장 개장.");
        BustaMine.ccLang.get().addDefault("Message.Stop", "§f밈장 폐장 예약.");
        BustaMine.ccLang.get().addDefault("Message.NotEnoughMoney", "§e시드가 부족합니다.");
        BustaMine.ccLang.get().addDefault("Message.NotEnoughExp", "§e경험치 시드가 부족합니다.");
        BustaMine.ccLang.get().addDefault("Message.DivUpper", "§6╔══════════════╗");
        BustaMine.ccLang.get().addDefault("Message.DivLower", "§6╚══════════════╝");
        BustaMine.ccLang.get().addDefault("Message.NoPermission", "§e권한이 없습니다.");
        BustaMine.ccLang.get().addDefault("Message.Reload2", "§f리로드 완료.");
        BustaMine.ccLang.get().addDefault("Message.Reload_FromNextRound", "§f리로드 완료. 다음 라운드부터 적용됩니다.");
        BustaMine.ccLang.get().addDefault("Message.PlayerNotExist", "§f플레이어가 없습니다.");
        BustaMine.ccLang.get().addDefault("Message.LastUpdate", "마지막 갱신: {sec}초 전");

        BustaMine.ccLang.get().addDefault("UI.Title", "§2[ 밈장 ]");
        BustaMine.ccLang.get().addDefault("UI.BetBtn", "§6§l진입");
        BustaMine.ccLang.get().addDefault("UI.BetSmall", "§6§l진입");
        BustaMine.ccLang.get().addDefault("UI.BetMedium", "§6§l물타기");
        BustaMine.ccLang.get().addDefault("UI.BetBig", "§6§l풀진입");
        BustaMine.ccLang.get().addDefault("UI.CashOut", "§6§l익절 / 탈출");
        BustaMine.ccLang.get().addDefault("UI.History", "§6§l차트 기록");
        BustaMine.ccLang.get().addDefault("UI.Bankroll", "§6§l유동성 풀");
        BustaMine.ccLang.get().addDefault("UI.PlayerInfo", "§f진입금: {amount}");
        BustaMine.ccLang.get().addDefault("UI.MyState", "§6§l내 포지션");
        BustaMine.ccLang.get().addDefault("UI.Click", "§e클릭");
        BustaMine.ccLang.get().addDefault("UI.WinChance", "§6§l급등 확률");
        BustaMine.ccLang.get().addDefault("UI.Close", "§6§l닫기");
        BustaMine.ccLang.get().addDefault("UI.CashOutSetting", "§6§l자동 익절");

        BustaMine.ccLang.get().addDefault("CO.Title", "§2[ 자동 익절 ]");
        BustaMine.ccLang.get().addDefault("CO.-10", "§6§l-10");
        BustaMine.ccLang.get().addDefault("CO.-1", "§6§l-1");
        BustaMine.ccLang.get().addDefault("CO.-01", "§6§l-0.1");
        BustaMine.ccLang.get().addDefault("CO.+10", "§6§l+10");
        BustaMine.ccLang.get().addDefault("CO.+1", "§6§l+1");
        BustaMine.ccLang.get().addDefault("CO.+01", "§6§l+0.1");
        BustaMine.ccLang.get().addDefault("CO.x", "§fx");
        BustaMine.ccLang.get().addDefault("CO.Enabled", "§f켜짐");
        BustaMine.ccLang.get().addDefault("CO.Disabled", "§f꺼짐");
        BustaMine.ccLang.get().addDefault("CO.OnOff", "§6§l자동 익절");
        BustaMine.ccLang.get().addDefault("CO.PlayMoneyGame", "§6§l모의 시드 밈장");
        BustaMine.ccLang.get().addDefault("CO.PlayExpGame", "§6§l경험치 밈장");

        BustaMine.ccLang.get().addDefault("Help.BmGo", "/bm go   §e밈장 개장");
        BustaMine.ccLang.get().addDefault("Help.BmStop", "/bm stop   §e밈장 폐장 예약");
        BustaMine.ccLang.get().addDefault("Help.BmStatistics", "/bm statistics   §e장부 보기");
        BustaMine.ccLang.get().addDefault("Help.BmReloadConfig", "/bm reloadConfig   §e설정 리로드");
        BustaMine.ccLang.get().addDefault("Help.BmReloadLang", "/bm reloadLang   §e언어 파일 리로드");
        BustaMine.ccLang.get().addDefault("Help.BmReloadLangWarning", "§c주의: 리로드하면 현재 라운드가 종료됩니다.");
        BustaMine.ccLang.get().addDefault("Help.BmTest", "/bm test   §e랜덤 상폐 숫자 테스트");

        BustaMine.ccLang.get().addDefault("MyBal", "내 시드");
        BustaMine.ccLang.get().addDefault("Bal", "시드");
        BustaMine.ccLang.get().addDefault("Money", "모의 시드");
        BustaMine.ccLang.get().addDefault("Exp", "경험치 시드");
        BustaMine.ccLang.get().addDefault("MaximumMultiplier", "상한");
        BustaMine.ccLang.get().addDefault("Bet", "진입");
        BustaMine.ccLang.get().addDefault("CashedOut", "익절");
        BustaMine.ccLang.get().addDefault("Busted", "상폐");
        BustaMine.ccLang.get().addDefault("Income", "유입");
        BustaMine.ccLang.get().addDefault("Expense", "유출");
        BustaMine.ccLang.get().addDefault("Profit", "수익");
        BustaMine.ccLang.get().addDefault("NetProfit", "누적 수익");
        BustaMine.ccLang.get().addDefault("GamesPlayed", "진입 횟수");
        BustaMine.ccLang.get().addDefault("Leaderboard", "수익 랭킹");
        BustaMine.ccLang.get().addDefault("BettingLimit", "§f진입 한도를 초과했습니다.");

        BustaMine.ccLang.get().options().copyDefaults(true);
        BustaMine.ccLang.save();
    }

    public static void setupSound() {
        BustaMine.ccSound.setup("Sound");
        BustaMine.ccSound.get().options().header("Enter 0 to play nothing.\nhttps://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html");
        //BustaMine.ccSound.get().addDefault("RoundStart","0");
        BustaMine.ccSound.get().addDefault("Bet", "ENTITY_PAINTING_PLACE");
        BustaMine.ccSound.get().addDefault("CashOut", "ENTITY_EXPERIENCE_ORB_PICKUP");
        BustaMine.ccSound.get().addDefault("Bust", "ENTITY_PLAYER_HURT");
        //BustaMine.ccSound.get().addDefault("RoundEnd","ENTITY_CHICKEN_EGG");
        BustaMine.ccSound.get().addDefault("Click", "0");
        BustaMine.ccSound.get().options().copyDefaults(true);
        BustaMine.ccSound.save();
    }

    public static void updateConfig() {
        if (BustaMine.ccConfig.get().contains("Bankroll")) {
            BustaMine.ccBank.get().set("Bankroll.Money", BustaMine.ccConfig.get().getDouble("Bankroll"));
            BustaMine.ccBank.save();
            BustaMine.ccConfig.get().set("Bankroll", null);
        }

        //--------------------------------

        BustaMine.prefix = BustaMine.ccLang.get().getString("Message.Prefix");

        if (BustaMine.ccConfig.get().getInt("MultiplierMax") > 150) BustaMine.ccConfig.get().set("MultiplierMax", 150);
        if (BustaMine.ccConfig.get().getInt("MultiplierMax") < 30) BustaMine.ccConfig.get().set("MultiplierMax", 30);

        if (BustaMine.ccConfig.get().getDouble("ProbabilityOfInstaBust") > 20) BustaMine.ccConfig.get().set("ProbabilityOfInstaBust", 20.0);
        if (BustaMine.ccConfig.get().getDouble("ProbabilityOfInstaBust") < 0.8) BustaMine.ccConfig.get().set("ProbabilityOfInstaBust", 0.8);

        if (BustaMine.ccConfig.get().getInt("RoundInterval") < 3) BustaMine.ccConfig.get().set("RoundInterval", 3);

        Game.maxMulti = BustaMine.ccConfig.get().getInt("MultiplierMax");

        Game.baseInstabust = BustaMine.ccConfig.get().getDouble("ProbabilityOfInstaBust") / 100 - Game.oddList[Game.maxMulti - 1];
        if (Game.baseInstabust < 0) Game.baseInstabust = 0;

        BustaMine.ccConfig.save();
    }
}
