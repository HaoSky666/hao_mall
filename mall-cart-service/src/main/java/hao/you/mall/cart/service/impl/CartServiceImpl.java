package hao.you.mall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import hao.you.mall.bean.OmsCartItem;
import hao.you.mall.cart.mapper.CartItemMapper;
import hao.you.mall.common.Constants;
import hao.you.mall.service.CartService;
import hao.you.mall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public OmsCartItem ifExistInDB(String memberId, String skuId) {
        Example e = new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("memberId", memberId).andEqualTo("productSkuId", skuId);
        OmsCartItem omsCartItem = cartItemMapper.selectOneByExample(e);

        return omsCartItem;
    }

    //添加购物车
    @Override
    public void addCart(OmsCartItem omsCartItem) {
        cartItemMapper.insertSelective(omsCartItem);
    }

    @Override
    public void flushCartCache(String memberId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem> cartItems = cartItemMapper.select(omsCartItem);
        //同步到缓存
        Jedis jedis = redisUtil.getJedis();
        Map<String, String> map = new HashMap<>();
        for (OmsCartItem cartItem : cartItems) {
            cartItem.setTotalPrice(cartItem.getPrice().multiply(new BigDecimal(cartItem.getQuantity())));
            map.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));
        }
        jedis.del(Constants.user + memberId + Constants.cart);
        jedis.hmset(Constants.user + memberId + Constants.cart, map);
        jedis.close();
    }

    @Override
    public void updateCart(OmsCartItem omsCartFromDB) {
        Example e = new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("id", omsCartFromDB.getId());
        cartItemMapper.updateByExampleSelective(omsCartFromDB, e);
    }

    //redis读取数据
    @Override
    public List<OmsCartItem> cartList(String memberId) {
        Jedis jedis = null;
        List<OmsCartItem> omsCartItemList = new ArrayList<>();
        try {
            jedis = redisUtil.getJedis();
            List<String> hvals = jedis.hvals(Constants.user + memberId + Constants.cart);
            for (String s : hvals) {
                omsCartItemList.add(JSON.parseObject(s, OmsCartItem.class));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            jedis.close();
        }
        return omsCartItemList;
    }

    @Override
    public void checkCart(OmsCartItem omsCartItem) {
        Example e = new Example(OmsCartItem.class);
        //getProductSkuId
        e.createCriteria().andEqualTo("memberId", omsCartItem.getMemberId()).andEqualTo("productSkuId", omsCartItem.getProductSkuId());
        cartItemMapper.updateByExampleSelective(omsCartItem, e);
        //刷新缓存
        flushCartCache(String.valueOf(omsCartItem.getMemberId()));
    }
}
