package cn.wuyi.payment.rpcimpl;

import cn.wuyi.payment.service.impl.BaseService;
import com.alibaba.fastjson.JSONObject;
import com.matrix.common.domain.BaseParam;
import com.matrix.common.enumm.RetEnum;
import com.matrix.common.util.*;
import com.matrix.dao.model.PayChannel;
import com.matrix.rpc.IPayChannelRpcService;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("payChannelRpcService")
public class PayChannelRpcServiceImpl extends BaseService implements IPayChannelRpcService {

    private static final MyLog _log = MyLog.getLog(PayChannelRpcServiceImpl.class);

    @Override
    public Map selectPayChannel(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("查询支付渠道信息失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String mchId = baseParam.isNullValue("mchId") ? null : bizParamMap.get("mchId").toString();
        String channelId = baseParam.isNullValue("channelId") ? null : bizParamMap.get("channelId").toString();
        if (ObjectValidUtil.isInvalid(mchId, channelId)) {
            _log.warn("查询支付渠道信息失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        PayChannel payChannel = super.baseSelectPayChannel(mchId, channelId);
        if(payChannel == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(payChannel);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }

	/** @description: 创建支付渠道
	 *
	 * @param jsonParam
	 * @return 
	 * @author wanghao
	 * @date 2019年6月27日 下午3:21:40 
	 * @version 1.0.0.1
	 */
	@Override
	public Map createPayChannel(String jsonParam) {
		BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
		Map<String, Object> bizParamMap = baseParam.getBizParamMap();
		if (ObjectValidUtil.isInvalid(bizParamMap)) {
			_log.warn("新增支付渠道失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
			return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
		}
		JSONObject payChannelObj = baseParam.isNullValue("payChannel") ? null: JSONObject.parseObject(bizParamMap.get("payChannel").toString());
		if (payChannelObj == null) {
			_log.warn("新增支付渠道失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
			return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
		}
		PayChannel payChannel = BeanConvertUtils.map2Bean(payChannelObj, PayChannel.class);
		if (payChannel == null) {
			_log.warn("新增支付渠道失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
			return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
		}
		int result = super.baseCreatePayChannel(payChannel);
	    return RpcUtil.createBizResult(baseParam, result);
	}
	
	/** @description: 修改支付渠道
	 *
	 * @param jsonParam
	 * @return 
	 * @author wanghao
	 * @date 2019年6月27日 下午3:21:40 
	 * @version 1.0.0.1
	 */
	@Override
	public Map updatePayChannel(String jsonParam) {
		BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
		Map<String, Object> bizParamMap = baseParam.getBizParamMap();
		if (ObjectValidUtil.isInvalid(bizParamMap)) {
			_log.warn("修改支付渠道失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
			return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
		}
		JSONObject payChannelObj = baseParam.isNullValue("payChannel") ? null: JSONObject.parseObject(bizParamMap.get("payChannel").toString());
		if (payChannelObj == null) {
			_log.warn("修改支付渠道失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
			return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
		}
		PayChannel payChannel = BeanConvertUtils.map2Bean(payChannelObj, PayChannel.class);
		if (payChannel == null) {
			_log.warn("修改支付渠道失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
			return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
		}
		int result = super.baseupdatePayChannel(payChannel);
	    return RpcUtil.createBizResult(baseParam, result);
	}
}
