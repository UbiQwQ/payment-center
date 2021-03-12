package cn.wuyi.payment.service.impl;

import com.matrix.common.constant.PayConstant;
import com.matrix.dao.mapper.PayOrderMapper;
import com.matrix.dao.model.PayOrder;
import com.matrix.dao.model.PayOrderExample;
import com.matrix.monitor.aspectj.CatTransaction;
import com.matrix.monitor.cat.CatMonitor;
import com.matrix.monitor.cat.ICommon;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;


@Service
public class BaseService4PayOrder extends BaseService{
    private static final Logger logger= LoggerFactory.getLogger(BaseService4PayOrder.class);
    @Autowired
    private PayOrderMapper payOrderMapper;

    @Autowired
    @Qualifier("CatMonitor")
    private CatMonitor catMonitor;
    
    @Transactional
    @CatTransaction(type = "SERVICE", name = "BaseService4PayOrder.baseCreatePayOrder")
    public int baseCreatePayOrder(PayOrder payOrder) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4PayOrder.baseCreatePayOrder");
        try {
            //创建支付单,删除原没有支付成功订单,插入新订单
            /**PayOrder dbPayOrder = baseSelectByMchIdAndMchOrderNo(payOrder.getMchId(),payOrder.getMchOrderNo());
            if(dbPayOrder != null) {
                if(dbPayOrder.getStatus() == PayConstant.PAY_STATUS_SUCCESS || dbPayOrder.getStatus() == PayConstant.PAY_STATUS_COMPLETE) {
                    return 0;
                }else {
                    if(new Date().getTime() - dbPayOrder.getCreateTime().getTime() < 2000) {
                        //防止重复提交,两秒内同一个订单不可重复调用
                        return 0;
                    }
                    PayOrderExample example = new PayOrderExample();
                    PayOrderExample.Criteria criteria = example.createCriteria();
                    criteria.andMchIdEqualTo(payOrder.getMchId());
                    criteria.andMchOrderNoEqualTo(payOrder.getMchOrderNo());
                    payOrderMapper.deleteByExample(example);
                }
            }**/
            int i = payOrderMapper.insertSelective(payOrder);
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

    @CatTransaction(type = "SERVICE", name = "BaseService4PayOrder.baseSelectPayOrder")
    public PayOrder baseSelectPayOrder(String payOrderId) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4PayOrder.baseSelectPayOrder");
        try {
            PayOrder payOrder = payOrderMapper.selectByPrimaryKey(payOrderId);
            t.success();
            return payOrder;
        } catch (Exception e) {
            logger.error("",e);
            t.error(e);
            throw e;
        } finally {
            t.end();
        }
    }

