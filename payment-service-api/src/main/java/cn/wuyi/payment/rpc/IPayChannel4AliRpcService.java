package cn.wuyi.payment.rpc;

import java.util.Map;


public interface IPayChannel4AliRpcService {

    Map doAliPayWapReq(String jsonParam);

    Map doAliPayPcReq(String jsonParam);

    Map doAliPayMobileReq(String jsonParam);

    Map doAliPayQrReq(String jsonParam);

    Map doAliTransReq(String jsonParam);

    Map getAliTransReq(String jsonParam);

    Map doAliRefundReq(String jsonParam);

    Map getAliRefundReq(String jsonParam);

}
