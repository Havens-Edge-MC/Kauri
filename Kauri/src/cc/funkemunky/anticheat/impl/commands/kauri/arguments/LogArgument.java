package cc.funkemunky.anticheat.impl.commands.kauri.arguments;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.anticheat.impl.menu.MenuUtils;
import cc.funkemunky.api.commands.FunkeArgument;
import cc.funkemunky.api.commands.FunkeCommand;
import cc.funkemunky.api.utils.Color;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class LogArgument extends FunkeArgument {
    public LogArgument(FunkeCommand parent, String name, String display, String description, String... permission) {
        super(parent, name, display, description, permission);

        addAlias("log");
        addAlias("view");
        addTabComplete(2, "gui", "web");
    }

    @Override
    public void onArgument(CommandSender sender, Command command, String[] args) {
        if (args.length > 2) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);

            if (target == null) {
                sender.sendMessage(getParent().getCommandMessages().getErrorColor() + "The player \"" + args[0] + "\" could not be found in Mojang's databases.");
                return;
            }
            switch(args[1].toLowerCase()) {
                case "gui": {
                    Player player = (Player) sender;

                    MenuUtils.openLogGUI(player, target);
                    break;
                }
                case "web": {
                    val violations = Kauri.getInstance().getLoggerManager().getViolations(target.getUniqueId());

                    StringBuilder url = new StringBuilder("https://funkemunky.cc/api/kauri?uuid=" + target.getUniqueId().toString().replaceAll("-", "") + (violations.keySet().size() > 0 ? "&violations=" : ""));

                   if(violations.keySet().size() > 0) {
                        for (String key : violations.keySet()) {
                            if(Kauri.getInstance().getCheckManager().isCheck(key)) {
                                Check check = Kauri.getInstance().getCheckManager().getCheck(key);
                                int vl = violations.get(key), maxVL = check.getMaxVL();
                                boolean developer = check.isDeveloper();

                                String toAppend = key + ":" + vl + ":" + maxVL + ":" + developer + ";";
                                toAppend = toAppend.replaceAll(" ", "%20");

                                url.append(toAppend);

                            }
                        }

                        if(violations.keySet().size() > 0) {
                            url.deleteCharAt(url.length() - 1);
                        }

                        String finalURL = "http://funkemunky.cc/api/kauri/cache/%id%";

                        try {
                            URL url2Run = new URL(url.toString());
                            //%3F
                            BufferedReader reader = new BufferedReader(new InputStreamReader(url2Run.openConnection().getInputStream(), Charset.forName("UTF-8")));

                            finalURL = finalURL.replace("%id%", readAll(reader));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        sender.sendMessage(Color.translate("&aView the log here&7: &f" + finalURL));
                    } else {
                       sender.sendMessage(Color.translate("&cThis player does not have any logs."));
                   }
                    break;
                }
                default: {
                    sender.sendMessage(getParent().getCommandMessages().getErrorColor() + getParent().getCommandMessages().getInvalidArguments());
                    break;
                }
            }
        }else {
            sender.sendMessage(getParent().getCommandMessages().getErrorColor() + getParent().getCommandMessages().getInvalidArguments());
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
