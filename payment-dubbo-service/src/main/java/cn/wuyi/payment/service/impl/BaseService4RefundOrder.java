package cn.wuyi.payment.service.impl;

import com.matrix.common.constant.PayConstant;
import com.matrix.dao.mapper.RefundOrderMapper;
import com.matrix.dao.model.RefundOrder;
import com.matrix.dao.model.RefundOrderExample;
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

import java.util.Date;
import java.util.List;


@Service
public class BaseService4RefundOrder extends BaseService{
    private static final Logger logger= LoggerFactory.getLogger(BaseService4RefundOrder.class);
    @Autowired
    private RefundOrderMapper refundOrderMapper;

    @Autowired
    @Qualifier("CatMonitor")
    private CatMonitor catMonitor;

    @Transactional
    @CatTransaction(type = "SERVICE", name = "BaseService4RefundOrder.baseCreateRefundOrder")
    public int baseCreateRefundOrder(RefundOrder refundOrder) {
    	//重新生存订单
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4RefundOrder.baseCreateRefundOrder");
        try {
            RefundOrder dbRefundOrder = baseSelectByMchIdAndMchRefundNo(refundOrder.getMchId(), refundOrder.getMchRefundNo());
            if(dbRefundOrder != null) {
                if (dbRefundOrder.getStatus() == PayConstant.REFUND_RESULT_SUCCESS || dbRefundOrder.getStatus() == PayConstant.REFUND_STATUS_COMPLETE) {
                    t.success();
                    return 0;
                } else {
                    if (new Date().getTime() - dbRefundOrder.getCreateTime().getTime() < 2000) {
                        // 防止重复提交,两秒内同一个订单不可重复调用
                        t.success();
                        return 0;
                    }
                    RefundOrderExample example = new RefundOrderExample();
                    RefundOrderExample.Criteria criteria = example.createCriteria();
                    criteria.andMchIdEqualTo(refundOrder.getMchId());
                    criteria.andMchRefundNoEqualTo(refundOrder.getMchRefundNo());
                    refundOrderMapper.deleteByExample(example);
                }
            }
            int i = refundOrderMapper.insertSelective(refundOrder);
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

    @CatTransaction(type = "SERVICE", name = "BaseService4RefundOrder.baseSelectRefundOrder")
    public RefundOrder baseSelectRefundOrder(String refundOrderId) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4RefundOrder.baseSelectRefundOrder");
        try {
            RefundOrder refundOrder = refundOrderMapper.selectByPrimaryKey(refundOrderId);
            t.success();
            return refundOrder;
        } catch (Exception e) {
            logger.error("",e);
            t.error(e);
            throw e;
        } finally {
            t.end();
        }
    }

    @CatTransaction(type = "SERVICE", name = "BaseService4RefundOrder.baseSelectByMchIdAndRefundOrderId")
    public RefundOrder baseSelectByMchIdAndRefundOrderId(String mchId, String refundOrderId) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4RefundOrder.baseSelectByMchIdAndRefundOrderId");
        try {
            RefundOrderExample example = new RefundOrderExample();
            RefundOrderExample.Criteria criteria = example.createCriteria();
            criteria.andMchIdEqualTo(mchId);
            criteria.andRefundOrderIdEqualTo(refundOrderId);
            List<RefundOrder> refundOrderList = refundOrderMapper.selectByExample(example);
            t.success();
            return CollectionUtils.isEmpty(refundOrderList) ? null : refundOrderList.get(0);
        } catch (Exception e) {
            logger.error("",e);
            t.error(e);
            throw e;
        } finally {
            t.end();
        }
    }

