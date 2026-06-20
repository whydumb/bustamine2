package me.sat7.bustaMine.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class BustaMineTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (args.length == 1) {
            ArrayList<String> temp = new ArrayList<>();
            ArrayList<String> alist = new ArrayList<>();

            temp.add("help");

            if (sender.hasPermission("bm.admin")) {
                temp.add("go");
                temp.add("stop");
                temp.add("statistics");
                temp.add("graph");
                temp.add("npc");
                temp.add("reloadConfig");
                temp.add("reloadLang");
                temp.add("test");
            }
            if (sender.hasPermission("bm.user.money") || sender.hasPermission("bm.user.exp")) {
                temp.add("money");
                temp.add("exp");
                temp.add("stats");
                temp.add("top");
            }

            for (String s : temp) {
                if (s.startsWith(args[0])) alist.add(s);
            }

            return alist;
        } else if (args.length > 1) {
            ArrayList<String> temp = new ArrayList<>();
            ArrayList<String> alist = new ArrayList<>();

            if (args[0].equals("top") && (sender.hasPermission("bm.user.money") || sender.hasPermission("bm.user.exp"))) {
                temp.add("NetProfit");
                temp.add("NetProfit_Exp");
                temp.add("GamesPlayed");
            } else if (args[0].equals("graph") && sender.hasPermission("bm.admin")) {
                temp.add("select");
                temp.add("info");
                temp.add("clear");
                temp.add("reload");
            } else if (args[0].equals("npc") && sender.hasPermission("bm.admin")) {
                temp.add("create");
                temp.add("remove");
                temp.add("list");
                temp.add("clear");
                temp.add("reload");
            }

            if (args.length == 3 && args[0].equals("npc") && args[1].equals("create") && sender.hasPermission("bm.admin")) {
                temp.clear();
                temp.add("menu");
                temp.add("cashout");
                temp.add("bet-small");
                temp.add("bet-medium");
                temp.add("bet-big");
                temp.add("exp-small");
                temp.add("exp-medium");
                temp.add("exp-big");
                temp.add("밈장");
                temp.add("익절");
                temp.add("탈출");
                temp.add("진입");
                temp.add("물타기");
                temp.add("풀진입");
            }

            String currentArg = args.length == 3 ? args[2] : args[1];
            for (String s : temp) {
                if (s.startsWith(currentArg)) alist.add(s);
            }

            return alist;
        }

        return null;
    }
}
