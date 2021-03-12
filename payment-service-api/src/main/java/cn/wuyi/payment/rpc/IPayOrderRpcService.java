package cn.wuyi.payment.rpc;

import java.util.Map;

public interface IPayOrderRpcService {

    Map create(String jsonParam);

    Map select(String jsonParam);

    Map selectByMchIdAndPayOrderId(String jsonParam);

    Map selectByMchIdAndMchOrderNo(String jsonParam);

    Map selectByMchIdAndMchOrderNoAndChannelOrderNo(String jsonParam);
    
    Map updateStatus4Ing(String jsonParam);

    Map updateStatus4Success(String jsonParam);

    Map updateStatus4Complete(String jsonParam);

    Map updateNotify(String jsonParam);

}
