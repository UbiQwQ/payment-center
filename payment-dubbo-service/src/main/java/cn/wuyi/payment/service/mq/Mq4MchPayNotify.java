package cn.wuyi.payment.service.mq;

import cn.wuyi.payment.service.impl.BaseService4PayOrder;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.matrix.common.util.MyLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.jms.Queue;

/**
 * @Description: 商户通知MQ统一处理
 * @author dingzhiwei jmdhappy@126.com
 * @date 2017-10-31
 * @version V1.0
 * @Copyright: www.xxpay.org
 */
@Service("mq4MchPayNotify")
public class Mq4MchPayNotify extends Mq4MchNotify {

    @Autowired
    private Queue mchPayNotifyQueue;

    @Autowired
    private BaseService4PayOrder baseService4PayOrder;

    private static final MyLog _log = MyLog.getLog(Mq4MchPayNotify.class);

    public void send(String msg) {
        super.send(mchPayNotifyQueue, msg);
    }

    @JmsListener(destination = MqConfig.MCH_PAY_NOTIFY_QUEUE_NAME)
    public void receive(String msg) {
        String logPrefix = "【商户支付通知】";
        _log.info("{}接收消息:msg={}", logPrefix, msg);
        JSONObject msgObj = JSON.parseObject(msg);
        String respUrl = msgObj.getString("url");
        String orderId = msgObj.getString("orderId");
        int count = msgObj.getInteger("count");
        if(StringUtils.isEmpty(respUrl)) {
            _log.warn("{}商户通知URL为空,respUrl={}", logPrefix, respUrl);
            return;
        }
        String httpResult = httpPost(respUrl);
        int cnt = count + 1;
        _log.info("{}notifyCount={}", logPrefix, cnt);
        if("success".equalsIgnoreCase(httpResult)){
            // 修改支付订单表
            try {
                int result = baseService4PayOrder.baseUpdateStatus4Complete(orderId);
                _log.info("{}修改payOrderId={},订单状态为处理完成->{}", logPrefix, orderId, result == 1 ? "成功" : "失败");
            } catch (Exception e) {
                _log.error(e, "修改订单状态为处理完成异常");
            }
            // 修改通知
            try {
                int result = super.baseUpdateMchNotifySuccess(orderId, httpResult, (byte) cnt);
                _log.info("{}修改商户通知,orderId={},result={},notifyCount={},结果:{}", logPrefix, orderId, httpResult, cnt, result == 1 ? "成功" : "失败");
            }catch (Exception e) {
                _log.error(e, "修改商户支付通知异常");
            }
            return ; // 通知成功结束
        }else {
            // 修改通知次数
            try {
                int result = super.baseUpdateMchNotifyFail(orderId, httpResult, (byte) cnt);
                _log.info("{}修改商户通知,orderId={},result={},notifyCount={},结果:{}", logPrefix, orderId, httpResult, cnt, result == 1 ? "成功" : "失败");
            }catch (Exception e) {
                _log.error(e, "修改商户支付通知异常");
            }
            if (cnt > 5) {
                _log.info("{}通知次数notifyCount()>5,停止通知", respUrl, cnt);
                return ;
            }
            // 通知失败，延时再通知
            msgObj.put("count", cnt);
            this.send(mchPayNotifyQueue, msgObj.toJSONString(), cnt * 60 * 1000);
            _log.info("{}发送延时通知完成,通知次数:{},{}秒后执行通知", respUrl, cnt, cnt * 60);
        }
    }
}
