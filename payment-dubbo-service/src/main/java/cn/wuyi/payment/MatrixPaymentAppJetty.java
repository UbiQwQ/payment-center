package cn.wuyi.payment;

import com.alibaba.druid.support.http.StatViewServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @description:通过内嵌jetty 启动
 *
 * @author: liwt
 * @date: 2020/10/21 9:58
 * @version: 1.0.1
 */
public class MatrixPaymentAppJetty {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatrixPaymentAppJetty.class);

    private static final int PORT = 8217;

    public static void main(String[] args) throws Exception {
        new MatrixPaymentAppJetty().startJetty();
    }

    void startJetty() throws Exception {
        System.out.println("**************************************\nStarting server at port" + MatrixPaymentAppJetty.PORT + "\n**************************************");
        Server server = new Server(MatrixPaymentAppJetty.PORT);


        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(StatViewServlet.class, "/druid/*");
        server.setHandler(handler);
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{
                "classpath*:applicationPaymentContext.xml",
        });
        context.start();


        addRuntimeShutdownHook(server);

        server.start();
        System.out.println("**************************************\nServer started at port " + MatrixPaymentAppJetty.PORT + "\n**************************************");
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
