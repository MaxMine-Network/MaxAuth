package ru.themrliamt.auth.types;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class AuthData {

    private String password;
    private String lastIP;
    private String regIP;
    @SerializedName("vk_id")
    private String vk;
    @SerializedName("session")
    private long sessionTime;

    public void setLastIP(String lastIP) {
        this.lastIP = lastIP;
    }

    public String getLastIP() {
        return lastIP;
    }

    public void setSessionTime(int sessionTime) {
        this.sessionTime = sessionTime;
    }

    public int getSessionTime() {
        return (int) sessionTime;
    }


    public String getVk() {
        return vk;
    }

    public void setVk(String vk) {
        this.vk = vk;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRegIP(String regIP) {
        this.regIP = regIP;
    }

    public String getRegIP() {
        return regIP;
    }
}
