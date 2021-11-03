package ru.themrliamt.auth.listeners;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;
import ru.maxmine.bungee.packets.PacketManager;
import ru.maxmine.bungee.packets.player.PlayerJoinPacket;
import ru.themrliamt.auth.MaxAuth;
import ru.themrliamt.auth.types.AuthUser;
import ru.themrliamt.auth.utils.VKUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class AuthListener implements Listener {

    private VKUtils vkUtils = new VKUtils();

    private Map<String, ScheduledTask> loginTask = new HashMap<>();
    private Map<String, String> vkIp = new HashMap<>();
    private Map<String, String> vkAuth = new HashMap<>();
    private Map<String, Integer> trying = new HashMap<>();

    @EventHandler
    public void onPreLogin(PreLoginEvent e) {
        InitialHandler handler = (InitialHandler) e.getConnection();

        if(!handler.getHandshake().getHost().toLowerCase().contains("maxmine")) {
            e.setCancelled(true);
            e.setCancelReason(new TextComponent("§cВНИМАНИЕ!\n §7Заходите на сервер по адресу: \n§cmc.maxmine.su"));
        }
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        String name = event.getConnection().getName();
        String ip = event.getConnection().getAddress().getHostName();
        MaxAuth.loadUser(name, ip);
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent e) {
        MaxAuth.unloadUser(e.getPlayer().getName());
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent e) {
        AuthUser user = MaxAuth.getUser(e.getPlayer());
        if (user.isLogined() && user.hasVK() && !this.hasVKAuth(user)) {
            (new Thread(() -> {
                String randomCode = String.valueOf(1111 + new Random().nextInt(9999));
                this.vkUtils.sendCode(randomCode, user.getAuthData().getVk(), e.getPlayer().getAddress().getHostName());
                this.vkAuth.put(randomCode, user.getName().toLowerCase());
            })).start();
            this.processAuth(e.getPlayer(), user);
        } else if (!user.isLogined()) {
            this.processAuth(e.getPlayer(), user);
        } else {
            ProxyServer.getInstance().getScheduler().schedule(MaxAuth.getInstance(), () ->
                    e.getPlayer().connect(ProxyServer.getInstance().getServerInfo("Hub-1")), 200L, TimeUnit.MILLISECONDS);

            PlayerJoinPacket packet = new PlayerJoinPacket(e.getPlayer());
            PacketManager.sendPacket(packet);

            if (!user.hasVK()) {
                e.getPlayer().sendMessage("[§cAuth§f] Привяжите аккаунт к ВКонтакте. Для этого напишите в сообщения группа §cvk.com/maxmine §fслово §c'Привязать'");
            }
        }

    }

    @EventHandler
    public void onRedirect(ServerConnectEvent e) {
        AuthUser user = MaxAuth.getUser(e.getPlayer());
        if (!user.isLogined() && !e.getTarget().getName().contains("Auth")) {
            e.setTarget(ProxyServer.getInstance().getServerInfo("Auth-1"));
        }

    }

    @EventHandler(priority = -32)
    public void onChat(ChatEvent e) {
        AuthUser user = MaxAuth.getUser(((ProxiedPlayer) e.getSender()).getName());
        ProxiedPlayer player = (ProxiedPlayer) e.getSender();
        if (e.getMessage().startsWith("/") && !user.isLogined()) {
            e.setCancelled(true);
            String[] message = e.getMessage().split(" ");
            String command = message[0].replace("/", "");
            String[] args = new String[message.length - 1];
            System.arraycopy(message, 1, args, 0, message.length - 1);

            String password;
            int t = trying.getOrDefault(player.getName().toLowerCase(), 0);

            switch (command) {
                case "l":
                case "login":
                    if (args.length != 0) {
                        password = args[0];
                        if (user.auth(player, password)) {
                            if (user.hasVK() && !this.hasVKAuth(user)) {
                                new Thread(() -> {
                                    String randomCode = String.valueOf(1111 + new Random().nextInt(9999));
                                    this.vkUtils.sendCode(randomCode, user.getAuthData().getVk(), player.getAddress().getHostName());
                                    this.vkAuth.put(randomCode, user.getName().toLowerCase());
                                }).start();
                                return;
                            }

                            player.sendMessage("[§cAuth§f] Вы успешно авторизовались.");
                            player.connect(ProxyServer.getInstance().getServerInfo("Hub-1"));
                            PlayerJoinPacket packet = new PlayerJoinPacket(player);
                            PacketManager.sendPacket(packet);
                            return;
                        }
                        if (t < 3) {
                            player.sendMessage("[§cAuth§f] §cНеверный пароль!");
                            t++;//голова уже просто болит ниче не соображаю пиздец отдохни ебать
                        } else {
                            player.disconnect("§cБыло произведено 3 неверных попытки входа");
                            this.vkUtils.sendWarning(player.getAddress().getHostName(), player.getName(), user.getAuthData().getVk());

                        }
                        trying.put(player.getName().toLowerCase(), t);
                    }
                    break;

                case "reg":
                case "register":
                    if (2 <= args.length && !user.isRegistered()) {
                        password = args[0];
                        String passwordRepeat = args[1];
                        if (!password.equals(passwordRepeat)) {
                            player.sendMessage("[§cAuth§f] §cПовторный пароль введён неверно!");
                        } else if (!user.auth(player, password)) {
                            player.disconnect("[§cAuth§f] §cПроизошла непредвиденная ошибка.\n\nСообщите администрации:\nvk.me/maxmine");
                        } else {
                            player.sendMessage("[§cAuth§f] Вы успешно зарегистрировались.");
                            player.connect(ProxyServer.getInstance().getServerInfo("Hub-1"));
                            PlayerJoinPacket packet = new PlayerJoinPacket(player);
                            PacketManager.sendPacket(packet);
                        }
                    }
            }
        }

        if (user.hasVK() && !this.hasVKAuth(user) && user.isLogined()) {
            e.setCancelled(true);
            String code = e.getMessage();
            if (!this.checkVKAuth(user, code)) {
                player.sendMessage("[§cAuth§f] §cКод был введён неверно!");
            } else {
                player.connect(ProxyServer.getInstance().getServerInfo("Hub-1"));
                PlayerJoinPacket packet = new PlayerJoinPacket(player);
                PacketManager.sendPacket(packet);
                vkIp.put(player.getName().toLowerCase(), player.getAddress().getHostName());
            }
        }
    }

    public boolean checkVKAuth(AuthUser user, String code) {
        if (this.vkAuth.containsKey(code) && this.vkAuth.get(code).equals(user.getName().toLowerCase())) {
            this.vkAuth.remove(code);
            this.vkAuth.put(user.getName().toLowerCase(), code);
            return true;
        } else {
            return false;
        }
    }

    public boolean hasVKAuth(AuthUser user) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(user.getName());

        return this.vkAuth.containsKey(user.getName().toLowerCase()) && vkIp.containsKey(user.getName().toLowerCase())
                && vkIp.get(user.getName().toLowerCase()).equals(player.getAddress().getHostName());
    }

    private void processAuth(ProxiedPlayer player, AuthUser user) {
        ScheduledTask task = ProxyServer.getInstance().getScheduler().schedule(MaxAuth.getInstance(), () -> {

            if(player == null || !player.isConnected()) {
                this.loginTask.get(player.getName().toLowerCase()).cancel();
                return;
            }

            if (!user.isRegistered()) {
                player.sendMessage("[§cAuth§f] Зарегистрируйтесь - §c/register §f[§спароль§f] §f[§спароль§f]");
            } else if (!user.isLogined()) {
                player.sendMessage("[§cAuth§f] Авторизируйтесь - §c/login §f[§cпароль§f]");
            } else if (user.hasVK() && !this.hasVKAuth(user)) {
                player.sendMessage("[§cAuth§f] Введите код, отправленный Вам в сообщении ВКонтакте.");
            }

            if (user.isLogined() || !player.isConnected()) {
                if (user.hasVK() && !this.hasVKAuth(user)) {
                    return;
                }

                if (!user.hasVK()) {
                    player.sendMessage("[§cAuth§f] Привяжите аккаунт к ВКонтакте. Для этого напишите в сообщения группа §cvk.com/maxmine §fслово §c'Привязать'");
                }
                this.loginTask.get(player.getName().toLowerCase()).cancel();

            }

        }, 0L, 3L, TimeUnit.SECONDS);

        this.loginTask.put(player.getName().toLowerCase(), task);

        ProxyServer.getInstance().getScheduler().schedule(MaxAuth.getInstance(), () -> {
            if (player.isConnected() && !user.isLogined()) {
                player.disconnect("[§cAuth§f] Вы не успели ввести пароль");
                this.loginTask.get(player.getName().toLowerCase()).cancel();
            }

        }, 40L, TimeUnit.SECONDS);
    }
}
