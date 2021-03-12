package cn.wuyi.payment.service.mq;

import com.matrix.base.BaseClass;
import com.matrix.base.BaseMqProducer;
import com.matrix.gtt.GttEnum;

/**
 * @description: 商品组mq
 *
 * @author wanghao
 * @date 2019年8月24日 上午9:50:18 
 * @version 1.0.0.1
 */
public class GroupPaymentSupport extends BaseClass {

    private BaseMqProducer baseMqProducer = null;

    private GroupPaymentSupport() {
        baseMqProducer = new BaseMqProducer(GttEnum.GroupProduct);
    }
    private static class LazyHolder {
        private static final GroupPaymentSupport INSTANCE = new GroupPaymentSupport();
    }
    public static final GroupPaymentSupport getInstance() {
        return LazyHolder.INSTANCE;
    }


    public BaseMqProducer getBaseMqProducer() {
        return baseMqProducer;
    }

}
