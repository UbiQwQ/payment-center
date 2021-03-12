package cn.wuyi.payment.rpcimpl;

import cn.wuyi.payment.channel.wechat.WxPayUtil;
import cn.wuyi.payment.rpc.IPayChannel4WxRpcService;
import cn.wuyi.payment.service.impl.BaseService;
import cn.wuyi.payment.service.impl.BaseService4PayOrder;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.entpay.EntPayRequest;
import com.github.binarywang.wxpay.bean.entpay.EntPayResult;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryResult;
import com.github.binarywang.wxpay.bean.result.WxPayRefundQueryResult;
import com.github.binarywang.wxpay.bean.result.WxPayRefundResult;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderResult;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.github.binarywang.wxpay.util.SignUtils;
import com.matrix.common.constant.PayConstant;
import com.matrix.common.domain.BaseParam;
import com.matrix.common.enumm.RetEnum;
import com.matrix.common.util.JsonUtil;
import com.matrix.common.util.MyLog;
import com.matrix.common.util.ObjectValidUtil;
import com.matrix.common.util.RpcUtil;
import com.matrix.dao.model.PayChannel;
import com.matrix.dao.model.PayOrder;
import com.matrix.dao.model.RefundOrder;
import com.matrix.dao.model.TransOrder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 微信支付渠道接口
 *
 * @author wanghao
 * @date 2019年6月24日 下午4:45:07 
 * @version 1.0.0.1
 */
@Service("payChannel4WxRpcService")
public class PayChannel4WxRpcServiceImpl extends BaseService implements IPayChannel4WxRpcService {

    private final MyLog myLog = MyLog.getLog(PayChannel4WxRpcServiceImpl.class);

    @Autowired
    private BaseService4PayOrder baseService4PayOrder;
    
