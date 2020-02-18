package info.stasha.proxywarrior;

import info.stasha.proxywarrior.config.loader.ConfigLoader;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import spark.ModelAndView;
import static spark.Spark.get;
import static spark.Spark.path;
import spark.servlet.SparkApplication;
import spark.template.thymeleaf.ThymeleafTemplateEngine;

/**
 *
 * @author stasha
 */
public class ProxyWarriorApp implements SparkApplication {

    public static void main(String[] args) throws Exception {
        Server server = new Server(9999);

        String rootPath = ProxyWarriorApp.class.getClassLoader().getResource(".").toString();
        WebAppContext webapp = new WebAppContext();
        webapp.setDescriptor(ProxyWarriorApp.class.getResource("/WEB-INF/web.xml").toString());
        webapp.setResourceBase("/");
        server.setHandler(webapp);

        server.start();
        server.join();

    }

    @Override
    public void init() {
        final Map model = new HashMap<>();

        path("/proxywarrior", () -> {
            get("/log", (req, res) -> new ModelAndView(new HashMap<>(), "LogTemplate"), new ThymeleafTemplateEngine());
            path("/config", () -> {

                get("/user", (req, res) -> {
                    model.put("config", ConfigLoader.getUserConfig());
                    return new ModelAndView(model, "UserConfigTemplate");
                }, new ThymeleafTemplateEngine());

                get("/effective", (req, res) -> {
                    model.put("config", ConfigLoader.getEffectiveConfig());
                    return new ModelAndView(model, "UserConfigTemplate");
                }, new ThymeleafTemplateEngine());
            });
        });
    }
}
