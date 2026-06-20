package me.sat7.bustaMine;

import me.sat7.bustaMine.Commands.BustaMineTabCompleter;
import me.sat7.bustaMine.Commands.Command;
import me.sat7.bustaMine.Events.OnClick;
import me.sat7.bustaMine.Events.OnJoinLeave;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

import static me.sat7.bustaMine.UpdateChecker.getResourceUrl;

public final class BustaMine extends JavaPlugin implements Listener
{

    public static BustaMine plugin;
    public static ConsoleCommandSender console;
    public static final String consolePrefix = "§6[BustaMine]§f ";
    public static String prefix = "";

    private static Economy econ = null; // 볼트에 물려있는 이코노미

    public static Economy getEconomy()
    {
        return econ;
    }

    public static final Random generator = new Random();

    public static CustomConfig ccConfig;
    public static CustomConfig ccBank;
    public static CustomConfig ccUser;
    public static CustomConfig ccLang;
    public static CustomConfig ccSound;

    @Override
    public void onEnable()
    {
        plugin = this;
        console = plugin.getServer().getConsoleSender();

        SetupVault();
    }

    private void SetupVault()
    {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
        {
            console.sendMessage(consolePrefix + " Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        else
        {
            console.sendMessage(consolePrefix + " Vault Found");
        }

        SetupRSP();
    }

    private int setupRspRetryCount = 0;
    private void SetupRSP()
    {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null)
        {
            econ = rsp.getProvider();

            Init();
        }
        else
        {
            if(setupRspRetryCount >= 3)
            {
                console.sendMessage(consolePrefix + " Disabled due to no Vault dependency found!");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            setupRspRetryCount++;
            console.sendMessage(consolePrefix + " Economy provider not found. Retry... " + setupRspRetryCount + "/3");

            Bukkit.getScheduler().runTaskLater(this, this::SetupRSP, 30L);
        }
    }

    private void Init()
    {
        ccConfig = new CustomConfig();
        ccBank = new CustomConfig();
        ccUser = new CustomConfig();
        ccLang = new CustomConfig();
        ccSound = new CustomConfig();

        // 이벤트 등록
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new OnClick(), this);
        getServer().getPluginManager().registerEvents(new OnJoinLeave(), this);

        // 명령어 등록 (개별 클레스로 되어있는것들)
        getCommand("BustaMine").setExecutor(new Command());
        getCommand("BustaMine").setTabCompleter(new BustaMineTabCompleter());

        Game.CalcOdds(); // 이건 컨픽보다 먼저 계산해둬야함.
        ConfigSetup.setupAll();
        GraphBoard.setup();
        NpcButtonManager.setup();

        console.sendMessage(consolePrefix + "Enabled! :)");

        // 업데이트 확인
        new UpdateChecker(this, UpdateChecker.PROJECT_ID).getVersion(version ->
        {
            try
            {
                if (getDescription().getVersion().equals(version))
                {
                    console.sendMessage("§6-------------------------------------------------------");
                    console.sendMessage(consolePrefix + "Plugin is up to date!");
                    console.sendMessage("Please rate my plugin if you like it");
                    console.sendMessage(getResourceUrl());
                    console.sendMessage("§6-------------------------------------------------------");
                } else
                {
                    console.sendMessage("§6-------------------------------------------------------");
                    console.sendMessage(consolePrefix + "Plugin outdated!!");
                    console.sendMessage(getResourceUrl());
                    console.sendMessage("§6-------------------------------------------------------");
                }
            } catch (Exception e)
            {
                console.sendMessage(consolePrefix + "Failed to check updated. Try again later.");
            }
        });

        // bstats
        try
        {
            int pluginId = 4334;
            Metrics metrics = new Metrics(this, pluginId);
        } catch (Exception e)
        {
            console.sendMessage(consolePrefix + "Failed to Init bstats : " + e);
        }

        // 게임인벤 생성
        Game.SetupGlass();
        Game.SetupSortedMap();
        Game.GameUISetup();

        // 첫 게임 시작
        Game.StartGame();
        Game.gameEnable = true;
    }


    @Override
    public void onDisable()
    {
        console.sendMessage(consolePrefix + "Disabled");
    }


    // 볼트 이코노미 초기화
    private boolean setupEconomy()
    {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
        {
            console.sendMessage(consolePrefix + " Vault Not Found");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
        {
            console.sendMessage(consolePrefix + "RSP is null!");
            return false;
        }
        econ = rsp.getProvider();
        console.sendMessage(consolePrefix + "Vault Found");
        return econ != null;
    }
}
