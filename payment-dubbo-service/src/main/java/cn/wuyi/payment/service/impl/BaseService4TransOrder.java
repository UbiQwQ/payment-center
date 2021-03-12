package cn.wuyi.payment.service.impl;

import com.matrix.common.constant.PayConstant;
import com.matrix.dao.mapper.TransOrderMapper;
import com.matrix.dao.model.TransOrder;
import com.matrix.dao.model.TransOrderExample;
import com.matrix.monitor.aspectj.CatTransaction;
import com.matrix.monitor.cat.CatMonitor;
import com.matrix.monitor.cat.ICommon;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;


@Service
public class BaseService4TransOrder extends BaseService{

    private static final Logger logger= LoggerFactory.getLogger(BaseService4TransOrder.class);

    @Autowired
    private TransOrderMapper transOrderMapper;

    @Autowired
    @Qualifier("CatMonitor")
    private CatMonitor catMonitor;

    @CatTransaction(type = "SERVICE", name = "BaseService4TransOrder.baseCreateTransOrder")
    public int baseCreateTransOrder(TransOrder transOrder) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4TransOrder.baseCreateTransOrder");
        try {
            //如果同一个商户同一个订单号,则删除原订单,插入新订单
            TransOrder dbTransOrder = baseSelectByMchIdAndMchTransNo(transOrder.getMchId(),transOrder.getMchTransNo());
            if(dbTransOrder != null) {
                if(dbTransOrder.getStatus() == PayConstant.TRANS_RESULT_SUCCESS || dbTransOrder.getStatus() == PayConstant.TRANS_STATUS_COMPLETE) {
                    t.success();
                    return 0;
                }else {
                    if(new Date().getTime() - dbTransOrder.getCreateTime().getTime() < 2000) {
                        //防止重复提交,两秒内同一个订单不可重复调用
                        t.success();
                        return 0;
                    }
                    TransOrderExample example = new TransOrderExample();
                    TransOrderExample.Criteria criteria = example.createCriteria();
                    criteria.andMchIdEqualTo(transOrder.getMchId());
                    criteria.andMchTransNoEqualTo(transOrder.getMchTransNo());
                    transOrderMapper.deleteByExample(example);
                }
            }
            int i = transOrderMapper.insertSelective(transOrder);
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

    @CatTransaction(type = "SERVICE", name = "BaseService4TransOrder.baseSelectTransOrder")
    public TransOrder baseSelectTransOrder(String transOrderId) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4TransOrder.baseSelectTransOrder");
        try {
            TransOrder transOrder = transOrderMapper.selectByPrimaryKey(transOrderId);
            t.success();
            return transOrder;
        } catch (Exception e) {
            logger.error("",e);
            t.error(e);
            throw e;
        } finally {
            t.end();
        }
    }

