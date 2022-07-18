package dev.crius.dropcollector.command;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.util.ChatUtils;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;

@SuppressWarnings("unused")
@RequiredArgsConstructor
@Command(value = "dropcollector", alias = "cdropcollector")
public class ReloadCommand extends BaseCommand {

    private final DropCollectorPlugin plugin;

    @SubCommand("reload")
    @Permission("dropcollector.command.reload")
    public void reloadCommand(CommandSender sender) {
        plugin.getPluginConfig().create();
        plugin.getEntityManager().init();
        plugin.getUpgradeManager().init();
        plugin.getAdventure().sender(sender).sendMessage(ChatUtils.format(
                plugin.getPluginConfig().getString("Messages.reloaded")
        ));
    }

}
