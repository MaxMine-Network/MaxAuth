package ru.themrliamt.auth.types;

import com.google.gson.Gson;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import ru.themrliamt.auth.MaxAuth;
import ru.themrliamt.auth.utils.PasswordUtils;

import java.util.concurrent.TimeUnit;

public class AuthUser {

    private static final Gson GSON = new Gson();

    private String name;
    private AuthData authData;
    private boolean registered;
    private boolean logined;

    public AuthUser(String name, String ip) {
        this.name = name;
        this.authData = this.loadData();
        this.registered = this.authData.getPassword() != null;
        this.logined = System.currentTimeMillis() < this.authData.getSessionTime() && this.authData.getLastIP().equals(ip);
    }

    public boolean hasVK() {
        return this.authData.getVk() != null && !this.authData.getVk().isEmpty();
    }

    public void dislogin() {
        this.authData.setLastIP("");
        this.authData.setSessionTime(0);
        this.logined = false;
        this.save();
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(name);
        if(player != null) {
            player.disconnect("§cВы вышли из аккаунта");
        }
    }

    public boolean auth(ProxiedPlayer player, String password) {
        if (!this.registered) {
            this.authData.setPassword(PasswordUtils.hash(password));
            this.authData.setRegIP(player.getAddress().getHostName());
            this.authData.setLastIP(player.getAddress().getHostName());
            this.authData.setSessionTime(Math.toIntExact(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1L)));
            this.registered = true;
            this.logined = true;
            this.save();
            return true;
        } else {
            String pHash = PasswordUtils.hash(password);
            if (!this.authData.getPassword().equals(pHash)) {
                return false;
            } else {
                this.authData.setLastIP(player.getAddress().getHostName());
                this.authData.setSessionTime(Math.toIntExact(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1L)));
                this.logined = true;
                this.save();
                return true;
            }
        }
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        if (!this.authData.getPassword().equals(PasswordUtils.hash(oldPassword))) {
            return false;
        } else {
            this.authData.setPassword(PasswordUtils.hash(newPassword));
            this.save();
            return true;
        }
    }

    private AuthData loadData() {
        return MaxAuth.getDatabase().executeQuery("SELECT * FROM `Auth`.`Auth` WHERE `Name` = ?", (rs) -> {
            return rs.next() ? GSON.fromJson(rs.getString("Data"), AuthData.class) : new AuthData();
        }, this.name.toLowerCase());
    }

    public void save() {
        String json = GSON.toJson(this.authData);
        MaxAuth.getDatabase().execute("INSERT INTO `Auth`.`Auth`(`Name`, `Data`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `Data` = ?", this.name.toLowerCase(), json, json);
    }

    public String getName() {
        return this.name;
    }

    public AuthData getAuthData() {
        return this.authData;
    }

    public boolean isRegistered() {
        return this.registered;
    }

    public boolean isLogined() {
        return this.logined;
    }
}

