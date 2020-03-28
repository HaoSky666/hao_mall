package hao.you.mall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import hao.you.mall.bean.*;
import hao.you.mall.common.Constants;
import hao.you.mall.manage.mapper.SkuAttrValueMapper;
import hao.you.mall.manage.mapper.SkuImageMapper;
import hao.you.mall.manage.mapper.SkuInfoMapper;
import hao.you.mall.manage.mapper.SkuSaleAttrValueMapper;
import hao.you.mall.service.SkuService;
import hao.you.mall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public String addSkuInfo(PmsSkuInfo pmsSkuInfo) {
        skuInfoMapper.insertSelective(pmsSkuInfo);
        return pmsSkuInfo.getId();
    }

    @Override
    public void addSkuImage(PmsSkuImage pmsSkuImage) {
        skuImageMapper.insertSelective(pmsSkuImage);
    }

    @Override
    public void addSkuAttrValue(PmsSkuAttrValue pmsSkuAttrValue) {
        skuAttrValueMapper.insertSelective(pmsSkuAttrValue);
    }

    @Override
    public void addSkuSaleAttrValue(PmsSkuSaleAttrValue pmsSkuSaleAttrValue) {
        skuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
    }

    //不采用缓存
    public PmsSkuInfo getSkuAndImageByIdFromDB(String skuId) {
        PmsSkuInfo skuInfoList = null;
        try {
            skuInfoList = skuInfoMapper.selectByPrimaryKey(skuId);
            PmsSkuImage pmsSkuImage = new PmsSkuImage();
            pmsSkuImage.setSkuId(skuId);
            List<PmsSkuImage> pmsSkuImages = skuImageMapper.select(pmsSkuImage);
            skuInfoList.setSkuImageList(pmsSkuImages);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return skuInfoList;
    }

    // 使用redis但未加锁
    public PmsSkuInfo getSkuByIdFromRedisNotLock(String skuId) {
        PmsSkuInfo skuInfo = null;
        //连接缓存
        Jedis jedis = redisUtil.getJedis();
        //查询缓存
        String skuKey = Constants.sku + skuId + Constants.info;
        String skuJson = jedis.get(skuKey);

        if (StringUtils.isNotBlank(skuJson)) {
            skuInfo = JSON.parseObject(skuJson, PmsSkuInfo.class);
        } else {
            skuInfo = getSkuAndImageByIdFromDB(skuId);
            //保存到redis
            if (skuInfo != null) {
                jedis.set(skuKey, JSON.toJSONString(skuInfo));
            } else {
                // 解决缓存穿透
                jedis.set(skuKey, JSON.toJSONString(""));
            }
        }
        jedis.close();

        return skuInfo;
    }

    // redis设置分布式锁
    @Override
    public PmsSkuInfo getSkuByIdFromRedis(String skuId) {
        PmsSkuInfo skuInfo = null;
        //链接缓存
        Jedis jedis = redisUtil.getJedis();
        //查询缓存
        String key = Constants.sku + skuId + Constants.info;
        String skuJson = jedis.get(key);
        if (StringUtils.isBlank(skuJson)) {
            //设置分布式锁:锁key,防止高并发访问
            String lockKey = Constants.sku + skuId + Constants.lock;
            String token = UUID.randomUUID().toString();
            String OK = jedis.set(lockKey, token, "nx", "px", 1000 * 10);
            //成功加锁
            if (StringUtils.isNotBlank(OK) && OK.equals("OK")) {
                //访问数据库
                skuInfo = getSkuAndImageByIdFromDB(skuId);
                if (skuInfo != null) {
                    jedis.set(key, JSON.toJSONString(skuInfo));
                } else {
                    //防止穿透,为查找到设置空串存储
                    jedis.setex(key, 60 * 3, JSON.toJSONString(""));
                }
                // 访问DB后释放锁
                // 使用rua脚本
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEY[1]) else return 0 end";
                jedis.eval(script, Collections.singletonList("lock"), Collections.singletonList(token));
//                String lockToken = jedis.get(lockKey);
//                if (StringUtils.isNotBlank(lockToken) && lockToken.equals(token)) {
//                    jedis.del(lockKey);
//                }
            } else {
                //自旋，睡眠一段时间
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //return不会创建新的线程
                return getSkuById(skuId);
            }
        } else {
            skuInfo = JSON.parseObject(skuJson, PmsSkuInfo.class);
        }
        jedis.close();
        return skuInfo;
    }

    @Override
    public PmsSkuInfo getSkuById(String skuId) {
        Example e = new Example(PmsSkuInfo.class);
        Example.Criteria criteria = e.createCriteria();
        criteria.andEqualTo("id", skuId);
        PmsSkuInfo skuInfo = skuInfoMapper.selectOneByExample(e);
        return skuInfo;
    }

    @Override
    public List<PmsSkuInfo> getAllSku() {
        List<PmsSkuInfo> pmsSkuInfoList = skuInfoMapper.selectAll();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());
            List<PmsSkuAttrValue> select = skuAttrValueMapper.select(pmsSkuAttrValue);
            pmsSkuInfo.setSkuAttrValueList(select);
        }
        return pmsSkuInfoList;
    }

    @Override
    public List<PmsSkuImage> getSkuImage(String skuId) {
        Example e = new Example(PmsSkuImage.class);
        Example.Criteria criteria = e.createCriteria();
        criteria.andEqualTo("skuId", skuId);
        List<PmsSkuImage> pmsSkuImageList = skuImageMapper.selectByExample(e);
        return pmsSkuImageList;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String spuId) {
        List<PmsSkuInfo> pmsSkuInfoList = skuInfoMapper.selectSkuSaleAttrValueListBySpu(spuId);
        return pmsSkuInfoList;
    }

}
