package cn.wuyi.payment.load;

import com.alibaba.fastjson.JSONObject;
import com.matrix.annotation.Inject;
import com.matrix.base.BaseClass;
import com.matrix.base.interfaces.ILoadCache;
import com.matrix.cache.CacheLaunch;
import com.matrix.cache.enums.DCacheEnum;
import com.matrix.cache.inf.IBaseLaunch;
import com.matrix.cache.inf.ICacheFactory;
import com.matrix.dao.mapper.MchInfoMapper;
import com.matrix.dao.model.MchInfo;

/** @description: 商户信息缓存
 *
 * @author wanghao
 * @date 2019年6月26日 下午5:35:35 
 * @version 1.0.0.1
 */
public class InitMchInfo extends BaseClass implements ILoadCache<String>{
	private IBaseLaunch<ICacheFactory> launch = CacheLaunch.getInstance().Launch();
	@Inject
	private MchInfoMapper mchInfoMapper;
	
	@Override
	public String load(String key, String field) {
	    MchInfo mchInfo = mchInfoMapper.selectByPrimaryKey(key);
	    if(mchInfo == null) return "";
	    String value = JSONObject.toJSONString(mchInfo);
		launch.loadDictCache(DCacheEnum.PaymentMchInfo, null).set(key , value , 24*60*60);
		return value;
	}

}
