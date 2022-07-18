package dev.crius.dropcollector.command;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.util.ChatUtils;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.Default;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;

@SuppressWarnings("unused")
@RequiredArgsConstructor
@Command(value = "dropcollector", alias = "cdropcollector")
public class MainCommand extends BaseCommand {

    protected final DropCollectorPlugin plugin;

    @Default
    @Permission("dropcollector.command")
    public void defaultCommand(CommandSender sender) {
        String message = String.join("\n", plugin.getPluginConfig().getStringList("Messages.help"));
        plugin.getAdventure().sender(sender).sendMessage(ChatUtils.format(
                message, Placeholder.unparsed("version", plugin.getDescription().getVersion())
        ));
    }

}
