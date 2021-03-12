package cn.wuyi.payment;

/**
 * @description:通过dubbo自带的main方法  spi启动
 * @author: liwt
 * @date: 2020/10/21 9:57
 * @version: 1.0.1
 */
public class MatrixPaymentAppDubboMain {
    public static void main(String[] args) {

        com.alibaba.dubbo.container.Main.main(new String[]{"spring","jetty"});
    }
}
