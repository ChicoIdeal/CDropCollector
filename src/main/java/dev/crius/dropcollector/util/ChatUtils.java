package dev.crius.dropcollector.util;

import dev.crius.dropcollector.DropCollectorPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.md_5.bungee.api.ChatColor;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatUtils {

    private final static Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");
    private final static MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    public static final DecimalFormat FORMATTER = (DecimalFormat) NumberFormat.getNumberInstance();

    static {
        FORMATTER.setMinimumIntegerDigits(1);
        FORMATTER.setMaximumIntegerDigits(20);
        FORMATTER.setMaximumFractionDigits(2);
        FORMATTER.setGroupingSize(3);
    }

    public static String colorLegacy(String string, Placeholder... placeholders) {
        for (Placeholder placeholder : placeholders) {
            string = string.replace(placeholder.getKey(), placeholder.getValue());
        }

        Matcher matcher = HEX_PATTERN.matcher(string);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of(matcher.group()).toString());
        }

        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    public static List<String> colorLegacy(List<String> list, Placeholder... placeholders) {
        return list.stream().map(s -> colorLegacy(s, placeholders)).collect(Collectors.toList());
    }

    public static Component format(String string, TagResolver... placeholders) {
        string = colorLegacy(string, new Placeholder("<prefix>",
                DropCollectorPlugin.getInstance().getPluginConfig().getString("Messages.prefix")));
        return MINI_MESSAGE.deserialize(string, placeholders);
    }

    public static List<Component> format(List<String> list, TagResolver... placeholders) {
        list = colorLegacy(list);

        return list.stream().map(s -> MINI_MESSAGE.deserialize(s, placeholders)).collect(Collectors.toList());
    }

}
