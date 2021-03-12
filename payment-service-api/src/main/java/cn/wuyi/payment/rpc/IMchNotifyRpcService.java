package cn.wuyi.payment.rpc;

import java.util.Map;

public interface IMchNotifyRpcService {
	
    Map createMchNotify(String jsonParam);
    Map updateMchNotifySuccess(String jsonParam);
    Map updateMchNotifyFail(String jsonParam);
}
