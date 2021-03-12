package cn.wuyi.payment.rpcimpl;

import cn.wuyi.payment.rpc.IRefundOrderRpcService;
import cn.wuyi.payment.service.impl.BaseService4RefundOrder;
import com.alibaba.fastjson.JSONObject;
import com.matrix.common.domain.BaseParam;
import com.matrix.common.enumm.RetEnum;
import com.matrix.common.util.*;
import com.matrix.dao.model.RefundOrder;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("refundOrderRpcService")
public class RefundOrderRpcServiceImpl extends BaseService4RefundOrder implements IRefundOrderRpcService {

    private static final MyLog _log = MyLog.getLog(RefundOrderRpcServiceImpl.class);


    @Override
    public Map create(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("新增退款订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject refundOrderObj = baseParam.isNullValue("refundOrder") ? null : JSONObject.parseObject(bizParamMap.get("refundOrder").toString());
        if(refundOrderObj == null) {
            _log.warn("新增退款订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        RefundOrder refundOrder = BeanConvertUtils.map2Bean(refundOrderObj, RefundOrder.class);
        if(refundOrder == null) {
            _log.warn("新增退款订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result = super.baseCreateRefundOrder(refundOrder);
        return RpcUtil.createBizResult(baseParam, result);
    }

    @Override
    public Map select(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("根据退款订单号查询退款订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String refundOrderId = baseParam.isNullValue("refundOrderId") ? null : bizParamMap.get("refundOrderId").toString();
        if (ObjectValidUtil.isInvalid(refundOrderId)) {
            _log.warn("根据退款订单号查询退款订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        RefundOrder refundOrder = super.baseSelectRefundOrder(refundOrderId);
        if(refundOrder == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(refundOrder);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }

    @Override
    public Map selectByMchIdAndRefundOrderId(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("根据商户号和退款订单号查询退款订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String mchId = baseParam.isNullValue("mchId") ? null : bizParamMap.get("mchId").toString();
        String refundOrderId = baseParam.isNullValue("refundOrderId") ? null : bizParamMap.get("refundOrderId").toString();
        if (ObjectValidUtil.isInvalid(mchId, refundOrderId)) {
            _log.warn("根据商户号和退款订单号查询退款订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        RefundOrder refundOrder = super.baseSelectByMchIdAndRefundOrderId(mchId, refundOrderId);
        if(refundOrder == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(refundOrder);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }

    @Override
    public Map selectByMchIdAndMchRefundNo(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("根据商户号和商户订单号查询支付订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String mchId = baseParam.isNullValue("mchId") ? null : bizParamMap.get("mchId").toString();
        String mchRefundNo = baseParam.isNullValue("mchRefundNo") ? null : bizParamMap.get("mchRefundNo").toString();
        if (ObjectValidUtil.isInvalid(mchId, mchRefundNo)) {
            _log.warn("根据商户号和商户订单号查询支付订单失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        RefundOrder refundOrder = super.baseSelectByMchIdAndMchRefundNo(mchId, mchRefundNo);
        if(refundOrder == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(refundOrder);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }

    @Override
    public Map updateStatus4Ing(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("修改退款订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String refundOrderId = baseParam.isNullValue("refundOrderId") ? null : bizParamMap.get("refundOrderId").toString();
        String channelOrderNo = baseParam.isNullValue("channelOrderNo") ? null : bizParamMap.get("channelOrderNo").toString();
        if (ObjectValidUtil.isInvalid(refundOrderId)) {
            _log.warn("修改退款订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result =  super.baseUpdateStatus4Ing(refundOrderId, channelOrderNo);
        return RpcUtil.createBizResult(baseParam, result);
    }

    @Override
    public Map updateStatus4Success(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("修改退款订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String refundOrderId = baseParam.isNullValue("refundOrderId") ? null : bizParamMap.get("refundOrderId").toString();
        String channelOrderNo = baseParam.isNullValue("channelOrderNo") ? null : bizParamMap.get("channelOrderNo").toString();
        String channelPayOrderNo = baseParam.isNullValue("channelPayOrderNo") ? null : bizParamMap.get("channelPayOrderNo").toString();
        if (ObjectValidUtil.isInvalid(refundOrderId)) {
            _log.warn("修改退款订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result =  super.baseUpdateStatus4Success(refundOrderId,channelOrderNo,channelPayOrderNo);
        return RpcUtil.createBizResult(baseParam, result);
    }
    
    @Override
	public Map updateStatus4Fail(String jsonParam) {
    	 BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
         Map<String, Object> bizParamMap = baseParam.getBizParamMap();
         if (ObjectValidUtil.isInvalid(bizParamMap)) {
             _log.warn("修改退款订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
             return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
         }
         String refundOrderId = baseParam.isNullValue("refundOrderId") ? null : bizParamMap.get("refundOrderId").toString();
         String channelErrCode = baseParam.isNullValue("channelErrCode") ? null : bizParamMap.get("channelErrCode").toString();
         String channelErrMsg = baseParam.isNullValue("channelErrMsg") ? null : bizParamMap.get("channelErrMsg").toString();
         if (ObjectValidUtil.isInvalid(refundOrderId)) {
             _log.warn("修改退款订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
             return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
         }
         int result =  super.baseUpdateStatus4Fail(refundOrderId, channelErrCode, channelErrMsg);
         return RpcUtil.createBizResult(baseParam, result);
	}
    
    @Override
    public Map updateStatus4Complete(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("修改退款订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String refundOrderId = baseParam.isNullValue("refundOrderId") ? null : bizParamMap.get("refundOrderId").toString();
        if (ObjectValidUtil.isInvalid(refundOrderId)) {
            _log.warn("修改退款订单状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        int result =  super.baseUpdateStatus4Complete(refundOrderId);
        return RpcUtil.createBizResult(baseParam, result);
    }

 

}