    @CatTransaction(type = "SERVICE", name = "BaseService4RefundOrder.baseSelectByMchIdAndMchRefundNo")
    public RefundOrder baseSelectByMchIdAndMchRefundNo(String mchId, String mchRefundNo) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4RefundOrder.baseSelectByMchIdAndMchRefundNo");
        try {
            RefundOrderExample example = new RefundOrderExample();
            RefundOrderExample.Criteria criteria = example.createCriteria();
            criteria.andMchIdEqualTo(mchId);
            criteria.andMchRefundNoEqualTo(mchRefundNo);
            List<RefundOrder> refundOrderList = refundOrderMapper.selectByExample(example);
            t.success();
            return CollectionUtils.isEmpty(refundOrderList) ? null : refundOrderList.get(0);
        } catch (Exception e) {
            logger.error("",e);
            t.error(e);
            throw e;
        } finally {
            t.end();
        }
    }

    @CatTransaction(type = "SERVICE", name = "BaseService4RefundOrder.baseUpdateStatus4Ing")
    public int baseUpdateStatus4Ing(String refundOrderId, String channelOrderNo) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4RefundOrder.baseUpdateStatus4Ing");
        try {
            RefundOrder refundOrder = new RefundOrder();
            refundOrder.setStatus(PayConstant.REFUND_STATUS_REFUNDING);
            if(channelOrderNo != null) refundOrder.setChannelOrderNo(channelOrderNo);
            refundOrder.setRefundSuccTime(new Date());
            RefundOrderExample example = new RefundOrderExample();
            RefundOrderExample.Criteria criteria = example.createCriteria();
            criteria.andRefundOrderIdEqualTo(refundOrderId);
            criteria.andStatusEqualTo(PayConstant.REFUND_STATUS_INIT);
            int i = refundOrderMapper.updateByExampleSelective(refundOrder, example);
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


    @CatTransaction(type = "SERVICE", name = "BaseService4RefundOrder.baseUpdateStatus4Success")
    public int baseUpdateStatus4Success(String refundOrderId ) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4RefundOrder.baseUpdateStatus4Success");
        try {
            int i = baseUpdateStatus4Success(refundOrderId, null);
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

    public int baseUpdateStatus4Success(String refundOrderId, String channelOrderNo) {
        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setRefundOrderId(refundOrderId);
        refundOrder.setStatus(PayConstant.REFUND_STATUS_SUCCESS);
        refundOrder.setResult(PayConstant.REFUND_RESULT_SUCCESS);
        refundOrder.setRefundSuccTime(new Date());
        if(StringUtils.isNotBlank(channelOrderNo)) refundOrder.setChannelOrderNo(channelOrderNo);
        RefundOrderExample example = new RefundOrderExample();
        RefundOrderExample.Criteria criteria = example.createCriteria();
        criteria.andRefundOrderIdEqualTo(refundOrderId);
        criteria.andStatusEqualTo(PayConstant.REFUND_STATUS_REFUNDING);
        return refundOrderMapper.updateByExampleSelective(refundOrder, example);
    }

    public int baseUpdateStatus4Success(String refundOrderId, String channelOrderNo,String channelPayOrderNo) {
        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setRefundOrderId(refundOrderId);
        refundOrder.setChannelOrderNo(channelOrderNo);
        refundOrder.setChannelPayOrderNo(channelPayOrderNo);
        refundOrder.setStatus(PayConstant.REFUND_STATUS_SUCCESS);
        refundOrder.setResult(PayConstant.REFUND_RESULT_SUCCESS);
        refundOrder.setRefundSuccTime(new Date());
        RefundOrderExample example = new RefundOrderExample();
        RefundOrderExample.Criteria criteria = example.createCriteria();
        criteria.andRefundOrderIdEqualTo(refundOrderId);
        criteria.andStatusEqualTo(PayConstant.REFUND_STATUS_REFUNDING);
        return refundOrderMapper.updateByExampleSelective(refundOrder, example);
    }

    public int baseUpdateStatus4Complete(String refundOrderId) {
        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setRefundOrderId(refundOrderId);
        refundOrder.setStatus(PayConstant.REFUND_STATUS_COMPLETE);
        RefundOrderExample example = new RefundOrderExample();
        RefundOrderExample.Criteria criteria = example.createCriteria();
        criteria.andRefundOrderIdEqualTo(refundOrderId);
        List values = CollectionUtils.arrayToList(new Byte[] {
                PayConstant.REFUND_STATUS_SUCCESS, PayConstant.REFUND_STATUS_FAIL
        });
        criteria.andStatusIn(values);
        return refundOrderMapper.updateByExampleSelective(refundOrder, example);
    }

    public int baseUpdateStatus4Fail(String refundOrderId, String channelErrCode, String channelErrMsg) {
        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setStatus(PayConstant.REFUND_STATUS_FAIL);
        refundOrder.setResult(PayConstant.REFUND_RESULT_FAIL);
        if(channelErrCode != null) refundOrder.setChannelErrCode(channelErrCode);
        if(channelErrMsg != null) refundOrder.setChannelErrMsg(channelErrMsg);
        RefundOrderExample example = new RefundOrderExample();
        RefundOrderExample.Criteria criteria = example.createCriteria();
        criteria.andRefundOrderIdEqualTo(refundOrderId);
        criteria.andStatusEqualTo(PayConstant.REFUND_STATUS_REFUNDING);
        return refundOrderMapper.updateByExampleSelective(refundOrder, example);
    }

}
