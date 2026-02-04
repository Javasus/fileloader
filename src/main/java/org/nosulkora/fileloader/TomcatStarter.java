package org.nosulkora.fileloader;

import org.apache.catalina.startup.Tomcat;
import org.nosulkora.fileloader.servlet.UserServlet;

import java.io.File;

public class TomcatStarter {
    public static void startTomcat() throws Exception {
        System.out.println("Запуск Tomcat.");

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);

        String webappDir = new File("src/main/webapp").getAbsolutePath();
        System.out.println("Webapp path: " + webappDir);

        tomcat.addWebapp("/fileloader", webappDir);
        tomcat.getConnector();

        System.out.println("Tomcat запущен");
        System.out.println("URL: http://localhost:8080/fileloader");
        System.out.println("API: http://localhost:8080/fileloader/api/users");
        System.out.println("API: http://localhost:8080/fileloader/api/files");

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                tomcat.stop();
                tomcat.destroy();
                System.out.println("Tomcat остановлен");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        tomcat.start();
        tomcat.getServer().await();
    }
}
