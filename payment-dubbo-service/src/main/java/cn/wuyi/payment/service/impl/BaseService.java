package cn.wuyi.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.matrix.base.BaseClass;
import com.matrix.cache.CacheLaunch;
import com.matrix.cache.enums.DCacheEnum;
import com.matrix.cache.inf.IBaseLaunch;
import com.matrix.cache.inf.ICacheFactory;
import com.matrix.common.constant.PayConstant;
import com.matrix.dao.mapper.MchInfoMapper;
import com.matrix.dao.mapper.MchNotifyMapper;
import com.matrix.dao.mapper.PayChannelMapper;
import com.matrix.dao.model.MchInfo;
import com.matrix.dao.model.MchNotify;
import com.matrix.dao.model.MchNotifyExample;
import com.matrix.dao.model.PayChannel;
import com.matrix.monitor.aspectj.CatTransaction;
import com.matrix.monitor.cat.CatMonitor;
import com.matrix.monitor.cat.ICommon;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;


@Service
public class BaseService extends BaseClass{
    private static final Logger logger= LoggerFactory.getLogger(BaseService.class);
    //缓存
    private IBaseLaunch<ICacheFactory> launch = CacheLaunch.getInstance().Launch();
    
    @Autowired
    private MchNotifyMapper mchNotifyMapper;
    
    @Autowired
    private PayChannelMapper payChannelMapper;
    
    @Autowired
	private MchInfoMapper mchInfoMapper;

    @Autowired
    @Qualifier("CatMonitor")
    private CatMonitor catMonitor;

    @CatTransaction(type = "SERVICE", name = "BaseService.baseSelectMchInfo")
    public MchInfo baseSelectMchInfo(String mchId) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService.baseSelectMchInfo");
        try {
            String cacheResult = launch.loadDictCache(DCacheEnum.PaymentMchInfo, "InitMchInfo").get(mchId);
            if(StringUtils.isBlank(cacheResult)) {
                t.success();
                 return null;
            }
            t.success();
            return JSONObject.parseObject(cacheResult, MchInfo.class);
        } catch (Exception e) {
            logger.error("",e);
            t.error(e);
            throw e;
        } finally {
            t.end();
        }
        //return mchInfoMapper.selectByPrimaryKey(mchId);
    }

    @CatTransaction(type = "SERVICE", name = "BaseService.baseSelectPayChannel")
    public PayChannel baseSelectPayChannel(String mchId, String channelId) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService.baseSelectPayChannel");
        try {
            String cacheResult = launch.loadDictCache(DCacheEnum.PaymentChannel, "InitPayChannel").get(mchId+"-"+channelId);
            if(StringUtils.isBlank(cacheResult)) {
                t.success();
                 return null;
            }
            t.success();
            return JSONObject.parseObject(cacheResult, PayChannel.class);
        } catch (Exception e) {
            logger.error("",e);
            t.error(e);
            throw e;
        } finally {
            t.end();
        }
