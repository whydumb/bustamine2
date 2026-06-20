package me.sat7.bustaMine.Commands;

import me.sat7.bustaMine.BustaMine;
import me.sat7.bustaMine.CustomConfig;
import me.sat7.bustaMine.Game;
import me.sat7.bustaMine.GraphBoard;
import me.sat7.bustaMine.NpcButtonManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class Command implements CommandExecutor {

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Collections.reverse(list);

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {

        if(!(sender instanceof Player))
        {
            return true;
        }

        Player player = (Player)sender;

        if(args.length == 0)
        {
            if(player.hasPermission("bm.user.money"))
            {
                Game.OpenGameInven(player, Game.bustaType.money);
            }
            else if(player.hasPermission("bm.user.exp"))
            {
                Game.OpenGameInven(player, Game.bustaType.exp);
            }
            else
            {
                player.sendMessage(BustaMine.prefix+BustaMine.ccLang.get().getString("Message.NoPermission"));
                return true;
            }
        }

        if (args.length >= 1)
        {
            switch (args[0])
            {
                case "help":
                case "?":
                    player.sendMessage(BustaMine.prefix + "도움말");
                    player.sendMessage("/bm [money | exp]  §e밈장 열기");
                    player.sendMessage("/bm stats [player]");
                    player.sendMessage("/bm top [NetProfit | NetProfit_Exp | GamesPlayed]");
                    if (player.hasPermission("bm.admin"))
                    {
                        player.sendMessage(BustaMine.ccLang.get().getString("Help.BmGo"));
                        player.sendMessage(BustaMine.ccLang.get().getString("Help.BmStop"));
                        player.sendMessage(BustaMine.ccLang.get().getString("Help.BmStatistics"));
                        player.sendMessage("/bm graph [select | info | clear | reload]");
                        player.sendMessage("/bm npc [create | remove | list | clear | reload]");
                        player.sendMessage(BustaMine.ccLang.get().getString("Help.BmTest"));
                        player.sendMessage(BustaMine.ccLang.get().getString("Help.BmReloadConfig"));
                        player.sendMessage(BustaMine.ccLang.get().getString("Help.BmReloadLang"));
                        player.sendMessage(BustaMine.ccLang.get().getString("Help.BmReloadLangWarning"));
                    }
                    break;
                case "money":
                    if (!player.hasPermission("bm.user.money"))
                    {
                        player.sendMessage(BustaMine.prefix + BustaMine.ccLang.get().getString("Message.NoPermission"));
                        return true;
                    }

                    Game.OpenGameInven(player, Game.bustaType.money);
                    break;
                case "exp":
                    if (!player.hasPermission("bm.user.exp"))
                    {
                        player.sendMessage(BustaMine.prefix + BustaMine.ccLang.get().getString("Message.NoPermission"));
                        return true;
                    }

                    Game.OpenGameInven(player, Game.bustaType.exp);
                    break;
                case "stats":
                    if (!player.hasPermission("bm.user.stats"))
                    {
                        player.sendMessage(BustaMine.prefix + BustaMine.ccLang.get().getString("Message.NoPermission"));
                        return true;
                    }

                    if (args.length > 1)
                    {
                        Player p = Bukkit.getPlayer(args[1]);
                        if (p != null)
                        {
                            Game.ShowPlayerInfo(player, p);
                        } else
                        {
                            player.sendMessage(BustaMine.prefix + BustaMine.ccLang.get().getString("Message.PlayerNotExist"));
                        }
                    } else
                    {
                        Game.ShowPlayerInfo(player, player);
                    }
                    break;
                case "top":
                    if (!player.hasPermission("bm.user.top"))
                    {
                        player.sendMessage(BustaMine.prefix + BustaMine.ccLang.get().getString("Message.NoPermission"));
                        return true;
                    }

                    if (args.length >= 2)
                    {
                        if (args[1].equals("NetProfit") || args[1].equals("NetProfit_Exp") || args[1].equals("GamesPlayed"))
                        {
                            Game.Top(player, args[1]);
                        } else
                        {
                            player.sendMessage(BustaMine.prefix + "[NetProfit | NetProfit_Exp | GamesPlayed]");
                        }
                    } else
                    {
                    Game.Top(player, "NetProfit");
                    }
                    break;
                case "graph":
                    if (!player.hasPermission("bm.admin"))
                    {
                        player.sendMessage(BustaMine.prefix + BustaMine.ccLang.get().getString("Message.NoPermission"));
                        return true;
                    }

                    if (args.length < 2)
                    {
                        player.sendMessage(BustaMine.prefix + "/bm graph [select | info | clear | reload]");
                        return true;
                    }

                    switch (args[1].toLowerCase(Locale.ROOT))
                    {
                        case "select":
                            GraphBoard.beginSelection(player);
                            break;
                        case "info":
                            GraphBoard.info(player);
                            break;
                        case "clear":
                            GraphBoard.clear(player);
                            break;
                        case "reload":
                            GraphBoard.setup();
                            player.sendMessage(BustaMine.prefix + "밈장 차트를 리로드했습니다.");
                            break;
                        default:
                            player.sendMessage(BustaMine.prefix + "/bm graph [select | info | clear | reload]");
                            break;
                    }
                    break;
                case "npc":
                    if (!player.hasPermission("bm.admin"))
                    {
                        player.sendMessage(BustaMine.prefix + BustaMine.ccLang.get().getString("Message.NoPermission"));
                        return true;
                    }

                    if (args.length < 2)
                    {
                        player.sendMessage(BustaMine.prefix + "/bm npc [create | remove | list | clear | reload]");
                        player.sendMessage(BustaMine.prefix + "동작: menu(밈장), cashout(익절/탈출), bet-small(진입), bet-medium(물타기), bet-big(풀진입)");
                        return true;
                    }

                    switch (args[1].toLowerCase(Locale.ROOT))
                    {
                        case "create":
                            if (args.length < 3)
                            {
                                player.sendMessage(BustaMine.prefix + "동작: menu(밈장), cashout(익절/탈출), bet-small(진입), bet-medium(물타기), bet-big(풀진입)");
                                return true;
                            }
                            NpcButtonManager.create(player, args[2]);
                            break;
                        case "remove":
                            NpcButtonManager.removeNearest(player);
                            break;
                        case "list":
                            NpcButtonManager.list(player);
                            break;
                        case "clear":
                            NpcButtonManager.clear(player);
                            break;
                        case "reload":
                            NpcButtonManager.setup();
                            player.sendMessage(BustaMine.prefix + "NPC 버튼을 리로드했습니다.");
                            break;
                        default:
                            player.sendMessage(BustaMine.prefix + "/bm npc [create | remove | list | clear | reload]");
                            break;
                    }
                    break;
                case "reloadConfig":
                    if (!player.hasPermission("bm.admin"))
                    {
                        player.sendMessage(BustaMine.prefix + BustaMine.ccLang.get().getString("Message.NoPermission"));
                        return true;
                    }

                    BustaMine.ccConfig.reload();
                    BustaMine.ccBank.reload();
                    BustaMine.ccUser.reload();
                    BustaMine.ccSound.reload();
                    BustaMine.UpdateConfig();
                    Game.RefreshIcons();
                    GraphBoard.setup();
                    NpcButtonManager.setup();

                    player.sendMessage(BustaMine.prefix + BustaMine.ccLang.get().getString("Message.Reload_FromNextRound"));
                    break;
                case "reloadLang":
                    if (!player.hasPermission("bm.admin"))
                    {
                        player.sendMessage(BustaMine.prefix + BustaMine.ccLang.get().getString("Message.NoPermission"));
                        return true;
                    }

                    BustaMine.ccLang.reload();
                    BustaMine.UpdateConfig();

                    Game.GameUISetup();
                    Game.StartGame();
                    Game.gameEnable = true;

                    player.sendMessage(BustaMine.prefix + "밈장이 서버에 의해 종료되었습니다.");
                    player.sendMessage(BustaMine.prefix + BustaMine.ccLang.get().getString("Message.Reload2"));
                    break;
                case "go":
                    if (!player.hasPermission("bm.admin"))
                    {
                        player.sendMessage(BustaMine.prefix + BustaMine.ccLang.get().getString("Message.NoPermission"));
                        return true;
                    }

                    Game.StartGame();
                    Game.gameEnable = true;
                    player.sendMessage(BustaMine.prefix + BustaMine.ccLang.get().getString("Message.Start"));
                    break;
                case "stop":
                    if (!player.hasPermission("bm.admin"))
                    {
                        player.sendMessage(BustaMine.prefix + BustaMine.ccLang.get().getString("Message.NoPermission"));
                        return true;
                    }

                    Game.gameEnable = false;
                    player.sendMessage(BustaMine.prefix + BustaMine.ccLang.get().getString("Message.Stop"));
                    break;
                case "test":
                    if (!player.hasPermission("bm.admin"))
                    {
                        player.sendMessage(BustaMine.prefix + BustaMine.ccLang.get().getString("Message.NoPermission"));
                        return true;
                    }

                    CustomConfig debugResult = new CustomConfig();
                    String name = "TestResult_" + System.currentTimeMillis();
                    debugResult.setup(name);

                    ArrayList<Integer> tempList = new ArrayList<>();
                    for (int i = 0; i < 100000; i++)
                    {
                        tempList.add(Game.GenBustNum());
                    }

                    debugResult.get().set("result", tempList);
                    debugResult.save();

                    player.sendMessage(BustaMine.prefix + "File generated. plugins/BustaMine/" + name + ".yml");
                    break;
                case "statistics":
                    if (!player.hasPermission("bm.admin"))
                    {
                        player.sendMessage(BustaMine.prefix + BustaMine.ccLang.get().getString("Message.NoPermission"));
                        return true;
                    }

                    Game.ShowStatistics(player);
                    break;
            }
        }

        return true;
    }
}
