package cn.wuyi.payment.rpcimpl;

import cn.wuyi.payment.channel.alipay.AlipayConfig;
import cn.wuyi.payment.rpc.INotifyPayRpcService;
import cn.wuyi.payment.service.impl.BaseNotify4MchPay;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.util.SignUtils;
import com.matrix.common.constant.PayConstant;
import com.matrix.common.domain.BaseParam;
import com.matrix.common.enumm.RetEnum;
import com.matrix.common.util.*;
import com.matrix.dao.model.PayChannel;
import com.matrix.dao.model.PayOrder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


@Service("notifyPayRpcService")
public class NotifyPayRpcServiceImpl extends BaseNotify4MchPay implements INotifyPayRpcService {

    private static final MyLog mylog = MyLog.getLog(NotifyPayRpcServiceImpl.class);

    private AlipayConfig alipayConfig;

    /**
     * @description: 处理支付宝支付回调
     *
     * @param jsonParam
     * @return 
     * @author wanghao
     * @date 2019年8月30日 上午10:15:02 
     * @version 1.0.0.1
     */
    @Override
    public Map doAliPayNotify(String jsonParam) {
        String logPrefix = "【处理支付宝支付回调】";
        mylog.info("====== 开始处理支付宝支付回调通知 ======");
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        try {
        	 Map<String, Object> bizParamMap = baseParam.getBizParamMap();
             if (ObjectValidUtil.isInvalid(bizParamMap)) {
                 mylog.warn("处理支付宝支付回调失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
                 return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
             }
             Map params = baseParam.isNullValue("params") ? null : (Map) bizParamMap.get("params");
             if (ObjectValidUtil.isInvalid(params)) {
                 mylog.warn("处理支付宝支付回调失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
                 return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
             }
             Map<String, Object> payContext = new HashMap<String, Object>();
             PayOrder payOrder;
             payContext.put("parameters", params);
             if(!verifyAliPayParams(payContext)) {
                 return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_PAY_NOTIFY_VERIFY_FAIL);
             }
             mylog.info("{}验证支付通知数据及签名通过", logPrefix);
             String trade_status = params.get("trade_status").toString();		// 交易状态
             // 支付状态成功或者完成
             if (trade_status.equals(PayConstant.AlipayConstant.TRADE_STATUS_SUCCESS) || trade_status.equals(PayConstant.AlipayConstant.TRADE_STATUS_FINISHED)) {
                 int updatePayOrderRows;
                 payOrder = (PayOrder)payContext.get("payOrder");
                 byte payStatus = payOrder.getStatus(); // 0：订单生成，1：支付中，-1：支付失败，2：支付成功，3：业务处理完成，-2：订单过期
                 payOrder.setStatus(PayConstant.PAY_STATUS_SUCCESS);
                 payOrder.setChannelOrderNo(StrUtil.toString(params.get("trade_no"), null));
                 if (payStatus != PayConstant.PAY_STATUS_SUCCESS && payStatus != PayConstant.PAY_STATUS_COMPLETE) {
                     updatePayOrderRows = super.baseUpdateStatus4Success(payOrder.getPayOrderId(), StrUtil.toString(params.get("trade_no"), null));
                     if (updatePayOrderRows != 1) {
                         mylog.error("{}更新支付状态失败,将payOrderId={},更新payStatus={}失败", logPrefix, payOrder.getPayOrderId(), PayConstant.PAY_STATUS_SUCCESS);
                         mylog.info("{}响应给支付宝结果：{}", logPrefix, PayConstant.RETURN_ALIPAY_VALUE_FAIL);
                         return RpcUtil.createBizResult(baseParam, PayConstant.RETURN_ALIPAY_VALUE_FAIL);
                     }
                     mylog.info("{}更新支付状态成功,将payOrderId={},更新payStatus={}成功", logPrefix, payOrder.getPayOrderId(), PayConstant.PAY_STATUS_SUCCESS);
                 }
                 boolean notifyResult = doNotify(payOrder, true);
                 if(notifyResult) {
                    mylog.info("====== 完成处理支付宝支付回调通知 ======");
                 	return RpcUtil.createBizResult(baseParam, PayConstant.RETURN_ALIPAY_VALUE_SUCCESS);
                 }else {
                 	return RpcUtil.createBizResult(baseParam, PayConstant.RETURN_ALIPAY_VALUE_FAIL);
                 }
             }else{
                 // 其他状态
                 mylog.info("{}支付状态trade_status={},不做业务处理", logPrefix, trade_status);
                 mylog.info("{}响应给支付宝结果：{}", logPrefix, PayConstant.RETURN_ALIPAY_VALUE_SUCCESS);
                 return RpcUtil.createBizResult(baseParam, PayConstant.RETURN_ALIPAY_VALUE_SUCCESS);
             }
		} catch (Exception e) {
			  mylog.error(e, "支付宝回调结果异常,异常原因");
			  return RpcUtil.createBizResult(baseParam, PayConstant.RETURN_ALIPAY_VALUE_FAIL);
		}
       
      
    }

    /**
     * @description: 处理微信支付回调
     *
     * @param jsonParam
     * @return 
     * @author wanghao
     * @date 2019年8月30日 上午10:16:06 
     * @version 1.0.0.1
     */
    @Override
    public Map doWxPayNotify(String jsonParam) {
        String logPrefix = "【处理微信支付回调】";
        mylog.info("====== 开始处理微信支付回调通知 ======");
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        try {
            Map<String, Object> bizParamMap = baseParam.getBizParamMap();
            if (ObjectValidUtil.isInvalid(bizParamMap)) {
                mylog.warn("处理微信支付回调失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
                return RpcUtil.createBizResult(baseParam, WxPayNotifyResponse.fail(RetEnum.RET_PARAM_NOT_FOUND.getMessage()));
            }
            String xmlResult = baseParam.isNullValue("xmlResult") ? null : bizParamMap.get("xmlResult").toString();
            if (ObjectValidUtil.isInvalid(xmlResult)) {
                mylog.warn("处理微信支付回调失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
                return RpcUtil.createBizResult(baseParam, WxPayNotifyResponse.fail(RetEnum.RET_PARAM_INVALID.getMessage()));
            }
            WxPayOrderNotifyResult result = WxPayOrderNotifyResult.fromXML(xmlResult);
            Map<String, Object> payContext = new HashMap<String, Object>();
            payContext.put("parameters", result);
            // 验证业务数据是否正确,验证通过后返回PayOrder和WxPayConfig对象
            if(!verifyWxPayParams(payContext)) {
                return RpcUtil.createBizResult(baseParam, WxPayNotifyResponse.fail((String) payContext.get("retMsg")));
            }
            PayOrder payOrder = (PayOrder) payContext.get("payOrder");
            // 处理订单
            byte payStatus = payOrder.getStatus(); // 0：订单生成，1：支付中，-1：支付失败，2：支付成功，3：业务处理完成，-2：订单过期
            payOrder.setStatus(PayConstant.PAY_STATUS_SUCCESS);
            payOrder.setChannelOrderNo(result.getTransactionId());
            if (payStatus != PayConstant.PAY_STATUS_SUCCESS && payStatus != PayConstant.PAY_STATUS_COMPLETE) {
                int updatePayOrderRows = super.baseUpdateStatus4Success(payOrder.getPayOrderId(), result.getTransactionId());
                if (updatePayOrderRows != 1) {
                    mylog.error("{}更新支付状态失败,将payOrderId={},更新payStatus={}失败", logPrefix, payOrder.getPayOrderId(), PayConstant.PAY_STATUS_SUCCESS);
                    return RpcUtil.createBizResult(baseParam, WxPayNotifyResponse.fail("处理订单失败"));
                }
                mylog.error("{}更新支付状态成功,将payOrderId={},更新payStatus={}成功", logPrefix, payOrder.getPayOrderId(), PayConstant.PAY_STATUS_SUCCESS);
            }
            // 业务系统后端通知
            boolean notifyResult = doNotify(payOrder, true);
       	 	mylog.info("====== 完成处理微信支付回调通知 ======"+notifyResult);
            if(notifyResult) {
                 return RpcUtil.createBizResult(baseParam, WxPayNotifyResponse.success("OK"));
            }else {
            	 return RpcUtil.createBizResult(baseParam, WxPayNotifyResponse.fail(RetEnum.RET_REMOTE_UNUSABLE.getMessage()));
            }
        } catch (Exception e) {
            mylog.error(e, "微信回调结果异常,异常原因");
            return RpcUtil.createBizResult(baseParam, WxPayNotifyResponse.fail(e.getMessage()));
        }
    }

    @Override
    public Map sendBizPayNotify(String jsonParam) {
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            mylog.warn("发送业务支付通知失败, {}. jsonParam={}", RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        String payOrderId = baseParam.isNullValue("payOrderId") ? null : bizParamMap.get("payOrderId").toString();
        if(ObjectValidUtil.isInvalid(payOrderId)) {
            mylog.warn("发送业务支付通知失败, {}. jsonParam={}", RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        PayOrder payOrder = super.baseSelectPayOrder(payOrderId);
        if(payOrder == null) return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_DATA_NOT_EXISTS);
        try {
            // 发送业务支付通知
            super.doNotify(payOrder, false);
        }catch (Exception e) {
            return RpcUtil.createBizResult(baseParam, 0);
        }
        return RpcUtil.createBizResult(baseParam, 1);
    }


    /**
     * @description: 验证支付宝支付通知参数
     *
     * @param payContext
     * @return 
     * @author wanghao
     * @date 2019年8月30日 上午10:13:37 
     * @version 1.0.0.1
     */
	public boolean verifyAliPayParams(Map<String, Object> payContext) {
		Map<String, String> params = (Map<String, String>) payContext.get("parameters");
		String out_trade_no = params.get("out_trade_no"); // 商户订单号
		String total_amount = params.get("total_amount"); // 支付金额
		if (StringUtils.isEmpty(out_trade_no)) {
			mylog.error("AliPay Notify parameter out_trade_no is empty. out_trade_no={}", out_trade_no);
			payContext.put("retMsg", "out_trade_no is empty");
			return false;
		}
		if (StringUtils.isEmpty(total_amount)) {
			mylog.error("AliPay Notify parameter total_amount is empty. total_fee={}", total_amount);
			payContext.put("retMsg", "total_amount is empty");
			return false;
		}
		String errorMessage;
		// 查询payOrder记录
		String payOrderId = out_trade_no;
		PayOrder payOrder = super.baseSelectPayOrder(payOrderId);
		if (payOrder == null) {
			mylog.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
			payContext.put("retMsg", "Can't found payOrder");
			return false;
		}
		// 查询payChannel记录
		String mchId = payOrder.getMchId();
		String channelId = payOrder.getChannelId();
		PayChannel payChannel = super.baseSelectPayChannel(mchId, PayConstant.PAY_CHANNEL_ALIPAY_ALL);
		if (payChannel == null) {
			mylog.error("Can't found payChannel form db. mchId={} channelId={}, ", payOrderId, mchId, channelId);
			payContext.put("retMsg", "Can't found payChannel");
			return false;
		}
		boolean verify_result = false;
		try {
			alipayConfig = new AlipayConfig();
			verify_result = AlipaySignature.rsaCheckV1(params,alipayConfig.init(payChannel.getParam()).getAlipay_public_key(), AlipayConfig.CHARSET, "RSA2");
		} catch (AlipayApiException e) {
			mylog.error(e, "AlipaySignature.rsaCheckV1 error");
		}
		
		// 验证签名
		if (!verify_result) {
			errorMessage = "rsaCheckV1 failed.";
			mylog.error("AliPay Notify parameter {}", errorMessage);
			payContext.put("retMsg", errorMessage);
			return false;
		}

		// 核对金额
		long aliPayAmt = new BigDecimal(total_amount).movePointRight(2).longValue();
		long dbPayAmt = payOrder.getAmount().longValue();
		if (dbPayAmt != aliPayAmt) {
			mylog.error("db payOrder record payPrice not equals total_amount. total_amount={},payOrderId={}",
					total_amount, payOrderId);
			payContext.put("retMsg", "");
			return false;
		}
		payContext.put("payOrder", payOrder);
		return true;
	}

    /**
     * @description: 验证微信支付通知参数
     *
     * @param payContext
     * @return 
     * @author wanghao
     * @date 2019年8月30日 上午10:13:14 
     * @version 1.0.0.1
     */
    public boolean verifyWxPayParams(Map<String, Object> payContext) {
		WxPayOrderNotifyResult params = (WxPayOrderNotifyResult) payContext.get("parameters");
		// 校验结果是否成功
		if (!PayConstant.RETURN_VALUE_SUCCESS.equalsIgnoreCase(params.getResultCode()) && !PayConstant.RETURN_VALUE_SUCCESS.equalsIgnoreCase(params.getReturnCode())) {
			mylog.error("returnCode={},resultCode={},errCode={},errCodeDes={}", params.getReturnCode(),params.getResultCode(), params.getErrCode(), params.getErrCodeDes());
			payContext.put("retMsg", "notify data failed");
			return false;
		}
		// 总金额
		Integer total_fee = params.getTotalFee();
		// 商户系统订单号
		String out_trade_no = params.getOutTradeNo();
		// 查询payOrder记录
		String payOrderId = out_trade_no;
		// 查询订单
		PayOrder payOrder = super.baseSelectPayOrder(payOrderId);
		if (payOrder == null) {
			mylog.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
			payContext.put("retMsg", "Can't found payOrder");
			return false;
		}
        
        //验证签名
        Map<String, String> map = params.toMap();
    	PayChannel channel = super.baseSelectPayChannel(payOrder.getMchId(),PayConstant.PAY_CHANNEL_WX_ALL);
    	String signKey = JSONObject.parseObject(channel.getParam()).getString("key");
        if (params.getSign() != null && !SignUtils.checkSign(map, WxPayConstants.SignType.MD5, signKey)) {
        	mylog.error("sign verify fail payOrderId={}, ", payOrderId);
			payContext.put("retMsg", "sign verify fail");
			return false;
        }

		// 核对金额
		long wxPayAmt = new BigDecimal(total_fee).longValue();
		long dbPayAmt = payOrder.getAmount().longValue();
		if (dbPayAmt != wxPayAmt) {
			mylog.error("db payOrder record payPrice not equals total_fee. total_fee={},payOrderId={}", total_fee,payOrderId);
			payContext.put("retMsg", "total_fee is not the same");
			return false;
		}
		payContext.put("payOrder", payOrder);
		return true;
    }


}