//    	PayChannelExample example = new PayChannelExample();
//		PayChannelExample.Criteria criteria = example.createCriteria();
//		criteria.andChannelIdEqualTo(channelId);
//		criteria.andMchIdEqualTo(mchId);
//		List<PayChannel> payChannelList = payChannelMapper.selectByExample(example);
//		if(payChannelList.size()>0) {
//			return payChannelList.get(0);
//		}else {
//			return null;
//		}
		
    }


    @CatTransaction(type = "SERVICE", name = "BaseService.baseSelectMchNotify")
    public MchNotify baseSelectMchNotify(String orderId) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService.baseSelectMchNotify");
        try {
            MchNotify mchNotify = mchNotifyMapper.selectByPrimaryKey(orderId);
            t.success();
            return mchNotify;
        } catch (Exception e) {
            logger.error("",e);
            t.error(e);
            throw e;
        } finally {
            t.end();
        }
    }

    @CatTransaction(type = "SERVICE", name = "BaseService.baseInsertMchNotify")
    public int baseInsertMchNotify(String orderId, String mchId, String mchOrderNo, String orderType, String notifyUrl,String notifyContent) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService.baseInsertMchNotify");
        try {
            MchNotify mchNotify = new MchNotify();
            mchNotify.setOrderId(orderId);
            mchNotify.setMchId(mchId);
            mchNotify.setMchOrderNo(mchOrderNo);
            mchNotify.setOrderType(orderType);
            mchNotify.setNotifyUrl(notifyUrl);
            mchNotify.setNotifyContent(notifyContent);
            int i = mchNotifyMapper.insertSelectiveOnDuplicateKeyUpdate(mchNotify);
            t.success();
            return i;
        } catch (Exception e) {
            logger.error("",e);
            t.error(e);
            throw e;
        } finally {
            t.end();
        }
    }

    @CatTransaction(type = "SERVICE", name = "BaseService.baseUpdateMchNotifySuccess")
    public int baseUpdateMchNotifySuccess(String orderId, String result, byte notifyCount) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService.baseUpdateMchNotifySuccess");
        try {
            MchNotify mchNotify = new MchNotify();
            mchNotify.setStatus(PayConstant.MCH_NOTIFY_STATUS_SUCCESS);
            mchNotify.setResult(result);
            mchNotify.setNotifyCount(notifyCount);
            mchNotify.setLastNotifyTime(new Date());
            MchNotifyExample example = new MchNotifyExample();
            MchNotifyExample.Criteria criteria = example.createCriteria();
            criteria.andOrderIdEqualTo(orderId);
            List values = new LinkedList<>();
            values.add(PayConstant.MCH_NOTIFY_STATUS_NOTIFYING);
            values.add(PayConstant.MCH_NOTIFY_STATUS_FAIL);
            criteria.andStatusIn(values);
            int i = mchNotifyMapper.updateByExampleSelective(mchNotify, example);
            t.success();
            return i;
        } catch (Exception e) {
            logger.error("",e);
            t.error(e);
            throw e;
        } finally {
            t.end();
        }
    }


    @CatTransaction(type = "SERVICE", name = "BaseService.baseUpdateMchNotifyFail")
    public int baseUpdateMchNotifyFail(String orderId, String result, byte notifyCount) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService.baseUpdateMchNotifyFail");
        try {
            MchNotify mchNotify = new MchNotify();
            mchNotify.setStatus(PayConstant.MCH_NOTIFY_STATUS_FAIL);
            mchNotify.setResult(result);
            mchNotify.setNotifyCount(notifyCount);
            mchNotify.setLastNotifyTime(new Date());
            MchNotifyExample example = new MchNotifyExample();
            MchNotifyExample.Criteria criteria = example.createCriteria();
            criteria.andOrderIdEqualTo(orderId);
            List values = new LinkedList<>();
            values.add(PayConstant.MCH_NOTIFY_STATUS_NOTIFYING);
            values.add(PayConstant.MCH_NOTIFY_STATUS_FAIL);
            int i = mchNotifyMapper.updateByExampleSelective(mchNotify, example);
            t.success();
            return i;
        } catch (Exception e) {
            logger.error("",e);
            t.error(e);
            throw e;
        } finally {
            t.end();
        }
    }

    @CatTransaction(type = "SERVICE", name = "BaseService.baseCreatePayChannel")
    public int baseCreatePayChannel(PayChannel payChannel) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService.baseCreatePayChannel");
        try {
            int i = payChannelMapper.insertSelective(payChannel);
            t.success();
            return i;
        } catch (Exception e) {
            logger.error("",e);
            t.error(e);
            throw e;
        } finally {
            t.end();
        }
    }

    @CatTransaction(type = "SERVICE", name = "BaseService.baseCreateMchInfo")
    public int baseCreateMchInfo(MchInfo mchInfo) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService.baseCreateMchInfo");
        try {
            int i = mchInfoMapper.insertSelective(mchInfo);
            t.success();
            return i;
        } catch (Exception e) {
            logger.error("",e);
            t.error(e);
            throw e;
        } finally {
            t.end();
        }
    }

    @CatTransaction(type = "SERVICE", name = "BaseService.baseupdatePayChannel")
    public int baseupdatePayChannel(PayChannel payChannel) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService.baseupdatePayChannel");
        try {
            int result = payChannelMapper.updateByPrimaryKeySelective(payChannel);
//    	PayChannelExample example = new PayChannelExample();
//    	PayChannelExample.Criteria criteria =  example.createCriteria();
//    	criteria.andMchIdEqualTo(payChannel.getMchId());
//    	criteria.andChannelNameEqualTo(payChannel.getChannelName());
//        int result = payChannelMapper.updateByExampleSelective(payChannel, example);
//        if(result > 0) {
//        	launch.loadDictCache(DCacheEnum.PaymentChannel, "InitPayChannel").del(payChannel.getMchId()+"-"+payChannel.getChannelName());
//        }
            launch.loadDictCache(DCacheEnum.PaymentChannel, null).del(payChannel.getMchId()+"-"+payChannel.getChannelId());
            t.success();
            return result;
        } catch (Exception e) {
            logger.error("",e);
            t.error(e);
            throw e;
        } finally {
            t.end();
        }
    }
}