    @CatTransaction(type = "SERVICE", name = "BaseService4PayOrder.baseSelectByMchIdAndPayOrderId")
    public PayOrder baseSelectByMchIdAndPayOrderId(String mchId, String payOrderId) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4PayOrder.baseSelectByMchIdAndPayOrderId");
        try {
            PayOrderExample example = new PayOrderExample();
            PayOrderExample.Criteria criteria = example.createCriteria();
            criteria.andMchIdEqualTo(mchId);
            criteria.andPayOrderIdEqualTo(payOrderId);
            List<PayOrder> payOrderList = payOrderMapper.selectByExample(example);
            t.success();
            return CollectionUtils.isEmpty(payOrderList) ? null : payOrderList.get(0);
        } catch (Exception e) {
            logger.error("",e);
            t.error(e);
            throw e;
        } finally {
            t.end();
        }
    }


    @CatTransaction(type = "SERVICE", name = "BaseService4PayOrder.baseSelectByMchIdAndMchOrderNo")
    public PayOrder baseSelectByMchIdAndMchOrderNo(String mchId, String mchOrderNo) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4PayOrder.baseSelectByMchIdAndMchOrderNo");
        try {
            PayOrderExample example = new PayOrderExample();
            PayOrderExample.Criteria criteria = example.createCriteria();
            criteria.andMchIdEqualTo(mchId);
            criteria.andMchOrderNoEqualTo(mchOrderNo);
            List<PayOrder> payOrderList = payOrderMapper.selectByExample(example);
            t.success();
            return CollectionUtils.isEmpty(payOrderList) ? null : payOrderList.get(0);
        } catch (Exception e) {
            logger.error("",e);
            t.error(e);
            throw e;
        } finally {
            t.end();
        }
    }

    @CatTransaction(type = "SERVICE", name = "BaseService4PayOrder.selectByMchIdAndMchOrderNoAndChannelOrderNo")
    public PayOrder selectByMchIdAndMchOrderNoAndChannelOrderNo(String mchId, String mchOrderNo,String channelOrderNo) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4PayOrder.selectByMchIdAndMchOrderNoAndChannelOrderNo");
        try {
            PayOrderExample example = new PayOrderExample();
            PayOrderExample.Criteria criteria = example.createCriteria();
            criteria.andMchIdEqualTo(mchId);
            criteria.andMchOrderNoEqualTo(mchOrderNo);
            criteria.andChannelOrderNoEqualTo(channelOrderNo);
            List<PayOrder> payOrderList = payOrderMapper.selectByExample(example);
            t.success();
            return CollectionUtils.isEmpty(payOrderList) ? null : payOrderList.get(0);
        } catch (Exception e) {
            logger.error("",e);
            t.error(e);
            throw e;
        } finally {
            t.end();
        }
    }


    @CatTransaction(type = "SERVICE", name = "BaseService4PayOrder.baseUpdateStatus4Ing")
    public int baseUpdateStatus4Ing(String payOrderId, String channelOrderNo) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4PayOrder.baseUpdateStatus4Ing");
        try {
            PayOrder payOrder = new PayOrder();
            payOrder.setStatus(PayConstant.PAY_STATUS_PAYING);
            if(channelOrderNo != null) payOrder.setChannelOrderNo(channelOrderNo);
            //payOrder.setPaySuccTime(System.currentTimeMillis());
            PayOrderExample example = new PayOrderExample();
            PayOrderExample.Criteria criteria = example.createCriteria();
            criteria.andPayOrderIdEqualTo(payOrderId);
            criteria.andStatusEqualTo(PayConstant.PAY_STATUS_INIT);
            int i = payOrderMapper.updateByExampleSelective(payOrder, example);
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

    @CatTransaction(type = "SERVICE", name = "BaseService4PayOrder.baseUpdateStatus4Success")
    public int baseUpdateStatus4Success(String payOrderId) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4PayOrder.baseUpdateStatus4Success");
        try {
            int i = baseUpdateStatus4Success(payOrderId, null);
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

    public int baseUpdateStatus4Success(String payOrderId, String channelOrderNo) {
        PayOrder payOrder = new PayOrder();
        payOrder.setPayOrderId(payOrderId);
        payOrder.setStatus(PayConstant.PAY_STATUS_SUCCESS);
        payOrder.setPaySuccTime(System.currentTimeMillis());
        if(StringUtils.isNotBlank(channelOrderNo)) payOrder.setChannelOrderNo(channelOrderNo);
        PayOrderExample example = new PayOrderExample();
        PayOrderExample.Criteria criteria = example.createCriteria();
        criteria.andPayOrderIdEqualTo(payOrderId);
        criteria.andStatusEqualTo(PayConstant.PAY_STATUS_PAYING);
        return payOrderMapper.updateByExampleSelective(payOrder, example);
    }

    @CatTransaction(type = "SERVICE", name = "BaseService4PayOrder.baseUpdateStatus4Complete")
    public int baseUpdateStatus4Complete(String payOrderId) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4PayOrder.baseUpdateStatus4Complete");
        try {
            PayOrder payOrder = new PayOrder();
            payOrder.setPayOrderId(payOrderId);
            payOrder.setStatus(PayConstant.PAY_STATUS_COMPLETE);
            PayOrderExample example = new PayOrderExample();
            PayOrderExample.Criteria criteria = example.createCriteria();
            criteria.andPayOrderIdEqualTo(payOrderId);
            criteria.andStatusEqualTo(PayConstant.PAY_STATUS_SUCCESS);
            int i = payOrderMapper.updateByExampleSelective(payOrder, example);
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

    @CatTransaction(type = "SERVICE", name = "BaseService4PayOrder.baseUpdateNotify")
    public int baseUpdateNotify(String payOrderId, byte count) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4PayOrder.baseUpdateNotify");
        try {
            PayOrder newPayOrder = new PayOrder();
            newPayOrder.setNotifyCount(count);
            newPayOrder.setLastNotifyTime(System.currentTimeMillis());
            newPayOrder.setPayOrderId(payOrderId);
            int i = payOrderMapper.updateByPrimaryKeySelective(newPayOrder);
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
}