    public Map doWxPayReq(String jsonParam) {
        String logPrefix = "【微信支付统一下单】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        try{
            if (ObjectValidUtil.isInvalid(bizParamMap)) {
                myLog.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
                return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
            }
            JSONObject payOrderObj = baseParam.isNullValue("payOrder") ? null : JSONObject.parseObject(bizParamMap.get("payOrder").toString());
            JSONObject payChannelObj = baseParam.isNullValue("payChannel") ? null : JSONObject.parseObject(bizParamMap.get("payChannel").toString());
            String tradeType = baseParam.isNullValue("tradeType") ? null : bizParamMap.get("tradeType").toString();
            String channelType = baseParam.isNullValue("channelType") ? null : bizParamMap.get("channelType").toString();
            PayOrder payOrder = JSON.toJavaObject(payOrderObj, PayOrder.class);
            if (ObjectValidUtil.isInvalid(payOrder, tradeType)) {
                myLog.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
                return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
            }
            PayChannel payChannel = JSON.toJavaObject(payChannelObj, PayChannel.class);;
			if (payChannel == null) {
				myLog.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
				return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
			}
            JSONObject paramObj = JSON.parseObject(payChannel.getParam());
            // 待优化 缺少参数提示|20191112|zhengyanhua
            if(StringUtils.isNotBlank(channelType) && "1".equals(channelType)) {
                //小程序
                if(StringUtils.isEmpty(paramObj.getString("mpAppId"))){
                    myLog.warn("{}失败, {}. PayChannel={}", logPrefix, "缺少参数：小程序AppId", paramObj.toString());
                    return RpcUtil.createFailResult(baseParam, RetEnum.RET_CHANNEL_PARAM_NOT_FOUND);
                }
            }else {
                if(StringUtils.isEmpty(paramObj.getString("appId"))){
                    myLog.warn("{}失败, {}. PayChannel={}", logPrefix,"缺少参数：公众号AppId", paramObj.toString());
                    return RpcUtil.createFailResult(baseParam, RetEnum.RET_CHANNEL_PARAM_NOT_FOUND);
                }
            }
            myLog.warn("{},jsonParam参数@@==>{}",logPrefix,jsonParam);
            WxPayConfig wxPayConfig = WxPayUtil.getWxPayConfig(payChannel, tradeType,channelType, getNotifyUrl());
            WxPayService wxPayService = new WxPayServiceImpl();
            wxPayService.setConfig(wxPayConfig);
            WxPayUnifiedOrderRequest wxPayUnifiedOrderRequest = buildUnifiedOrderRequest(payOrder, wxPayConfig);
            String payOrderId = payOrder.getPayOrderId();
            WxPayUnifiedOrderResult wxPayUnifiedOrderResult;
            try {
                wxPayUnifiedOrderResult = wxPayService.unifiedOrder(wxPayUnifiedOrderRequest);
                myLog.info("{} >>> 下单成功", logPrefix);
                Map<String, Object> map = new HashMap<>();
                map.put("payOrderId", payOrderId);
                map.put("prepayId", wxPayUnifiedOrderResult.getPrepayId());
                int result = baseService4PayOrder.baseUpdateStatus4Ing(payOrderId, null);
                myLog.info("更新第三方支付订单号:payOrderId={},prepayId={},result={}", payOrderId, wxPayUnifiedOrderResult.getPrepayId(), result);
                switch (tradeType) {
                    case PayConstant.WxConstant.TRADE_TYPE_NATIVE : {
                        map.put("codeUrl", wxPayUnifiedOrderResult.getCodeURL());   // 二维码支付链接
                        break;
                    }
                    case PayConstant.WxConstant.TRADE_TYPE_APP : {
                        Map<String, String> payInfo = new HashMap<>();
                        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
                        String nonceStr = String.valueOf(System.currentTimeMillis());
                        // APP支付绑定的是微信开放平台上的账号，APPID为开放平台上绑定APP后发放的参数
                        String appId = wxPayConfig.getAppId();
                        Map<String, String> configMap = new HashMap<>();
                        // 此map用于参与调起sdk支付的二次签名,格式全小写，timestamp只能是10位,格式固定，切勿修改
                        String partnerId = wxPayConfig.getMchId();
                        configMap.put("prepayid", wxPayUnifiedOrderResult.getPrepayId());
                        configMap.put("partnerid", partnerId);
                        String packageValue = "Sign=WXPay";
                        configMap.put("package", packageValue);
                        configMap.put("timestamp", timestamp);
                        configMap.put("noncestr", nonceStr);
                        configMap.put("appid", appId);
                        // 此map用于客户端与微信服务器交互
                        payInfo.put("sign", SignUtils.createSign(configMap, null,wxPayConfig.getMchKey(), null));
                        payInfo.put("prepayId", wxPayUnifiedOrderResult.getPrepayId());
                        payInfo.put("partnerId", partnerId);
                        payInfo.put("appId", appId);
                        payInfo.put("packageValue", packageValue);
                        payInfo.put("timeStamp", timestamp);
                        payInfo.put("nonceStr", nonceStr);
                        map.put("payParams", payInfo);
                        break;
                    }
                    case PayConstant.WxConstant.TRADE_TYPE_JSPAI : {
                        Map<String, String> payInfo = new HashMap<>();
                        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
                        String nonceStr = String.valueOf(System.currentTimeMillis());
                        payInfo.put("appId", wxPayUnifiedOrderResult.getAppid());
                        // 支付签名时间戳，注意微信jssdk中的所有使用timestamp字段均为小写。但最新版的支付后台生成签名使用的timeStamp字段名需大写其中的S字符
                        payInfo.put("timeStamp", timestamp);
                        payInfo.put("nonceStr", nonceStr);
                        payInfo.put("package", "prepay_id=" + wxPayUnifiedOrderResult.getPrepayId());
                        payInfo.put("signType", WxPayConstants.SignType.MD5);
                        payInfo.put("paySign", SignUtils.createSign(payInfo,null, wxPayConfig.getMchKey(), null));
                        map.put("payParams", payInfo);
                        break;
                    }
                    case PayConstant.WxConstant.TRADE_TYPE_MWEB : {
                        map.put("payUrl", wxPayUnifiedOrderResult.getMwebUrl());    // h5支付链接地址
                        //String content = HttpConnection.getUrl(wxPayUnifiedOrderResult.getMwebUrl(), "utf-8", "aiyouyi.cn");
                        //content = content.replace("top.location.href=url", "window.top.postMessage({ success: true, pay_url: url }, '*')");
                        //content = content.replace("top.location.href=redirect_url", "self.location.href=redirect_url");
                        //map.put("payHtml", content);  
                        break;
                    } 
                }
                return RpcUtil.createBizResult(baseParam, map);
            } catch (WxPayException e) {
                myLog.error(e, "下单失败");
                //出现业务错误
                myLog.info("{}下单返回失败", logPrefix);
                myLog.info("returnMsg{}",e.getReturnMsg());
                myLog.info("err_code:{}", e.getErrCode());
                myLog.info("err_code_des:{}", e.getErrCodeDes());
                Map<String, Object> resultMap = null;
                if (baseParam != null) {
                    resultMap = baseParam.convert2Map();
                } else {
                    resultMap = new HashMap();
                }
                ((Map)resultMap).put("rpcRetCode", e.getErrCode());
                ((Map)resultMap).put("rpcRetMsg", (e.getReturnMsg() == null ? "" : "["+e.getReturnMsg()+"]") + (e.getErrCodeDes() == null ? "" : e.getErrCodeDes()) );
                //return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_WX_PAY_CREATE_FAIL);
                return resultMap;
            }
        }catch (Exception e) {
            myLog.error(e, "微信支付统一下单异常");
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_WX_PAY_CREATE_FAIL);
        }
    }

    @Override
    public Map doWxTransReq(String jsonParam) {
        String logPrefix = "【微信企业付款】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        try{
            if (ObjectValidUtil.isInvalid(bizParamMap)) {
                myLog.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
                return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
            }
            JSONObject transOrderObj = baseParam.isNullValue("transOrder") ? null : JSONObject.parseObject(bizParamMap.get("transOrder").toString());
            TransOrder transOrder = JSON.toJavaObject(transOrderObj, TransOrder.class);
            if (ObjectValidUtil.isInvalid(transOrder)) {
                myLog.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
                return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
            }
            PayChannel payChannel = super.baseSelectPayChannel(transOrder.getMchId(), PayConstant.PAY_CHANNEL_WX_ALL);
            Map<String, Object> map = new HashMap<>();
            if(payChannel == null) {
            	map.put("isSuccess", false);
                map.put("channelErrMsg","商户微信支付渠道配置信息为空!");
                return RpcUtil.createBizResult(baseParam, map);
            }
            WxPayConfig wxPayConfig = WxPayUtil.getWxPayConfig(payChannel, "", transOrderObj.getString("channelType"),getNotifyUrl());
            WxPayService wxPayService = new WxPayServiceImpl();
            wxPayService.setConfig(wxPayConfig);
            EntPayRequest entPayRequest = buildWxEntPayRequest(transOrder, wxPayConfig);
            String transOrderId = transOrder.getTransOrderId();
            EntPayResult result;
            try {
                result = wxPayService.getEntPayService().entPay(entPayRequest);
                myLog.info("{} >>> 转账成功", logPrefix);
                map.put("transOrderId", transOrderId);
                map.put("isSuccess", true);
                map.put("channelOrderNo", result.getPaymentNo());
            } catch (WxPayException e) {
                myLog.error(e, "转账失败");
                //出现业务错误
                myLog.info("{}转账返回失败", logPrefix);
                myLog.info("err_code:{}", e.getErrCode());
                myLog.info("err_code_des:{}", e.getErrCodeDes());
                map.put("transOrderId", transOrderId);
                map.put("isSuccess", false);
                map.put("channelErrCode", e.getErrCode());
                map.put("channelErrMsg",(e.getErrCodeDes() == null ? "" :e.getErrCodeDes()) + (e.getReturnMsg() == null ? "" : "["+e.getReturnMsg()+"]") + (e.getCustomErrorMsg() == null ? "" : "["+e.getCustomErrorMsg()+"]") );
            }
            return RpcUtil.createBizResult(baseParam, map);
        }catch (Exception e) {
            myLog.error(e, "微信转账异常");
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_WX_PAY_CREATE_FAIL);
        }
    }

    @Override
    public Map getWxTransReq(String jsonParam) {
        String logPrefix = "【微信企业付款查询】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        try{
            if (ObjectValidUtil.isInvalid(bizParamMap)) {
                myLog.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
                return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
            }
            JSONObject transOrderObj = baseParam.isNullValue("transOrder") ? null : JSONObject.parseObject(bizParamMap.get("transOrder").toString());
            TransOrder transOrder = JSON.toJavaObject(transOrderObj, TransOrder.class);
            if (ObjectValidUtil.isInvalid(transOrder)) {
                myLog.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
                return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
            }
            PayChannel payChannel = super.baseSelectPayChannel(transOrder.getChannelId(), PayConstant.PAY_CHANNEL_WX_ALL);
            Map<String, Object> map = new HashMap<>();
            if(payChannel == null) {
            	map.put("isSuccess", false);
                map.put("channelErrMsg","商户微信支付渠道配置信息为空!");
                return RpcUtil.createBizResult(baseParam, map);
            }
            WxPayConfig wxPayConfig = WxPayUtil.getWxPayConfig(payChannel, "",null, getNotifyUrl());
            WxPayService wxPayService = new WxPayServiceImpl();
            wxPayService.setConfig(wxPayConfig);
            String transOrderId = transOrder.getTransOrderId();
            WxPayOrderQueryResult result;
            try {
                result = wxPayService.queryOrder(transOrderId, null);
                myLog.info("{} >>> 成功", logPrefix);
                map.putAll((Map) JSON.toJSON(result));
                map.put("isSuccess", true);
                map.put("transOrderId", transOrderId);
            } catch (WxPayException e) {
                myLog.error(e, "失败");
                //出现业务错误
                myLog.info("{}返回失败", logPrefix);
                myLog.info("err_code:{}", e.getErrCode());
                myLog.info("err_code_des:{}", e.getErrCodeDes());
                map.put("channelErrCode", e.getErrCode());
                map.put("channelErrMsg",(e.getErrCodeDes() == null ? "" :e.getErrCodeDes()) + (e.getReturnMsg() == null ? "" : "["+e.getReturnMsg()+"]") + (e.getMessage() == null ? "" : "["+e.getMessage()+"]") );
                map.put("isSuccess", false);
            }
            return RpcUtil.createBizResult(baseParam, map);
        }catch (Exception e) {
            myLog.error(e, "微信企业付款查询异常");
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_WX_PAY_CREATE_FAIL);
        }
    }

    @Override
    public Map doWxRefundReq(String jsonParam) {
        String logPrefix = "【微信退款】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        try{
            if (ObjectValidUtil.isInvalid(bizParamMap)) {
                myLog.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
                return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
            }
            JSONObject refundOrderObj = baseParam.isNullValue("refundOrder") ? null : JSONObject.parseObject(bizParamMap.get("refundOrder").toString());
            RefundOrder refundOrder = JSON.toJavaObject(refundOrderObj, RefundOrder.class);
            if (ObjectValidUtil.isInvalid(refundOrder)) {
                myLog.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
                return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
            }
            Map<String, Object> map = new HashMap<>();
            PayChannel payChannel = super.baseSelectPayChannel(refundOrder.getMchId(),PayConstant.PAY_CHANNEL_WX_ALL);
            if(payChannel == null) {
            	map.put("isSuccess", false);
                map.put("channelErrMsg","商户微信支付渠道配置信息为空!");
                return RpcUtil.createBizResult(baseParam, map);
            }
            WxPayConfig wxPayConfig = WxPayUtil.getWxPayConfig(payChannel, "",null,getNotifyUrl());
            WxPayService wxPayService = new WxPayServiceImpl();
            if (StringUtils.isNotBlank(refundOrder.getAppId())) {
                wxPayConfig.setAppId(refundOrder.getAppId());
            }
            wxPayService.setConfig(wxPayConfig);
            WxPayRefundRequest wxPayRefundRequest = buildWxPayRefundRequest(refundOrder, wxPayConfig);
            String refundOrderId = refundOrder.getRefundOrderId();
            WxPayRefundResult result;
            try {
                result = wxPayService.refund(wxPayRefundRequest);
                myLog.info("{} >>> 下单成功", logPrefix);
                map.put("isSuccess", true);
                //系统退款单号
                map.put("refundOrderId", refundOrderId);
                //渠道退款单号
                map.put("channelOrderNo", result.getRefundId());
                //渠道支付单号
                map.put("channelPayOrderNo", result.getTransactionId());
            } catch (WxPayException e) {
                myLog.error(e, "下单失败");
                //出现业务错误
                myLog.info("{}下单返回失败", logPrefix);
                myLog.info("err_code:{}", e.getErrCode());
                myLog.info("err_code_des:{}", e.getErrCodeDes());
                myLog.error(e.getMessage());
                myLog.error(e.getCustomErrorMsg());
                map.put("isSuccess", false);
                map.put("channelErrCode", e.getErrCode());
                map.put("channelErrMsg",(e.getErrCodeDes() == null ? "" :e.getErrCodeDes()) + (e.getReturnMsg() == null ? "" : "["+e.getReturnMsg()+"]") + (e.getCustomErrorMsg() == null ? "" : "["+e.getCustomErrorMsg()+"]") );
            }
            return RpcUtil.createBizResult(baseParam, map);
        }catch (Exception e) {
            myLog.error(e, "微信退款异常");
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_WX_PAY_CREATE_FAIL);
        }
    }

    @Override
    public Map getWxRefundReq(String jsonParam) {
        String logPrefix = "【微信退款查询】";
        BaseParam baseParam = JsonUtil.getObjectFromJson(jsonParam, BaseParam.class);
        Map<String, Object> bizParamMap = baseParam.getBizParamMap();
        try{
            if (ObjectValidUtil.isInvalid(bizParamMap)) {
                myLog.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_NOT_FOUND.getMessage(), jsonParam);
                return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_NOT_FOUND);
            }
            JSONObject refundOrderObj = baseParam.isNullValue("refundOrder") ? null : JSONObject.parseObject(bizParamMap.get("refundOrder").toString());
            RefundOrder refundOrder = JSON.toJavaObject(refundOrderObj, RefundOrder.class);
            if (ObjectValidUtil.isInvalid(refundOrder)) {
                myLog.warn("{}失败, {}. jsonParam={}", logPrefix, RetEnum.RET_PARAM_INVALID.getMessage(), jsonParam);
                return RpcUtil.createFailResult(baseParam, RetEnum.RET_PARAM_INVALID);
            }
            PayChannel payChannel = super.baseSelectPayChannel(refundOrder.getMchId(),PayConstant.PAY_CHANNEL_WX_ALL);
            Map<String, Object> map = new HashMap<>();
            if(payChannel == null) {
            	map.put("isSuccess", false);
                map.put("channelErrMsg","商户微信支付渠道配置信息为空!");
                return RpcUtil.createBizResult(baseParam, map);
            }
            WxPayConfig wxPayConfig = WxPayUtil.getWxPayConfig(payChannel, "",null,  getNotifyUrl());
            WxPayService wxPayService = new WxPayServiceImpl();
            wxPayService.setConfig(wxPayConfig);
            String refundOrderId = refundOrder.getRefundOrderId();
            WxPayRefundQueryResult result;
            try {
                result = wxPayService.refundQuery(refundOrder.getChannelPayOrderNo(), refundOrder.getPayOrderId(), refundOrder.getRefundOrderId(), refundOrder.getChannelOrderNo());
                myLog.info("{} >>> 成功", logPrefix);
                map.putAll((Map) JSON.toJSON(result));
                map.put("isSuccess", true);
                map.put("refundOrderId", refundOrderId);
            } catch (WxPayException e) {
                myLog.error(e, "失败");
                //出现业务错误
                myLog.info("{}返回失败", logPrefix);
                myLog.info("err_code:{}", e.getErrCode());
                myLog.info("err_code_des:{}", e.getErrCodeDes());
                map.put("channelErrCode", e.getErrCode());
                map.put("channelErrMsg",(e.getErrCodeDes() == null ? "" :e.getErrCodeDes()) + (e.getReturnMsg() == null ? "" : "["+e.getReturnMsg()+"]") + (e.getCustomErrorMsg() == null ? "" : "["+e.getCustomErrorMsg()+"]") );
                map.put("isSuccess", false);
            }
            return RpcUtil.createBizResult(baseParam, map);
        }catch (Exception e) {
            myLog.error(e, "微信退款查询异常");
            return RpcUtil.createFailResult(baseParam, RetEnum.RET_BIZ_WX_PAY_CREATE_FAIL);
        }
    }

    /**
     * 构建微信统一下单请求数据
     * @param payOrder
     * @param wxPayConfig
     * @return
     */
    WxPayUnifiedOrderRequest buildUnifiedOrderRequest(PayOrder payOrder, WxPayConfig wxPayConfig) {
        String tradeType = wxPayConfig.getTradeType();
        String payOrderId = payOrder.getPayOrderId();
        Integer totalFee = payOrder.getAmount().intValue();// 支付金额,单位分
        String deviceInfo = payOrder.getDevice();
        String body = payOrder.getBody();
        String detail = null;
        String attach = null;
        String outTradeNo = payOrderId;
        String feeType = "CNY";
        String spBillCreateIP = payOrder.getClientIp();
        String timeStart = null;
        String timeExpire = payOrder.getExpireTime() == null? null : payOrder.getExpireTime()+"";
        String goodsTag = null;
        String notifyUrl = wxPayConfig.getNotifyUrl();
        String productId = null;
        if(tradeType.equals(PayConstant.WxConstant.TRADE_TYPE_NATIVE)) productId = JSON.parseObject(payOrder.getExtra()).getString("productId");
        String limitPay = null;
        String openId = null;
        if(tradeType.equals(PayConstant.WxConstant.TRADE_TYPE_JSPAI)) openId = JSON.parseObject(payOrder.getExtra()).getString("openId");
        String sceneInfo = null;
        if(tradeType.equals(PayConstant.WxConstant.TRADE_TYPE_MWEB)) sceneInfo = JSON.parseObject(payOrder.getExtra()).getString("sceneInfo");
        // 微信统一下单请求对象
        WxPayUnifiedOrderRequest request = new WxPayUnifiedOrderRequest();
        request.setDeviceInfo(deviceInfo);
        request.setBody(body);
        request.setDetail(detail);
        request.setAttach(attach);
        request.setOutTradeNo(outTradeNo);
        request.setFeeType(feeType);
        request.setTotalFee(totalFee);
        request.setSpbillCreateIp(spBillCreateIP);
        request.setTimeStart(timeStart);
        request.setTimeExpire(timeExpire);
        request.setGoodsTag(goodsTag);
        request.setNotifyUrl(notifyUrl);
        request.setTradeType(tradeType);
        request.setProductId(productId);
        request.setLimitPay(limitPay);
        request.setOpenid(openId);
        request.setSceneInfo(sceneInfo);
        return request;
    }
    
    /**
     * 构建微信企业付款请求数据
     * @param transOrder
     * @param wxPayConfig
     * @return
     */
    EntPayRequest buildWxEntPayRequest(TransOrder transOrder, WxPayConfig wxPayConfig) {
        // 微信企业付款请求对象
        EntPayRequest request = new EntPayRequest();
        request.setAmount(transOrder.getAmount().intValue()); // 金额,单位分
        String checkName = "NO_CHECK";
        if(transOrder.getExtra() != null) checkName = JSON.parseObject(transOrder.getExtra()).getString("checkName");
        request.setCheckName(checkName);
        request.setDescription(transOrder.getRemarkInfo());
        request.setReUserName(transOrder.getUserName());
        request.setPartnerTradeNo(transOrder.getTransOrderId());
        request.setDeviceInfo(transOrder.getDevice());
        request.setSpbillCreateIp(transOrder.getClientIp());
        request.setOpenid(transOrder.getChannelUser());
        return request;
    }

    /**
     * 构建微信退款请求数据
     * @param refundOrder
     * @param wxPayConfig
     * @return
     */
    WxPayRefundRequest buildWxPayRefundRequest(RefundOrder refundOrder, WxPayConfig wxPayConfig) {
        // 微信退款请求对象
        WxPayRefundRequest request = new WxPayRefundRequest();
        request.setTransactionId(refundOrder.getChannelPayOrderNo());
        request.setOutTradeNo(refundOrder.getPayOrderId());
        request.setDeviceInfo(refundOrder.getDevice());
        request.setOutRefundNo(refundOrder.getRefundOrderId());
        request.setRefundDesc(refundOrder.getRemarkInfo());
        request.setRefundFee(refundOrder.getRefundAmount().intValue());
        request.setRefundFeeType("CNY");
        request.setTotalFee(refundOrder.getPayAmount().intValue());
        return request;
    }
    private String getNotifyUrl() {
		return  getConfig("matrix-payment.wx_notify_url_" + getConfig("matrix-core.model"));
    }
}
