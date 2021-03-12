package cn.wuyi.payment.rpcimpl;

import cn.wuyi.payment.channel.alipay.AlipayConfig;
import cn.wuyi.payment.rpc.IPayChannel4AliRpcService;
import cn.wuyi.payment.service.impl.BaseService4PayOrder;
import cn.wuyi.payment.service.impl.BaseService4RefundOrder;
import cn.wuyi.payment.service.impl.BaseService4TransOrder;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.*;
import com.alipay.api.request.*;
import com.alipay.api.response.AlipayFundTransOrderQueryResponse;
import com.alipay.api.response.AlipayFundTransToaccountTransferResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.matrix.common.constant.PayConstant;
import com.matrix.common.domain.BaseParam;
import com.matrix.common.enumm.RetEnum;
import com.matrix.common.util.*;
import com.matrix.dao.model.PayChannel;
import com.matrix.dao.model.PayOrder;
import com.matrix.dao.model.RefundOrder;
import com.matrix.dao.model.TransOrder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service("payChannel4AliRpcService")
public class PayChannel4AliRpcServiceImpl implements IPayChannel4AliRpcService {

    private static final MyLog _log = MyLog.getLog(PayChannel4AliRpcServiceImpl.class);

    
    private AlipayConfig alipayConfig;

    @Autowired
    private BaseService4PayOrder baseService4PayOrder;

    @Autowired
    private BaseService4TransOrder baseService4TransOrder;

    @Autowired
    private BaseService4RefundOrder baseService4RefundOrder;

