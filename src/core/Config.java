package core;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final String FILE_PATH = "resources/settings.properties";
    private static Properties properties = new Properties();

    // Значения по умолчанию (если файла нет)
    public static int width = 800;
    public static int height = 600;
    public static String title = "Game";
    public static boolean vSync = true;
    public static float hFov = 90;

    public static void load() {
        try (FileInputStream fis = new FileInputStream(FILE_PATH)) {
            properties.load(fis);

            // Читаем значения и конвертируем типы
            // Второй аргумент в getProperty — это значение по умолчанию
            width = Integer.parseInt(properties.getProperty("window.width", "800"));
            height = Integer.parseInt(properties.getProperty("window.height", "600"));
            title = properties.getProperty("window.title", "My Game");
            vSync = Boolean.parseBoolean(properties.getProperty("window.vSync", "true"));
            hFov = Float.parseFloat(properties.getProperty("window.horizontalFov", "90"));

            System.out.println("Конфигурация успешно загружена.");
        } catch (IOException e) {
            System.err.println("Файл настроек не найден. Используются дефолтные значения.");
        } catch (NumberFormatException e) {
            System.err.println("Ошибка в формате чисел в конфиге!");
        }
    }

}
