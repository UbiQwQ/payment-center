package cn.wuyi.payment.rpcimpl;

import cn.wuyi.payment.service.impl.BaseService;
import com.alibaba.fastjson.JSONObject;
import com.matrix.common.domain.BaseParam;
import com.matrix.common.enumm.RetEnum;
import com.matrix.common.util.*;
import com.matrix.dao.model.MchInfo;
import com.matrix.rpc.IMchInfoRpcService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("mchInfoRpcService")
public class MchInfoRpcServiceImpl extends BaseService implements IMchInfoRpcService {

    private static final MyLog _log = MyLog.getLog(MchInfoRpcServiceImpl.class);

    @Override
    public Map selectMchInfo(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("查询商户信息失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String mchId = baseParam.isNullValue("mchId") ? null : bizParamMap.get("mchId").toString();
        if (ObjectValidUtil.isInvalid(mchId)) {
            _log.warn("查询商户信息失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        MchInfo mchInfo = super.baseSelectMchInfo(mchId);
        if(mchInfo == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        String jsonResult = JsonUtil.object2Json(mchInfo);
        return RpcUtil.createBizResult(baseParam, jsonResult);
    }

	/** @description: 创建商户信息
	 *
	 * @param jsonParam
	 * @return 
	 * @author wanghao
	 * @date 2019年8月27日 下午6:30:57 
	 * @version 1.0.0.1
	 */
	@Override
	public Map createMchInfo(String jsonParam) {
		BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
		Map<String, Object> bizParamMap = baseParam.getBizParamMap();
		if (ObjectValidUtil.isInvalid(bizParamMap)) {
			_log.warn("新增商户信息失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
			return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
		}
		JSONObject mchInfoObj = baseParam.isNullValue("mchInfo") ? null
				: JSONObject.parseObject(bizParamMap.get("mchInfo").toString());
		if (mchInfoObj == null) {
			_log.warn("新增商户信息失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
			return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
		}
		MchInfo mchInfo = BeanConvertUtils.map2Bean(mchInfoObj, MchInfo.class);
		if (mchInfoObj == null) {
			_log.warn("新增商户信息失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
			return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
		}
		int result = super.baseCreateMchInfo(mchInfo);
		return RpcUtil.createBizResult(baseParam, result);
	}
}
