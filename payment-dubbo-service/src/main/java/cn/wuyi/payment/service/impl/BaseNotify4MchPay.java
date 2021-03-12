package cn.wuyi.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.matrix.common.constant.PayConstant;
import com.matrix.common.util.MyLog;
import com.matrix.dao.model.PayOrder;
import com.matrix.monitor.aspectj.CatTransaction;
import com.matrix.monitor.cat.CatMonitor;
import com.matrix.monitor.cat.ICommon;
import com.matrix.util.RestfulHttpClient;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 商户支付通知处理基类
 *
 * @author wanghao
 * @date 2019年6月24日 下午4:45:38 
 * @version 1.0.0.1
 */
@Component
public class BaseNotify4MchPay extends BaseService4PayOrder {

	private static final MyLog myLog = MyLog.getLog(BaseNotify4MchPay.class);
	
	//private static final BaseMqProducer baseMqProducer = GroupPaymentSupport.getInstance().getBaseMqProducer();

	@Autowired
	@Qualifier("CatMonitor")
	private CatMonitor catMonitor;

	/**
	 * @description: 处理支付结果后台服务器通知
	 *
	 * @param payOrder
	 * @param isFirst 
	 * @author wanghao
	 * @date 2019年8月30日 上午10:32:26 
	 * @version 1.0.0.1
	 */
	@CatTransaction(type = "SERVICE", name = "BaseNotify4MchPay.doNotify")
	public boolean doNotify(PayOrder payOrder, boolean isFirst) {
		ICommon t = catMonitor.newTransaction("SERVICE", "BaseNotify4MchPay.doNotify");
		try {
			myLog.info("[>>>>>> PAY开始回调通知业务系统 <<<<<<]");
			String content = JSONObject.toJSONString(payOrder);
			// 插入商户通知
			int baseInsertMchNotifyStatus = baseInsertMchNotify(payOrder.getPayOrderId(), payOrder.getMchId(),payOrder.getMchOrderNo(), PayConstant.MCH_NOTIFY_TYPE_PAY,  payOrder.getNotifyUrl(),content);
			myLog.info("插入通知商户消息状态 baseInsertMchNotifyStatus:"+ baseInsertMchNotifyStatus);
			// 通知给商户
			myLog.info("发送通知商户业务系统url:"+payOrder.getNotifyUrl());
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Content-Type", "text/plain");
			myLog.info("发送商户业务系统参数:"+content);
			String result_content = RestfulHttpClient.request(RestfulHttpClient.METHOD_POST, payOrder.getNotifyUrl(), content, headers).getContent();
			//String result_content ="{\"code\":200,\"data\":200,\"status\":\"success\"}";
			myLog.info("商户业务系统返回数据:"+result_content);
			JSONObject result = JSONObject.parseObject(result_content);
			if ("success".equals(result.getString("status"))) {
				// 标记支付单已完成状态
				int baseUpdateStatus4CompleteStatus = baseUpdateStatus4Complete(payOrder.getPayOrderId());
				myLog.info("修改订单支付为处理完成状态 baseUpdateStatus4CompleteStatus:" + baseUpdateStatus4CompleteStatus);
				// 修改通知状态为成功状态
				int baseUpdateMchNotifySuccessStatus = baseUpdateMchNotifySuccess(payOrder.getPayOrderId(),result_content, Byte.valueOf("1"));
				myLog.info("修改商户通知为处理完成状态 baseUpdateMchNotifySuccessStatus:" + baseUpdateMchNotifySuccessStatus);
				myLog.info("[>>>>>> PAY回调通知业务系统完成 <<<<<<]");
				t.success();
				return true;
			}else {
				myLog.error("商户返回数据错误!!");
				baseUpdateMchNotifyFail(payOrder.getPayOrderId(),result_content, Byte.valueOf("1"));
			}
			// 下一面一行,MQ暂时不用了
			//PaymentRocketMQHelper.sendMessage(baseMqProducer,"PaymentPayNotify","*",content);
			t.success();
		} catch (Exception e) {
			myLog.error("通知商户处理异常:"+e.getMessage());
			baseUpdateMchNotifyFail(payOrder.getPayOrderId(),"回调商户业务系统异常!!", Byte.valueOf("1"));
			myLog.error(e, "payOrderId=%s,sendMessage error.", ObjectUtils.defaultIfNull(payOrder.getPayOrderId(), ""));
			t.error(e);
		} finally {
			t.end();
		}
		return false;
	}

	public void createNotifyInfo(PayOrder payOrder, boolean isFirst) {
		baseInsertMchNotify(payOrder.getPayOrderId(), payOrder.getMchId(), payOrder.getMchOrderNo(), PayConstant.MCH_NOTIFY_TYPE_PAY,  payOrder.getNotifyUrl(),JSONObject.toJSONString(payOrder));
	}

}
