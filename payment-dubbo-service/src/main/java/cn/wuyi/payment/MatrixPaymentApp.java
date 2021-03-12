package cn.wuyi.payment;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class MatrixPaymentApp {

    public static void main(String[] args) throws IOException {
    	
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{
                "classpath*:applicationPaymentContext.xml",  
                });
        context.start();
        
        System.out.println("matrix-payment-service 启动成功...");
        System.in.read();  
    }
}
















