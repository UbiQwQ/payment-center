package cn.wuyi.payment.rpc;

import java.util.Map;


public interface IPayChannelRpcService {
	Map createPayChannel(String jsonParam);
	Map updatePayChannel(String jsonParam);
    Map selectPayChannel(String jsonParam);

}
