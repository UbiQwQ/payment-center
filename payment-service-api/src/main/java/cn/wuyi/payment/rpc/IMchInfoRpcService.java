package cn.wuyi.payment.rpc;

import java.util.Map;

public interface IMchInfoRpcService {
    Map selectMchInfo(String jsonParam);
    Map createMchInfo(String jsonParam);
}
