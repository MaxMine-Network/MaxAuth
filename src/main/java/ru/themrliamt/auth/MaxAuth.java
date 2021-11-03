package ru.themrliamt.auth;

import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import ru.themrliamt.auth.commands.AuthCommand;
import ru.themrliamt.auth.commands.ChangePassword;
import ru.themrliamt.auth.commands.LogoutCommand;
import ru.themrliamt.auth.database.MySQL;
import ru.themrliamt.auth.listeners.AuthListener;
import ru.themrliamt.auth.types.AuthUser;

import java.util.HashMap;
import java.util.Map;

public class MaxAuth extends Plugin {
    private static Map<String, AuthUser> users = new HashMap<>();

    @Getter
    private static final MySQL database = new MySQL("localhost", "root", "(($@(%^RWEXMDO###", "Auth");
            /*
            MySQL.newBuilder()
        .host("localhost")
        .user("root")
        .password("(($@(%^RWEXMDO###")
        .database("Auth")
        .create(); */

    @Getter
    private static MaxAuth instance;

    @Override
    public void onEnable() {
        instance = this;

        getProxy().getPluginManager().registerListener(this, new AuthListener());
        getProxy().getPluginManager().registerCommand(this, new ChangePassword());
        getProxy().getPluginManager().registerCommand(this, new LogoutCommand());
        getProxy().getPluginManager().registerCommand(this, new AuthCommand());
    }

    public static void loadUser(String name, String ip) {
        users.put(name.toLowerCase(), new AuthUser(name, ip));
    }

    public static AuthUser getUser(String name) {
        return users.getOrDefault(name.toLowerCase(), new AuthUser(name, name));
    }

    public static AuthUser getUser(ProxiedPlayer player) {
        return getUser(player.getName());
    }

    public static void unloadUser(String name) {
        users.remove(name.toLowerCase());
    }

    public static MySQL getDatabase() {
        return database;
    }
    public static MaxAuth getInstance() {
        return instance;
    }

}
