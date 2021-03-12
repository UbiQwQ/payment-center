package cn.wuyi.payment.rpc;

import java.util.Map;

public interface IPayChannel4WxRpcService {

    Map doWxPayReq(String jsonParam);

    Map doWxTransReq(String jsonParam);

    Map getWxTransReq(String jsonParam);

    Map doWxRefundReq(String jsonParam);

    Map getWxRefundReq(String jsonParam);

}
