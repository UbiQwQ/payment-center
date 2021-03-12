package cn.wuyi.payment.rpc;

import java.util.Map;


public interface INotifyPayRpcService {

    Map doAliPayNotify(String jsonParam);

    Map doWxPayNotify(String jsonParam);

    Map sendBizPayNotify(String jsonParam);
}