    @Override
    public Map doAliPayWapReq(String jsonParam) {
        String logPrefix = "【支付宝WAP支付下单】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject payOrderObj = baseParam.isNullValue("payOrder") ? null : JSONObject.parseObject(bizParamMap.get("payOrder").toString());
        JSONObject payChannelObj = baseParam.isNullValue("payChannel") ? null : JSONObject.parseObject(bizParamMap.get("payChannel").toString());
        PayOrder payOrder = JSON.toJavaObject(payOrderObj, PayOrder.class);
        if (ObjectValidUtil.isInvalid(payOrder)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        String payOrderId = payOrder.getPayOrderId();
        PayChannel payChannel = JSON.toJavaObject(payChannelObj, PayChannel.class);
        alipayConfig = new AlipayConfig();
        alipayConfig.init(payChannel.getParam());
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getUrl(), alipayConfig.getApp_id(), alipayConfig.getRsa_private_key(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipay_public_key(), AlipayConfig.SIGNTYPE);
        AlipayTradeWapPayRequest alipay_request = new AlipayTradeWapPayRequest();
        // 封装请求支付信息
        AlipayTradeWapPayModel model=new AlipayTradeWapPayModel();
        model.setOutTradeNo(payOrderId);
        model.setSubject(payOrder.getSubject());
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setBody(payOrder.getBody());
        model.setProductCode("QUICK_WAP_PAY");
        if(payOrder.getExpireTime() != null) {
        	model.setTimeExpire(DateUtil.convertDateFormat(payOrder.getExpireTime()+"", "yyyyMMddHHmmss", "yyyy-MM-dd HH:mm:ss"));
        }
        // 获取extraParam参数
        JSONObject extraParam = JSON.parseObject(payOrder.getExtra());
        //设置返回地址
        model.setQuitUrl(extraParam.getString("quit_url"));
        alipay_request.setBizModel(model);
        //设置同步地址
        alipay_request.setReturnUrl(extraParam.getString("return_url"));   
        // 设置异步通知地址
        alipay_request.setNotifyUrl(alipayConfig.getNotify_url());
        String payUrl = null;
		Map<String, Object> map = new HashMap<>();
		try {
			payUrl = client.pageExecute(alipay_request).getBody();
		} catch (AlipayApiException e) {
			map.put("isSuccess", false);
			map.put("channelErrMsg", "商户支付宝支付信息配置错误!");
			return RpcUtil.createBizResult(baseParam, map);
		}
		if (StringUtils.isBlank(payUrl)) {
			map.put("isSuccess", false);
			map.put("channelErrMsg", "商户支付宝支付信息配置错误!");
			return RpcUtil.createBizResult(baseParam, map);
		}
        _log.info("{}生成跳转路径：payUrl={}", logPrefix, payUrl);
        baseService4PayOrder.baseUpdateStatus4Ing(payOrderId, null);
        _log.info("{}生成请求支付宝数据,req={}", logPrefix, alipay_request.getBizModel());
        _log.info("###### 商户统一下单处理完成 ######");
        map.put("isSuccess", true);
        map.put("payOrderId", payOrderId);
        map.put("payUrl", payUrl);
        return RpcUtil.createBizResult(baseParam, map);
    }

    @Override
    public Map doAliPayPcReq(String jsonParam) {
        String logPrefix = "【支付宝PC支付下单】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject payOrderObj = baseParam.isNullValue("payOrder") ? null : JSONObject.parseObject(bizParamMap.get("payOrder").toString());
        JSONObject payChannelObj = baseParam.isNullValue("payChannel") ? null : JSONObject.parseObject(bizParamMap.get("payChannel").toString());
        PayOrder payOrder = JSON.toJavaObject(payOrderObj, PayOrder.class);
        if (ObjectValidUtil.isInvalid(payOrder)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        String payOrderId = payOrder.getPayOrderId();
        PayChannel payChannel = JSON.toJavaObject(payChannelObj, PayChannel.class);
        alipayConfig = new AlipayConfig();
        alipayConfig.init(payChannel.getParam());
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getUrl(), alipayConfig.getApp_id(), alipayConfig.getRsa_private_key(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipay_public_key(), AlipayConfig.SIGNTYPE);
        AlipayTradePagePayRequest alipay_request = new AlipayTradePagePayRequest();
        // 封装请求支付信息
        AlipayTradePagePayModel model=new AlipayTradePagePayModel();
        model.setOutTradeNo(payOrderId);
        model.setSubject(payOrder.getSubject());
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setBody(payOrder.getBody());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        if(payOrder.getExpireTime() != null) {
        	model.setTimeExpire(DateUtil.convertDateFormat(payOrder.getExpireTime()+"", "yyyyMMddHHmmss", "yyyy-MM-dd HH:mm:ss"));
        }
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(alipayConfig.getNotify_url());
        // 获取extraParam参数
        JSONObject extraParam = JSON.parseObject(payOrder.getExtra());
        //设置同步地址
        alipay_request.setReturnUrl(extraParam.getString("return_url"));   
        String payUrl = null;
		Map<String, Object> map = new HashMap<>();
		try {
			payUrl = client.pageExecute(alipay_request).getBody();
		} catch (AlipayApiException e) {
			map.put("isSuccess", false);
			map.put("channelErrMsg", "商户支付宝支付信息配置错误!");
			return RpcUtil.createBizResult(baseParam, map);
		}
		if (StringUtils.isBlank(payUrl)) {
			map.put("isSuccess", false);
			map.put("channelErrMsg", "商户支付宝支付信息配置错误!");
			return RpcUtil.createBizResult(baseParam, map);
		}
        _log.info("{}生成跳转路径：payUrl={}", logPrefix, payUrl);
        baseService4PayOrder.baseUpdateStatus4Ing(payOrderId, null);
        _log.info("{}生成请求支付宝数据,req={}", logPrefix, alipay_request.getBizModel());
        _log.info("###### 商户统一下单处理完成 ######");
        map.put("isSuccess", true);
        map.put("payOrderId", payOrderId);
        map.put("payUrl", payUrl);
        return RpcUtil.createBizResult(baseParam, map);
    }

    @Override
    public Map doAliPayMobileReq(String jsonParam) {
        String logPrefix = "【支付宝APP支付下单】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject payOrderObj = baseParam.isNullValue("payOrder") ? null : JSONObject.parseObject(bizParamMap.get("payOrder").toString());
        JSONObject payChannelObj = baseParam.isNullValue("payChannel") ? null : JSONObject.parseObject(bizParamMap.get("payChannel").toString());

        PayOrder payOrder = JSON.toJavaObject(payOrderObj, PayOrder.class);
        if (ObjectValidUtil.isInvalid(payOrder)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        String payOrderId = payOrder.getPayOrderId();
        PayChannel payChannel = JSON.toJavaObject(payChannelObj, PayChannel.class);
        alipayConfig = new AlipayConfig();
        alipayConfig.init(payChannel.getParam());
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getUrl(), alipayConfig.getApp_id(), alipayConfig.getRsa_private_key(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipay_public_key(), AlipayConfig.SIGNTYPE);
        AlipayTradeAppPayRequest alipay_request = new AlipayTradeAppPayRequest();
        // 封装请求支付信息
        AlipayTradeAppPayModel model=new AlipayTradeAppPayModel();
        model.setOutTradeNo(payOrderId);
        model.setSubject(payOrder.getSubject());
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setBody(payOrder.getBody());
        model.setProductCode("QUICK_MSECURITY_PAY");
        if(payOrder.getExpireTime() != null) {
        	model.setTimeExpire(DateUtil.convertDateFormat(payOrder.getExpireTime()+"", "yyyyMMddHHmmss", "yyyy-MM-dd HH:mm:ss"));
        }
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(alipayConfig.getNotify_url());
        // 设置同步地址
        alipay_request.setReturnUrl(alipayConfig.getReturn_url());
        String payParams = null;
        Map<String, Object> map = new HashMap<>();
		try {
			payParams = client.sdkExecute(alipay_request).getBody();
		} catch (AlipayApiException e) {
			map.put("isSuccess", false);
			map.put("channelErrMsg", "商户支付宝支付信息配置错误!");
			return RpcUtil.createBizResult(baseParam, map);
		}
		if (StringUtils.isBlank(payParams)) {
			map.put("isSuccess", false);
			map.put("channelErrMsg", "商户支付宝支付信息配置错误!");
			return RpcUtil.createBizResult(baseParam, map);
		}
        baseService4PayOrder.baseUpdateStatus4Ing(payOrderId, null);
        _log.info("{}生成请求支付宝数据,payParams={}", logPrefix, payParams);
        _log.info("###### 商户统一下单处理完成 ######");
        map.put("isSuccess", true);
        map.put("payOrderId", payOrderId);
        map.put("payParams", payParams);
        return RpcUtil.createBizResult(baseParam, map);
    }

    @Override
    public Map doAliPayQrReq(String jsonParam) {
        String logPrefix = "【支付宝当面付之扫码支付下单】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject payOrderObj = baseParam.isNullValue("payOrder") ? null : JSONObject.parseObject(bizParamMap.get("payOrder").toString());
        JSONObject payChannelObj = baseParam.isNullValue("payChannel") ? null : JSONObject.parseObject(bizParamMap.get("payChannel").toString());
        PayOrder payOrder = JSON.toJavaObject(payOrderObj, PayOrder.class);
        if (ObjectValidUtil.isInvalid(payOrder)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        String payOrderId = payOrder.getPayOrderId();
        PayChannel payChannel = JSON.toJavaObject(payChannelObj, PayChannel.class);
        alipayConfig = new AlipayConfig();
        alipayConfig.init(payChannel.getParam());
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getUrl(), alipayConfig.getApp_id(), alipayConfig.getRsa_private_key(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipay_public_key(), AlipayConfig.SIGNTYPE);
        AlipayTradePrecreateRequest alipay_request = new AlipayTradePrecreateRequest();
        // 封装请求支付信息
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setOutTradeNo(payOrderId);
        model.setSubject(payOrder.getSubject());
        model.setTotalAmount(AmountUtil.convertCent2Dollar(payOrder.getAmount().toString()));
        model.setBody(payOrder.getBody());
        // 获取objParams参数
        String objParams = payOrder.getExtra();
        if (StringUtils.isNotEmpty(objParams)) {
            try {
                JSONObject objParamsJson = JSON.parseObject(objParams);
                if(StringUtils.isNotBlank(objParamsJson.getString("discountable_amount"))) {
                    //可打折金额
                    model.setDiscountableAmount(objParamsJson.getString("discountable_amount"));
                }
                if(StringUtils.isNotBlank(objParamsJson.getString("undiscountable_amount"))) {
                    //不可打折金额
                    model.setUndiscountableAmount(objParamsJson.getString("undiscountable_amount"));
                }
            } catch (Exception e) {
                _log.error("{}objParams参数格式错误！", logPrefix);
            }
        }
        alipay_request.setBizModel(model);
        // 设置异步通知地址
        alipay_request.setNotifyUrl(alipayConfig.getNotify_url());
        // 设置同步地址
        alipay_request.setReturnUrl(alipayConfig.getReturn_url());
        String payUrl = null;
        Map<String, Object> map = new HashMap<>();
        try {
            payUrl = client.execute(alipay_request).getBody();
        } catch (AlipayApiException e) {
        	 map.put("isSuccess", false);
             map.put("channelErrMsg","商户支付宝支付信息配置错误!");
             return RpcUtil.createBizResult(baseParam, map); 
        }
        if(StringUtils.isBlank(payUrl)) {
        	map.put("isSuccess", false);
          	map.put("channelErrMsg","商户支付宝支付信息配置错误!");
          	return RpcUtil.createBizResult(baseParam, map); 
        }
        _log.info("{}生成跳转路径：payUrl={}", logPrefix, payUrl);
        baseService4PayOrder.baseUpdateStatus4Ing(payOrderId, null);
        _log.info("{}生成请求支付宝数据,req={}", logPrefix, alipay_request.getBizModel());
        _log.info("###### 商户统一下单处理完成 ######");
        map.put("isSuccess", true);
        map.put("payOrderId", payOrderId);
        map.put("payUrl", payUrl);
        return RpcUtil.createBizResult(baseParam, map);
    }

    /**
     * 支付宝转账,文档:https://docs.open.alipay.com/api_28/alipay.fund.trans.toaccount.transfer
     * @param jsonParam
     * @return
     */
    @Override
    public Map doAliTransReq(String jsonParam) {
        String logPrefix = "【支付宝转账】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject transOrderObj = baseParam.isNullValue("transOrder") ? null : JSONObject.parseObject(bizParamMap.get("transOrder").toString());
        TransOrder transOrder = JSON.toJavaObject(transOrderObj, TransOrder.class);
        if (ObjectValidUtil.isInvalid(transOrder)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        String transOrderId = transOrder.getTransOrderId();
        String mchId = transOrder.getMchId();
        //String channelId = transOrder.getChannelId();
        PayChannel payChannel = baseService4TransOrder.baseSelectPayChannel(mchId, PayConstant.PAY_CHANNEL_ALIPAY_ALL);
        //alipayConfig = new AlipayConfig();
        alipayConfig.init(payChannel.getParam());
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getUrl(), alipayConfig.getApp_id(), alipayConfig.getRsa_private_key(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipay_public_key(), AlipayConfig.SIGNTYPE);
        AlipayFundTransToaccountTransferRequest request = new AlipayFundTransToaccountTransferRequest();
        AlipayFundTransToaccountTransferModel model = new AlipayFundTransToaccountTransferModel();
        model.setOutBizNo(transOrderId);
        model.setPayeeType("ALIPAY_LOGONID");                            // 收款方账户类型
        model.setPayeeAccount(transOrder.getChannelUser());              // 收款方账户
        model.setAmount(AmountUtil.convertCent2Dollar(transOrder.getAmount().toString()));
        model.setPayerShowName("支付转账");
        model.setPayeeRealName(transOrder.getUserName());
        model.setRemark(transOrder.getRemarkInfo());
        request.setBizModel(model);
        Map<String, Object> map = new HashMap<>();
        map.put("transOrderId", transOrderId);
        map.put("isSuccess", false);
        try {
            AlipayFundTransToaccountTransferResponse response = client.execute(request);
            if(response.isSuccess()) {
                map.put("isSuccess", true);
                map.put("channelOrderNo", response.getOrderId());
            }else {
                //出现业务错误
                _log.info("{}返回失败", logPrefix);
                _log.info("sub_code:{},sub_msg:{}", response.getSubCode(), response.getSubMsg());
                map.put("channelErrCode", response.getSubCode());
                map.put("channelErrMsg", response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            _log.error(e, "");
        }
        return RpcUtil.createBizResult(baseParam, map);
    }

    @Override
    public Map getAliTransReq(String jsonParam) {
        String logPrefix = "【支付宝转账查询】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject transOrderObj = baseParam.isNullValue("transOrder") ? null : JSONObject.parseObject(bizParamMap.get("transOrder").toString());
        TransOrder transOrder = JSON.toJavaObject(transOrderObj, TransOrder.class);
        if (ObjectValidUtil.isInvalid(transOrder)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        String transOrderId = transOrder.getTransOrderId();
        String mchId = transOrder.getMchId();
      //String channelId = transOrder.getChannelId();
        PayChannel payChannel = baseService4PayOrder.baseSelectPayChannel(mchId,PayConstant.PAY_CHANNEL_ALIPAY_ALL);
        alipayConfig.init(payChannel.getParam());
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getUrl(), alipayConfig.getApp_id(), alipayConfig.getRsa_private_key(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipay_public_key(), AlipayConfig.SIGNTYPE);
        AlipayFundTransOrderQueryRequest request = new AlipayFundTransOrderQueryRequest();
        AlipayFundTransOrderQueryModel model = new AlipayFundTransOrderQueryModel();
        model.setOutBizNo(transOrderId);
        model.setOrderId(transOrder.getChannelOrderNo());
        request.setBizModel(model);
        Map<String, Object> map = XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_SUCCESS, "", PayConstant.RETURN_VALUE_SUCCESS, null);
        map.put("transOrderId", transOrderId);
        try {
            AlipayFundTransOrderQueryResponse response = client.execute(request);
            if(response.isSuccess()){
                map.putAll((Map) JSON.toJSON(response));
                map.put("isSuccess", true);
            }else {
                _log.info("{}返回失败", logPrefix);
                _log.info("sub_code:{},sub_msg:{}", response.getSubCode(), response.getSubMsg());
                map.put("channelErrCode", response.getSubCode());
                map.put("channelErrMsg", response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            _log.error(e, "");
        }
        return RpcUtil.createBizResult(baseParam, map);
    }

    @Override
    public Map doAliRefundReq(String jsonParam) {
        String logPrefix = "【支付宝退款】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject refundOrderObj = baseParam.isNullValue("refundOrder") ? null : JSONObject.parseObject(bizParamMap.get("refundOrder").toString());
        RefundOrder refundOrder = JSON.toJavaObject(refundOrderObj, RefundOrder.class);
        if (ObjectValidUtil.isInvalid(refundOrder)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        String refundOrderId = refundOrder.getRefundOrderId();
        String mchId = refundOrder.getMchId();
      //String channelId = transOrder.getChannelId();
        PayChannel payChannel = baseService4PayOrder.baseSelectPayChannel(mchId,PayConstant.PAY_CHANNEL_ALIPAY_ALL);
        alipayConfig = new AlipayConfig();
        alipayConfig.init(payChannel.getParam());
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getUrl(), alipayConfig.getApp_id(), alipayConfig.getRsa_private_key(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipay_public_key(), AlipayConfig.SIGNTYPE);
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(refundOrder.getPayOrderId());
        model.setTradeNo(refundOrder.getChannelPayOrderNo());
        model.setOutRequestNo(refundOrderId);
        model.setRefundAmount(AmountUtil.convertCent2Dollar(refundOrder.getRefundAmount().toString()));
        model.setRefundReason("正常退款");
        request.setBizModel(model);
        Map<String, Object> map = new HashMap<>();
        map.put("refundOrderId", refundOrderId);
        map.put("isSuccess", false);
        try {
            AlipayTradeRefundResponse response = client.execute(request);
            if(response.isSuccess()){
                map.put("isSuccess", true);
                map.put("channelOrderNo", response.getTradeNo());
            }else {
                _log.info("{}返回失败", logPrefix);
                _log.info("sub_code:{},sub_msg:{}", response.getSubCode(), response.getSubMsg());
                map.put("channelErrCode", response.getSubCode());
                map.put("channelErrMsg", response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            _log.error(e, "");
        }
        return RpcUtil.createBizResult(baseParam, map);
    }

    @Override
    public Map getAliRefundReq(String jsonParam) {
        String logPrefix = "【支付宝退款查询】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        if (ObjectValidUtil.isInvalid(bizParamMap)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
        }
        JSONObject refundOrderObj = baseParam.isNullValue("refundOrder") ? null : JSONObject.parseObject(bizParamMap.get("refundOrder").toString());
        RefundOrder refundOrder = JSON.toJavaObject(refundOrderObj, RefundOrder.class);
        if (ObjectValidUtil.isInvalid(refundOrder)) {
            _log.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
        }
        String refundOrderId = refundOrder.getRefundOrderId();
        String mchId = refundOrder.getMchId();
        String channelId = refundOrder.getChannelId();
        PayChannel payChannel = baseService4PayOrder.baseSelectPayChannel(mchId, channelId.split("_")[0]);
        alipayConfig.init(payChannel.getParam());
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getUrl(), alipayConfig.getApp_id(), alipayConfig.getRsa_private_key(), AlipayConfig.FORMAT, AlipayConfig.CHARSET, alipayConfig.getAlipay_public_key(), AlipayConfig.SIGNTYPE);
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        AlipayTradeFastpayRefundQueryModel model = new AlipayTradeFastpayRefundQueryModel();
        model.setOutTradeNo(refundOrder.getPayOrderId());
        model.setTradeNo(refundOrder.getChannelPayOrderNo());
        model.setOutRequestNo(refundOrderId);
        request.setBizModel(model);
        Map<String, Object> map = new HashMap<>();
        map.put("refundOrderId", refundOrderId);
        try {
            AlipayTradeFastpayRefundQueryResponse response = client.execute(request);
            if(response.isSuccess()){
                map.putAll((Map) JSON.toJSON(response));
                map.put("isSuccess", true);
            }else {
                _log.info("{}返回失败", logPrefix);
                _log.info("sub_code:{},sub_msg:{}", response.getSubCode(), response.getSubMsg());
                map.put("channelErrCode", response.getSubCode());
                map.put("channelErrMsg", response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            _log.error(e, "");
        }
        return RpcUtil.createBizResult(baseParam, map);
    }

}
