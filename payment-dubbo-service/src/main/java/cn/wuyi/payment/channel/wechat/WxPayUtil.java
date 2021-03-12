package cn.wuyi.payment.channel.wechat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.matrix.base.BaseClass;
import com.matrix.dao.model.PayChannel;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;


/**
 * @description: 
 *
 * @author wanghao
 * @date 2019年12月31日 下午5:28:12 
 * @version 1.0.0.1
 */
public class WxPayUtil extends BaseClass{

    /**
     * 获取微信支付配置
     * @param configParam
     * @param tradeType
     * @param certRootPath
     * @param notifyUrl
     * @return
     */
    public static WxPayConfig getWxPayConfig(PayChannel payChannel, String tradeType, String channelType,String notifyUrl) {
        WxPayConfig wxPayConfig = new WxPayConfig();
        JSONObject paramObj = JSON.parseObject(payChannel.getParam());
        wxPayConfig.setMchId(paramObj.getString("mchId"));
        if(StringUtils.isNotBlank(channelType) && "1".equals(channelType)) {
        	//小程序
        	wxPayConfig.setAppId(paramObj.getString("mpAppId"));
        }else {
        	wxPayConfig.setAppId(paramObj.getString("appId"));
        }
        String certBase64Content = paramObj.getString("certBase64Content");
        if(StringUtils.isNotBlank(certBase64Content)) {
        	 certBase64Content = certBase64Content.substring(certBase64Content.indexOf(",") + 1,certBase64Content.length());
             byte[] certContent = Base64.decodeBase64(certBase64Content);
             wxPayConfig.setKeyContent(certContent);
        }
        //wxPayConfig.setKeyContent(NetFileCilent.getNetFileByteContent(paramObj.getString("certLocalPath")));
        //wxPayConfig.setKeyContent(payChannel.getKeyContent().getBytes());
       // wxPayConfig.setKeyPath(certRootPath + File.separator + paramObj.getString("certLocalPath"));
        wxPayConfig.setMchKey(paramObj.getString("key"));
        wxPayConfig.setNotifyUrl(notifyUrl);
        wxPayConfig.setTradeType(tradeType);
        return wxPayConfig;
    }

    /**
     * 获取微信支付配置
     * @param configParam
     * @return
     */
    public static WxPayConfig getWxPayConfig(String configParam) {
        WxPayConfig wxPayConfig = new WxPayConfig();
        JSONObject paramObj = JSON.parseObject(configParam);
        wxPayConfig.setMchId(paramObj.getString("mchId"));
        wxPayConfig.setAppId(paramObj.getString("appId"));
        wxPayConfig.setMchKey(paramObj.getString("key"));
        return wxPayConfig;
    }
    
}
