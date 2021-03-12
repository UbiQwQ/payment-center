package cn.wuyi.payment.rpc;

import java.util.Map;


public interface ITransOrderRpcService {

    Map create(String jsonParam);

    Map select(String jsonParam);

    Map selectByMchIdAndTransOrderId(String jsonParam);

    Map selectByMchIdAndMchTransNo(String jsonParam);

    Map updateStatus4Ing(String jsonParam);

    Map updateStatus4Success(String jsonParam);
    
    Map updateStatus4Fail(String jsonParam);
    
    Map updateStatus4Complete(String jsonParam);


}
