package ru.themrliamt.auth.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import ru.themrliamt.auth.MaxAuth;
import ru.themrliamt.auth.types.AuthUser;

public class LogoutCommand extends Command {
    public LogoutCommand() {
        super("logout", null, "exit");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer) sender;

        AuthUser user = MaxAuth.getUser(player);

        if(user != null)
            user.dislogin();
    }
}
