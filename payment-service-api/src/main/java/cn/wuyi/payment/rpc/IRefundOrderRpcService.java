package cn.wuyi.payment.rpc;

import java.util.Map;


public interface IRefundOrderRpcService {

    Map create(String jsonParam);

    Map select(String jsonParam);

    Map selectByMchIdAndRefundOrderId(String jsonParam);

    Map selectByMchIdAndMchRefundNo(String jsonParam);

    Map updateStatus4Ing(String jsonParam);

    Map updateStatus4Success(String jsonParam);
    
    Map updateStatus4Fail(String jsonParam);
    
    Map updateStatus4Complete(String jsonParam);


}
