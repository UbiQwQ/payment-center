package cn.wuyi.payment.load;

import com.alibaba.fastjson.JSONObject;
import com.matrix.annotation.Inject;
import com.matrix.base.BaseClass;
import com.matrix.base.interfaces.ILoadCache;
import com.matrix.cache.CacheLaunch;
import com.matrix.cache.enums.DCacheEnum;
import com.matrix.cache.inf.IBaseLaunch;
import com.matrix.cache.inf.ICacheFactory;
import com.matrix.dao.mapper.PayChannelMapper;
import com.matrix.dao.model.PayChannel;
import com.matrix.dao.model.PayChannelExample;
import org.springframework.util.CollectionUtils;

import java.util.List;

/** @description: 支付渠道信息缓存
 *
 * @author wanghao
 * @date 2019年6月26日 下午5:36:46 
 * @version 1.0.0.1
 */
public class InitPayChannel extends BaseClass implements ILoadCache<String>{
    private IBaseLaunch<ICacheFactory> launch = CacheLaunch.getInstance().Launch();
    @Inject
    private PayChannelMapper payChannelMapper;
	@Override
	public String load(String key, String field) {
		String mchId = key.split("-")[0];
		String channelId = key.split("-")[1];
		PayChannelExample example = new PayChannelExample();
		PayChannelExample.Criteria criteria = example.createCriteria();
		criteria.andChannelIdEqualTo(channelId);
		criteria.andMchIdEqualTo(mchId);
		List<PayChannel> payChannelList = payChannelMapper.selectByExample(example);
		if (CollectionUtils.isEmpty(payChannelList))return "";
		PayChannel payChannel = payChannelList.get(0);
		String value = JSONObject.toJSONString(payChannel);
		launch.loadDictCache(DCacheEnum.PaymentChannel, null).set(key , value , 24*60*60);
		return value;
	}
}
