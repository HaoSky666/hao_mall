package hao.you.mall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import hao.you.mall.bean.OmsOrder;
import hao.you.mall.bean.OmsOrderItem;
import hao.you.mall.common.Constants;
import hao.you.mall.order.mapper.OmsOrderItemMapper;
import hao.you.mall.order.mapper.OmsOrderMapper;
import hao.you.mall.service.OrderService;
import hao.you.mall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.Collections;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OmsOrderMapper omsOrderMapper;

    @Autowired
    private OmsOrderItemMapper omsOrderItemMapper;

    @Override
    public String genTradeCode(String memberId) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String tradeKey = Constants.user + memberId + Constants.tradeCode;
            String code = UUID.randomUUID().toString();
            jedis.setex(tradeKey, 60 * 15, code);
            return code;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
        return null;
    }

    @Override
    public boolean checkTradeCode(String memberId, String tradeCode) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String key = Constants.user + memberId + Constants.tradeCode;
            //String code = jedis.get(key);
            // 使用lua脚本在发现key的同时将key删除，防止并发订单攻击,产生多个订单
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long eval = (Long) jedis.eval(script, Collections.singletonList(key), Collections.singletonList(tradeCode));
            if (eval != null && eval != 0) {
                //jedis.del(key);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
        return false;
    }

    @Override
    public void saveOrder(OmsOrder omsOrder) {
        omsOrderMapper.insertSelective(omsOrder);
        String id = omsOrder.getId();
        for (OmsOrderItem omsOrderItem : omsOrder.getOmsOrderItems()) {
            omsOrderItem.setOrderId(id);
            omsOrderItemMapper.insertSelective(omsOrderItem);
            //删除购物车数据,重复测试，不实现
            //cartService.delCart(omsOrderItem.getProductId())
        }
    }

    @Override
    public OmsOrder getOrderByOutTradeNo(String outTradeNo) {
        Example e = new Example(OmsOrder.class);
        e.createCriteria().andEqualTo("orderSn", outTradeNo);
        OmsOrder omsOrder = omsOrderMapper.selectOneByExample(e);
        return omsOrder;
    }
}
