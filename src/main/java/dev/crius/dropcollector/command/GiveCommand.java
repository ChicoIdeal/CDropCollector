package dev.crius.dropcollector.command;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.entity.CEntity;
import dev.crius.dropcollector.util.ChatUtils;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.Optional;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
@RequiredArgsConstructor
@Command(value = "dropcollector", alias = "cdropcollector")
public class GiveCommand extends BaseCommand {

    private final DropCollectorPlugin plugin;

    @SubCommand("give")
    @Permission("dropcollector.command.give")
    public void giveCommand(CommandSender sender, CEntity entity, @Optional Player target) {
        if (sender instanceof Player && target == null) {
            target = (Player) sender;
        }

        target.getInventory().addItem(plugin.getCollectorManager().createCreator(entity, target.getUniqueId()));

        plugin.getAdventure().sender(sender).sendMessage(ChatUtils.format(
                plugin.getPluginConfig().getString("Messages.gave-a-creator"),
                Placeholder.unparsed("target", target.getName()),
                Placeholder.unparsed("entity-displayname", entity.getDisplayName())
        ));

        plugin.getAdventure().player(target).sendMessage(ChatUtils.format(
                plugin.getPluginConfig().getString("Messages.got-a-creator"),
                Placeholder.unparsed("entity-displayname", entity.getDisplayName())
        ));
    }

}
