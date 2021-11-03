package ru.themrliamt.auth.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import ru.themrliamt.auth.MaxAuth;
import ru.themrliamt.auth.types.AuthUser;

public class ChangePassword extends Command {
    public ChangePassword() {
        super("changepassword");
    }

    public void execute(CommandSender commandSender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer)commandSender;
        if (2 > args.length) {
            player.sendMessage("§f[§cMaxMine Auth§f] Ошибка, пишите /changepassword <старый пароль> <новый пароль>");
        } else {
            AuthUser user = MaxAuth.getUser(player);
            String oldPasword = args[0];
            String newPassword = args[1];
            if (user.changePassword(oldPasword, newPassword)) {
                TextComponent password = new TextComponent("Наведите");
                password.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder("Ваш пароль: " + newPassword + "\n Сохраните его!")).create()));
                BaseComponent[] component = (new ComponentBuilder("§f[§cMaxMine Auth§f] Вы сменили пароль!")).append("\n").append(password).append(", что бы посмотреть пароль ещё раз").create();
                player.sendMessage(component);
            }

        }
    }
}
