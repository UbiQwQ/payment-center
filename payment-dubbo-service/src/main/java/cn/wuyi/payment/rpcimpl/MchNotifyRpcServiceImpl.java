package cn.wuyi.payment.rpcimpl;

import cn.wuyi.payment.service.impl.BaseNotify4MchPay;
import com.alibaba.fastjson.JSONObject;
import com.matrix.common.constant.PayConstant;
import com.matrix.common.domain.BaseParam;
import com.matrix.common.enumm.RetEnum;
import com.matrix.common.util.*;
import com.matrix.dao.model.MchNotify;
import com.matrix.rpc.IMchNotifyRpcService;
import org.springframework.stereotype.Service;

import java.util.Map;

/** @description: 商户通知rpc接口实现类
 *
 * @author wanghao
 * @date 2019年8月30日 下午2:10:14 
 * @version 1.0.0.1
 */
@Service("mchNotifyRpcService")
public class MchNotifyRpcServiceImpl extends BaseNotify4MchPay implements IMchNotifyRpcService{
    private static final MyLog myLog = MyLog.getLog(MchNotifyRpcServiceImpl.class);

	/** @description: 插入商户通知
	 *
	 * @param jsonParam
	 * @return 
	 * @author wanghao
	 * @date 2019年8月30日 下午2:11:28 
	 * @version 1.0.0.1
	 */
	@Override
	public Map createMchNotify(String jsonParam) {
		BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
		Map<String, Object> bizParamMap = baseParam.getBizParamMap();
		if (ObjectValidUtil.isInvalid(bizParamMap)) {
			myLog.warn("新增商户通知信息失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
			return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
		}
		JSONObject mchNotifyObj = baseParam.isNullValue("mchNotifyObj") ? null
				: JSONObject.parseObject(bizParamMap.get("mchNotifyObj").toString());
		if (mchNotifyObj == null) {
			myLog.warn("新增商户通知信息失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
			return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
		}
		MchNotify mchNotify = BeanConvertUtils.map2Bean(mchNotifyObj, MchNotify.class);
		baseInsertMchNotify(mchNotify.getOrderId(), mchNotify.getMchId(),mchNotify.getMchOrderNo(), PayConstant.MCH_NOTIFY_TYPE_PAY,  mchNotify.getNotifyUrl(),mchNotify.getNotifyContent());
		return null;
	}

	/** @description: 修改通知状态为成功
	 *
	 * @param jsonParam
	 * @return 
	 * @author wanghao
	 * @date 2019年8月30日 下午3:19:42 
	 * @version 1.0.0.1
	 */
	@Override
	public Map updateMchNotifySuccess(String jsonParam) {
		BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
		Map<String, Object> bizParamMap = baseParam.getBizParamMap();
		if (ObjectValidUtil.isInvalid(bizParamMap)) {
			myLog.warn("修改商户通知信息状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
			return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
		}
	    String orderId = baseParam.isNullValue("orderId") ? null : bizParamMap.get("orderId").toString();
	    String result = baseParam.isNullValue("result") ? null : bizParamMap.get("result").toString();
	    if (orderId == null) {
			myLog.warn("修改商户通知信息状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
			return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
		}
		int resultNum = baseUpdateMchNotifySuccess(orderId, result, Byte.valueOf("1"));
		return RpcUtil.createBizResult(baseParam, resultNum);
	}

	/** @description: 修改通知状态为失败
	 *
	 * @param jsonParam
	 * @return 
	 * @author wanghao
	 * @date 2019年8月30日 下午3:21:30 
	 * @version 1.0.0.1
	 */
	@Override
	public Map updateMchNotifyFail(String jsonParam) {
		BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
		Map<String, Object> bizParamMap = baseParam.getBizParamMap();
		if (ObjectValidUtil.isInvalid(bizParamMap)) {
			myLog.warn("修改商户通知信息状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
			return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
		}
	    String orderId = baseParam.isNullValue("orderId") ? null : bizParamMap.get("orderId").toString();
	    String result = baseParam.isNullValue("result") ? null : bizParamMap.get("result").toString();
	    if (orderId == null) {
			myLog.warn("修改商户通知信息状态失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
			return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
		}
		int resultNum = baseUpdateMchNotifyFail(orderId, result, Byte.valueOf("1"));
		return RpcUtil.createBizResult(baseParam, resultNum);
	}

}