    @CatTransaction(type = "SERVICE", name = "BaseService4TransOrder.baseSelectByMchIdAndTransOrderId")
    public TransOrder baseSelectByMchIdAndTransOrderId(String mchId, String transOrderId) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4TransOrder.baseSelectByMchIdAndTransOrderId");
        try {
            TransOrderExample example = new TransOrderExample();
            TransOrderExample.Criteria criteria = example.createCriteria();
            criteria.andMchIdEqualTo(mchId);
            criteria.andTransOrderIdEqualTo(transOrderId);
            List<TransOrder> transOrderList = transOrderMapper.selectByExample(example);
            t.success();
            return CollectionUtils.isEmpty(transOrderList) ? null : transOrderList.get(0);
        } catch (Exception e) {
            logger.error("",e);
            t.error(e);
            throw e;
        } finally {
            t.end();
        }
    }

    @CatTransaction(type = "SERVICE", name = "BaseService4TransOrder.baseSelectByMchIdAndMchTransNo")
    public TransOrder baseSelectByMchIdAndMchTransNo(String mchId, String mchTransNo) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4TransOrder.baseSelectByMchIdAndMchTransNo");
        try {
            TransOrderExample example = new TransOrderExample();
            TransOrderExample.Criteria criteria = example.createCriteria();
            criteria.andMchIdEqualTo(mchId);
            criteria.andMchTransNoEqualTo(mchTransNo);
            List<TransOrder> transOrderList = transOrderMapper.selectByExample(example);
            t.success();
            return CollectionUtils.isEmpty(transOrderList) ? null : transOrderList.get(0);
        } catch (Exception e) {
            logger.error("",e);
            t.error(e);
            throw e;
        } finally {
            t.end();
        }
    }

    @CatTransaction(type = "SERVICE", name = "BaseService4TransOrder.baseUpdateStatus4Ing")
    public int baseUpdateStatus4Ing(String transOrderId, String channelOrderNo) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4TransOrder.baseUpdateStatus4Ing");
        try {
            TransOrder transOrder = new TransOrder();
            transOrder.setStatus(PayConstant.TRANS_STATUS_TRANING);
            if(channelOrderNo != null) transOrder.setChannelOrderNo(channelOrderNo);
            transOrder.setTransSuccTime(new Date());
            TransOrderExample example = new TransOrderExample();
            TransOrderExample.Criteria criteria = example.createCriteria();
            criteria.andTransOrderIdEqualTo(transOrderId);
            List<Byte> list = new LinkedList<>();
            list.add(PayConstant.TRANS_STATUS_INIT);
            list.add(PayConstant.TRANS_STATUS_FAIL);
            criteria.andStatusIn(list);
            int i = transOrderMapper.updateByExampleSelective(transOrder, example);
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

    @CatTransaction(type = "SERVICE", name = "BaseService4TransOrder.baseUpdateStatus4Success")
    public int baseUpdateStatus4Success(String transOrderId) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4TransOrder.baseUpdateStatus4Success");
        try {
            int i = baseUpdateStatus4Success(transOrderId, null);
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

    public int baseUpdateStatus4Success(String transOrderId, String channelOrderNo) {
        TransOrder transOrder = new TransOrder();
        transOrder.setTransOrderId(transOrderId);
        transOrder.setStatus(PayConstant.TRANS_STATUS_SUCCESS);
        transOrder.setResult(PayConstant.TRANS_RESULT_SUCCESS);
        transOrder.setTransSuccTime(new Date());
        if(StringUtils.isNotBlank(channelOrderNo)) transOrder.setChannelOrderNo(channelOrderNo);
        TransOrderExample example = new TransOrderExample();
        TransOrderExample.Criteria criteria = example.createCriteria();
        criteria.andTransOrderIdEqualTo(transOrderId);
        criteria.andStatusEqualTo(PayConstant.TRANS_STATUS_TRANING);
        return transOrderMapper.updateByExampleSelective(transOrder, example);
    }

    @CatTransaction(type = "SERVICE", name = "BaseService4TransOrder.baseUpdateStatus4Complete")
    public int baseUpdateStatus4Complete(String transOrderId) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4TransOrder.baseUpdateStatus4Complete");
        try {
            TransOrder transOrder = new TransOrder();
            transOrder.setTransOrderId(transOrderId);
            transOrder.setStatus(PayConstant.TRANS_STATUS_COMPLETE);
            TransOrderExample example = new TransOrderExample();
            TransOrderExample.Criteria criteria = example.createCriteria();
            criteria.andTransOrderIdEqualTo(transOrderId);
            List values = CollectionUtils.arrayToList(new Byte[] {
                    PayConstant.TRANS_STATUS_SUCCESS, PayConstant.TRANS_STATUS_FAIL
            });
            criteria.andStatusIn(values);
            int i = transOrderMapper.updateByExampleSelective(transOrder, example);
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

    @CatTransaction(type = "SERVICE", name = "BaseService4TransOrder.baseUpdateStatus4Fail")
    public int baseUpdateStatus4Fail(String transOrderId, String channelErrCode, String channelErrMsg) {
        ICommon t = catMonitor.newTransaction("SERVICE", "BaseService4TransOrder.baseUpdateStatus4Fail");
        try {
            TransOrder transOrder = new TransOrder();
            transOrder.setStatus(PayConstant.TRANS_STATUS_FAIL);
            transOrder.setResult(PayConstant.TRANS_RESULT_FAIL);
            if(channelErrCode != null) transOrder.setChannelErrCode(channelErrCode);
            if(channelErrMsg != null) transOrder.setChannelErrMsg(channelErrMsg);
            TransOrderExample example = new TransOrderExample();
            TransOrderExample.Criteria criteria = example.createCriteria();
            criteria.andTransOrderIdEqualTo(transOrderId);
            criteria.andStatusEqualTo(PayConstant.TRANS_STATUS_TRANING);
            int i = transOrderMapper.updateByExampleSelective(transOrder, example);
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
