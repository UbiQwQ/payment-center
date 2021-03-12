package cn.wuyi.payment.service.mq;

import com.matrix.base.BaseMqProducer;
import org.apache.log4j.Logger;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;

import java.util.List;

/**
 * @description: 商品发送消息
 *
 * @author wanghao
 * @date 2019年8月24日 下午2:10:30
 * @version 1.0.0.1
 */
public class PaymentRocketMQHelper {
	private static Logger logger = Logger.getLogger(PaymentRocketMQHelper.class);

	public static void sendMessage(BaseMqProducer baseMqProducer, String topic, String tag, String body) {
		Message msg = baseMqProducer.initMqMessage(topic, tag, body);
		DefaultMQProducer defaultMQProducer = baseMqProducer.getDefaultMQProducer();
		// 失败自动重发消息
		defaultMQProducer.setRetryTimesWhenSendAsyncFailed(3);
		try {
			defaultMQProducer.send(msg, new MessageQueueSelector() {
				@Override
				public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
					Integer id = (Integer) arg;
					return mqs.get(id);
				}
			}, 0, // 队列的下标
					60_000 // 超时时间
			);
		} catch (Exception ex) {
			logger.error("sendMessage-->ex:" + ex.toString());
		}
	}
}
