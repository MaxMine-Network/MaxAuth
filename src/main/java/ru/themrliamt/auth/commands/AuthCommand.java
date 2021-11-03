package ru.themrliamt.auth.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import ru.themrliamt.auth.MaxAuth;
import ru.themrliamt.auth.types.AuthUser;
import ru.themrliamt.auth.utils.PasswordUtils;

public class AuthCommand extends Command {

    public AuthCommand() {
        super("auth");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ProxiedPlayer)) return;

        ProxiedPlayer player = ((ProxiedPlayer) sender);

        if(getPlayerLevel(player.getName()) < 100) {
            player.sendMessage("§cДанная возможность доступна от привилегии Администратор");
            player.sendMessage("§cА твой размер писюна - " + getPlayerLevel(player.getName()) + " см");
            return;
        }

        if(args.length == 0) {
            player.sendMessage("§cПодкоманды для Auth:");
            player.sendMessage("Сменить пароль игроку - §c/auth changepassword <ник игрока> <пароль>");
            player.sendMessage("Удалить аккаунт игроку - §c/auth remove <ник игрока>");
            player.sendMessage("Информация об аккаунте - §c/auth userinfo <ник игрока>");
            return;
        }

        switch (args[0]) {
            case "changepassword": {
                if(args.length < 3) {
                    player.sendMessage("[§cAuth§f] Укажите ник игрока и пароль");
                    return;
                }

                String name = args[1];
                String password = args[2];

                AuthUser user = MaxAuth.getUser(name);

                if(user.isRegistered()) {
                    user.getAuthData().setPassword(PasswordUtils.hash(password));
                    if(user.isLogined())
                        user.dislogin();
                    user.save();
                    player.sendMessage("[§cAuth§f] Пароль был успешно изменён");
                } else {
                    player.sendMessage("[§cAuth§f] §cПользователь не зарегистрирован");
                }

                break;
            }

            case "remove": {
                if(args.length < 2) {
                    player.sendMessage("[§cAuth§f] Укажите ник игрока, аккаунт которого Вы хотите удалить");
                    return;
                }

                String name = args[1];
                ProxiedPlayer p = ProxyServer.getInstance().getPlayer(name);

                if(p != null)
                    p.disconnect("§cПРОИЗОШЛА ОШИБКА В ТВОЕЙ ГОЛОВЕ");

                MaxAuth.getDatabase().execute("DELETE FROM `Auth`.`Auth` WHERE `Name` = ?", name.toLowerCase());
                MaxAuth.getDatabase().execute("DELETE FROM `Groups`.`Groups` WHERE `Name` = ?", name);

                player.sendMessage("[§cAuth§f] Аккаунт был удалён");
                break;
            }

            case "userinfo": {
                if(args.length < 2) {
                    player.sendMessage("[§cAuth§f] Укажите ник игрока, аккаунт которого хотите проверить");
                    return;
                }

                String name = args[1];

                ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(name);

                player.sendMessage("[§cAuth§] Игрок §c" + name + " §f-> " + (proxiedPlayer == null ? "§cофлайн" : "§aонлайн"));
                player.sendMessage("Последний Ip: §c" + MaxAuth.getUser(name).getAuthData().getLastIP());
                player.sendMessage("Ip при регистрации: §c" + MaxAuth.getUser(name).getAuthData().getRegIP());
                player.sendMessage("Vk: " + (MaxAuth.getUser(name).getAuthData().getVk() == null ? " §cне привязан" : MaxAuth.getUser(name).getAuthData().getVk()));
                player.sendMessage("Авторизирован: " + (MaxAuth.getUser(name).isLogined() ? " §aда" : "§cнет"));
                break;
            }
        }
    }

    public int getPlayerLevel(String player) {
        return MaxAuth.getDatabase().executeQuery("SELECT * FROM `Groups`.`Users` WHERE `Name` = ?", rs -> {
            if(rs.next()) return MaxAuth.getDatabase().executeQuery("SELECT * FROM `Groups`.`Groups` WHERE `Name` = ?", r -> {
                if(rs.next()) return r.getInt("Level");

                return 0;
            }, rs.getString("Group"));

            return 0;
        }, player);
    }

}
