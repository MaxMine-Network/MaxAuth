package ru.themrliamt.auth.utils;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class VKUtils {

    private final String VK_TOKEN = "dcfcd9fe5d28240734a903348f3d48882c0c1eda2f45ff3c1825f97d70177384533d6aa5eb2221f729ba8";
    private final String SEND_MESSAGE = "https://api.vk.com/method/messages.send?v=5.52&access_token=%s&message=%s&user_id=%s";

    public void sendCode(String code, String user, String ip) {
        try {
            URL url = new URL(String.format(SEND_MESSAGE,
                    VK_TOKEN,
                    URLEncoder.encode("Ваш код для авторизации: " + code + ".\nIP: " + ip, "UTF-8"),
                    user
            ));

            HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.getInputStream().close();
        } catch (Exception ignored) { }
    }

    public void sendWarning(String ip, String name, String user) {
        try {
            URL url = new URL(String.format(SEND_MESSAGE,
                    VK_TOKEN,
                    URLEncoder.encode("Произведена неудачная попытка входа с IP: " + ip + "\nНик аккаунта: " + name, "UTF-8"),
                    user
            ));

            HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.getInputStream().close();
        } catch (Exception ignored) {}
    }
}
