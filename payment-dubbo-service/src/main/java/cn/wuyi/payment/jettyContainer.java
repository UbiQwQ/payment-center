package cn.wuyi.payment;

import cn.wuyi.payment.tool.PropertyPlaceholder;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.dubbo.container.Container;
import com.matrix.base.BaseLog;
import lombok.SneakyThrows;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class jettyContainer implements Container {

    private static final String JETTY_PORT = "jetty.port";

    private static final Logger LOGGER = LoggerFactory.getLogger(MatrixPaymentAppJetty.class);


    public jettyContainer() {
    }

    @SneakyThrows
    @Override
    public void start() {
        new jettyContainer().startJetty();
    }

    @Override
    public void stop() {


    }


    private void startJetty() throws Exception {
        BaseLog.getInstance().sysoutInfo("[-------------------------------------------jetty启动中!]", this.getClass());
        Integer port = Integer.parseInt(String.valueOf(PropertyPlaceholder.getProperty(JETTY_PORT)));
        Server server = new Server(port);
        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(StatViewServlet.class, "/druid/*");
        server.setHandler(handler);
        addRuntimeShutdownHook(server);

        BaseLog.getInstance().sysoutInfo("[-------------------------------------------jetty启动成功端口：" + port + "!]", this.getClass());
        server.start();
        server.join();
    }


    private static void addRuntimeShutdownHook(final Server server) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (server.isStarted()) {
                    server.setStopAtShutdown(true);
                    try {
                        server.stop();
                    } catch (Exception e) {
                        System.out.println("Error while stopping jetty server: " + e.getMessage());
                        LOGGER.error("Error while stopping jetty server: " + e.getMessage(), e);
                    }
                }
            }
        }));
    }

}
