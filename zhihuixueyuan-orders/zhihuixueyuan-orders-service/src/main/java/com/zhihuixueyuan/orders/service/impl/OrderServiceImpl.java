package com.zhihuixueyuan.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zhihuixueyuan.base.exception.ZHXYException;
import com.zhihuixueyuan.base.utils.IdWorkerUtils;
import com.zhihuixueyuan.base.utils.QRCodeUtil;
import com.zhihuixueyuan.messagesdk.model.po.MqMessage;
import com.zhihuixueyuan.messagesdk.service.MqMessageService;
import com.zhihuixueyuan.orders.config.AlipayConfig;
import com.zhihuixueyuan.orders.config.PayConfig;
import com.zhihuixueyuan.orders.config.PayNotifyConfig;
import com.zhihuixueyuan.orders.mapper.XcOrdersGoodsMapper;
import com.zhihuixueyuan.orders.mapper.XcOrdersMapper;
import com.zhihuixueyuan.orders.mapper.XcPayRecordMapper;
import com.zhihuixueyuan.orders.model.dto.AddOrderDto;
import com.zhihuixueyuan.orders.model.dto.PayRecordDto;
import com.zhihuixueyuan.orders.model.dto.PayStatusDto;
import com.zhihuixueyuan.orders.model.po.XcOrders;
import com.zhihuixueyuan.orders.model.po.XcOrdersGoods;
import com.zhihuixueyuan.orders.model.po.XcPayRecord;
import com.zhihuixueyuan.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/***
 * 订单相关接口实现类
 */

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    XcOrdersMapper ordersMapper;
    @Autowired
    XcOrdersGoodsMapper ordersGoodsMapper;
    @Autowired
    XcPayRecordMapper payRecordMapper;
    @Autowired
    OrderService currentProxy;
    @Autowired
    MqMessageService mqMessageService;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Value("${pay.qrcodeurl}")
    String qrcodeurl;
    String APP_ID= PayConfig.APP_ID;
    String APP_PRIVATE_KEY= PayConfig.APP_PRIVATE_KEY;
    String ALIPAY_PUBLIC_KEY= PayConfig.ALIPAY_PUBLIC_KEY;

    /***
     * 下单生成支付二维码
     * @return
     */
    @Override
    @Transactional
    public PayRecordDto createOder(String userId, AddOrderDto addOrderDto) {

        //创建商品订单
        XcOrders orders = saveXcOrders(userId, addOrderDto);
        if(orders==null){
            ZHXYException.cast("订单创建失败");
        }
        //生成支付记录
        XcPayRecord payRecord = createPayRecord(orders);
        //生成二维码
        String qrCode = null;
        try {
            String url = String.format(qrcodeurl, payRecord.getPayNo());
            qrCode = new QRCodeUtil().createQRCode(url, 200, 200);
        } catch (IOException e) {
            ZHXYException.cast("生成二维码出错");
        }
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord,payRecordDto);
        payRecordDto.setQrcode(qrCode);

        return payRecordDto;
    }

    @Override
    public XcPayRecord getPayRecordByPayno(String payNo) {
        XcPayRecord xcPayRecord = payRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
        return xcPayRecord;
    }

    /**
     * 请求支付宝查询支付结果，并且从支付宝获取的信息表示该订单已支付，则需要对订单表以及支付记录表中的相应记录进行修改，把状态字段改为“已支付”
     */
    @Override
    public PayRecordDto queryPayResult(String payNo) {
        //先从支付记录表获取该记录
        XcPayRecord payRecord = getPayRecordByPayno(payNo);
        if (payRecord == null) {
            ZHXYException.cast("请重新点击支付获取二维码");
        }
        //支付状态
        String status = payRecord.getStatus();
        //如果支付成功直接返回
        if ("601002".equals(status)) {
            PayRecordDto payRecordDto = new PayRecordDto();
            BeanUtils.copyProperties(payRecord, payRecordDto);
            return payRecordDto;
        }
        //从支付宝查询支付结果
        PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);
        //保存支付结果
        currentProxy.saveAliPayStatus(payStatusDto);
        //重新查询支付记录
        payRecord = getPayRecordByPayno(payNo);
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        return payRecordDto;
    }

    /**
     * @description 保存支付宝支付结果
     */
    @Override
    @Transactional
    public void saveAliPayStatus(PayStatusDto payStatusDto) {
        //支付流水号
        String payNo = payStatusDto.getOut_trade_no();  //payNo
        XcPayRecord payRecord = getPayRecordByPayno(payNo);
        if (payRecord == null) {
            ZHXYException.cast("支付记录找不到");
        }
        //关联的订单号
        Long orderId = payRecord.getOrderId();
        XcOrders orders = ordersMapper.selectById(orderId);
        if (orders == null) {
            ZHXYException.cast("根据支付记录找不到订单");
        }
        //支付结果
        String trade_status = payStatusDto.getTrade_status();
        log.debug("收到支付结果:{},支付记录:{}}", payStatusDto.toString(),payRecord.toString());
        if (trade_status.equals("TRADE_SUCCESS")) {

            //支付金额变为分
            Float totalPrice = payRecord.getTotalPrice() * 100;
            Float total_amount = Float.parseFloat(payStatusDto.getTotal_amount()) * 100;
            //校验是否一致
            if (totalPrice.intValue() != total_amount.intValue()) {
                //校验失败
                log.info("校验支付结果失败,支付记录:{},APP_ID:{},totalPrice:{}" ,payRecord.toString(),payStatusDto.getApp_id(),total_amount.intValue());
                ZHXYException.cast("校验支付结果失败");
            }
            log.debug("更新支付结果,支付交易流水号:{},支付结果:{}", payNo, trade_status);
            XcPayRecord payRecord_u = new XcPayRecord();
            payRecord_u.setStatus("601002"); //支付成功
            payRecord_u.setOutPayChannel("Alipay");
            payRecord_u.setOutPayNo(payStatusDto.getTrade_no()); //支付宝交易号
            payRecord_u.setPaySuccessTime(LocalDateTime.now()); //通知时间
            int update1 = payRecordMapper.update(payRecord_u, new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
            if (update1 > 0) {
                log.info("更新支付记录状态成功:{}", payRecord_u.toString());
            } else {
                log.info("更新支付记录状态失败:{}", payRecord_u.toString());
                ZHXYException.cast("更新支付记录状态失败");
            }
            XcOrders order_u = new XcOrders();
            order_u.setStatus("600002");//支付成功
            int update = ordersMapper.update(order_u, new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getId, orderId));
            if (update > 0) {
                log.info("更新订单表状态成功,订单号:{}", orderId);
            } else {
                log.info("更新订单表状态失败,订单号:{}", orderId);
                ZHXYException.cast("更新订单表状态失败");
            }
            //保存消息记录,参数1：支付结果通知类型，2: 业务id，3:业务类型
            MqMessage mqMessage = mqMessageService.addMessage("payresult_notify", orders.getOutBusinessId(), orders.getOrderType(), null);
            //通知消息
            notifyPayResult(mqMessage);
        }
    }

    /**
     * 发送支付完成通知结果
     * @param message
     */
    @Override
    public void notifyPayResult(MqMessage message) {
        //1、消息体，转json
        String msg = JSON.toJSONString(message);
        //设置消息持久化
        Message msgObj = MessageBuilder.withBody(msg.getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
        // 2.全局唯一的消息ID，需要封装到CorrelationData中
        CorrelationData correlationData = new CorrelationData(message.getId().toString());
        // 3.添加callback
        correlationData.getFuture().addCallback(
                result -> {
                    if(result.isAck()){
                        // 3.1.ack，消息成功
                        log.debug("通知支付结果消息发送成功, ID:{}", correlationData.getId());
                        //删除消息表中的记录
                        mqMessageService.completed(message.getId());
                    }else{
                        // 3.2.nack，消息失败
                        log.error("通知支付结果消息发送失败, ID:{}, 原因{}",correlationData.getId(), result.getReason());
                    }
                },
                ex -> log.error("消息发送异常, ID:{}, 原因{}",correlationData.getId(),ex.getMessage())
        );
        // 发送消息
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT, "", msgObj,correlationData);

    }

    /**
     * 请求支付宝查询支付结果
     */
    public PayStatusDto queryPayResultFromAlipay(String payNo) {

        //========请求支付宝查询支付结果=============
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, "json", AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE); //获得初始化的AlipayClient
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payNo);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                ZHXYException.cast("请求支付查询查询失败");
            }
        } catch (AlipayApiException e) {
            log.error("请求支付宝查询支付结果异常:{}", e.toString(), e);
            ZHXYException.cast("请求支付查询查询失败");
        }

        //获取支付结果
        String resultJson = response.getBody();
        //转map
        Map resultMap = JSON.parseObject(resultJson, Map.class);
        Map alipay_trade_query_response = (Map) resultMap.get("alipay_trade_query_response");
        //支付结果
        String trade_status = (String) alipay_trade_query_response.get("trade_status");
        String total_amount = (String) alipay_trade_query_response.get("total_amount");
        String trade_no = (String) alipay_trade_query_response.get("trade_no");
        //保存支付结果
        PayStatusDto payStatusDto = new PayStatusDto();
        payStatusDto.setOut_trade_no(payNo);
        payStatusDto.setTrade_status(trade_status);
        payStatusDto.setApp_id(APP_ID);
        payStatusDto.setTrade_no(trade_no);
        payStatusDto.setTotal_amount(total_amount);
        return payStatusDto;

    }

    /**
     * 保存订单信息
     */
    public XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto){
        //插入订单
        //进行幂等判断，同一个选课记录最多只能有一个订单记录
        XcOrders order = getOrderByBusinessId(addOrderDto.getOutBusinessId());
        if(order!=null)
            return order;
        order=new XcOrders();
        //生成订单号
        long orderId = IdWorkerUtils.getInstance().nextId();
        order.setId(orderId);
        order.setTotalPrice(addOrderDto.getTotalPrice());
        order.setCreateDate(LocalDateTime.now());
        order.setStatus("600001");//未支付
        order.setUserId(userId);
        order.setOrderType(addOrderDto.getOrderType());
        order.setOrderName(addOrderDto.getOrderName());
        order.setOrderDetail(addOrderDto.getOrderDetail());
        order.setOrderDescrip(addOrderDto.getOrderDescrip());
        order.setOutBusinessId(addOrderDto.getOutBusinessId());//选课记录id
        ordersMapper.insert(order);
        String orderDetailJson = addOrderDto.getOrderDetail();
        List<XcOrdersGoods> xcOrdersGoodsList = JSON.parseArray(orderDetailJson, XcOrdersGoods.class);
        xcOrdersGoodsList.forEach(goods->{
            XcOrdersGoods xcOrdersGoods = new XcOrdersGoods();
            BeanUtils.copyProperties(goods,xcOrdersGoods);
            xcOrdersGoods.setOrderId(orderId);//订单号
            ordersGoodsMapper.insert(xcOrdersGoods);
        });
        return order;
    }

    /***
     * 根据业务id查询订单
     */
    public XcOrders getOrderByBusinessId(String businessId) {
        XcOrders orders = ordersMapper.selectOne(new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getOutBusinessId, businessId));
        return orders;
    }

    /***
     * 根据订单生成支付记录
     */
    public XcPayRecord createPayRecord(XcOrders orders){
        if(orders==null){
            ZHXYException.cast("订单不存在");
        }
        if(orders.getStatus().equals("600002")){
            ZHXYException.cast("订单已支付");
        }
        XcPayRecord payRecord = new XcPayRecord();
        //生成支付交易流水号
        long payNo = IdWorkerUtils.getInstance().nextId();
        payRecord.setPayNo(payNo);
        payRecord.setOrderId(orders.getId()); //商品订单号
        payRecord.setOrderName(orders.getOrderName());
        payRecord.setTotalPrice(orders.getTotalPrice());
        payRecord.setCurrency("CNY");
        payRecord.setCreateDate(LocalDateTime.now());
        payRecord.setStatus("601001"); //未支付
        payRecord.setUserId(orders.getUserId());
        payRecordMapper.insert(payRecord);
        return payRecord;

    }
}
