package cn.wuyi.payment.rpcimpl;

import cn.wuyi.payment.rpc.ITransOrderRpcService;
import cn.wuyi.payment.service.impl.BaseService4TransOrder;
import com.alibaba.fastjson.JSONObject;
import com.matrix.common.domain.BaseParam;
import com.matrix.common.enumm.RetEnum;
import com.matrix.common.util.*;
import com.matrix.dao.model.TransOrder;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("transOrderRpcService")
public class TransOrderRpcServiceImpl extends BaseService4TransOrder implements ITransOrderRpcService {

    private static final MyLog _log = MyLog.getLog(TransOrderRpcServiceImpl.class);



    @Override
    public Map create(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("新增转账订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject transOrderObj = baseParam.isNullValue("transOrder") ? null : JSONObject.parseObject(bizParamMap.get("transOrder").toString());
        if(transOrderObj == null) {
            _log.warn("新增转账订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        TransOrder transOrder = BeanConvertUtils.map2Bean(transOrderObj, TransOrder.class);
        if(transOrder == null) {
            _log.warn("新增转账订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result = super.baseCreateTransOrder(transOrder);
        return RpcUtil.createBizResult(baseParam, result);
    }

    @Override
    public Map select(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("根据转账订单号查询转账订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String transOrderId = baseParam.isNullValue("transOrderId") ? null : bizParamMap.get("transOrderId").toString();
        if (ObjectValidUtil.isInvalid(transOrderId)) {
            _log.warn("根据转账订单号查询转账订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        TransOrder transOrder = super.baseSelectTransOrder(transOrderId);
        if(transOrder == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(transOrder);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }

    @Override
    public Map selectByMchIdAndTransOrderId(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("根据商户号和转账订单号查询转账订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String mchId = baseParam.isNullValue("mchId") ? null : bizParamMap.get("mchId").toString();
        String transOrderId = baseParam.isNullValue("transOrderId") ? null : bizParamMap.get("transOrderId").toString();
        if (ObjectValidUtil.isInvalid(mchId, transOrderId)) {
            _log.warn("根据商户号和转账订单号查询转账订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        TransOrder transOrder = super.baseSelectByMchIdAndTransOrderId(mchId, transOrderId);
        if(transOrder == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(transOrder);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }

    @Override
    public Map selectByMchIdAndMchTransNo(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("根据商户号和商户订单号查询支付订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String mchId = baseParam.isNullValue("mchId") ? null : bizParamMap.get("mchId").toString();
        String mchTransNo = baseParam.isNullValue("mchTransNo") ? null : bizParamMap.get("mchTransNo").toString();
        if (ObjectValidUtil.isInvalid(mchId, mchTransNo)) {
            _log.warn("根据商户号和商户订单号查询支付订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        TransOrder transOrder = super.baseSelectByMchIdAndMchTransNo(mchId, mchTransNo);
        if(transOrder == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(transOrder);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }

    @Override
    public Map updateStatus4Ing(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("修改转账订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String transOrderId = baseParam.isNullValue("transOrderId") ? null : bizParamMap.get("transOrderId").toString();
        String channelOrderNo = baseParam.isNullValue("channelOrderNo") ? null : bizParamMap.get("channelOrderNo").toString();
        if (ObjectValidUtil.isInvalid(transOrderId)) {
            _log.warn("修改转账订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result =  super.baseUpdateStatus4Ing(transOrderId, channelOrderNo);
        return RpcUtil.createBizResult(baseParam, result);
    }

    @Override
    public Map updateStatus4Success(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("修改转账订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String transOrderId = baseParam.isNullValue("transOrderId") ? null : bizParamMap.get("transOrderId").toString();
        if (ObjectValidUtil.isInvalid(transOrderId)) {
            _log.warn("修改转账订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result =  super.baseUpdateStatus4Success(transOrderId);
        return RpcUtil.createBizResult(baseParam, result);
    }
    
	@Override
	public Map updateStatus4Fail(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("修改转账订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String transOrderId = baseParam.isNullValue("transOrderId") ? null : bizParamMap.get("transOrderId").toString();
        String channelErrCode = baseParam.isNullValue("channelErrCode") ? null : bizParamMap.get("channelErrCode").toString();
        String channelErrMsg = baseParam.isNullValue("channelErrMsg") ? null : bizParamMap.get("channelErrMsg").toString();
        if (ObjectValidUtil.isInvalid(transOrderId)) {
            _log.warn("修改转账订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result =  super.baseUpdateStatus4Fail(transOrderId, channelErrCode, channelErrMsg);
        return RpcUtil.createBizResult(baseParam, result);		
	}
	
	
    @Override
    public Map updateStatus4Complete(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("修改转账订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String transOrderId = baseParam.isNullValue("transOrderId") ? null : bizParamMap.get("transOrderId").toString();
        if (ObjectValidUtil.isInvalid(transOrderId)) {
            _log.warn("修改转账订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result =  super.baseUpdateStatus4Complete(transOrderId);
        return RpcUtil.createBizResult(baseParam, result);
    }


	

}
