package hao.you.mall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import hao.you.mall.bean.UmsMember;
import hao.you.mall.bean.UmsMemberReceiveAddress;
import hao.you.mall.common.Constants;
import hao.you.mall.service.UserService;
import hao.you.mall.user.mapper.UmsMemberReceiveAddressMapper;
import hao.you.mall.user.mapper.UserMapper;
import hao.you.mall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Override
    public List<UmsMember> getAllUser() {
        List<UmsMember> umsMemberList = userMapper.selectAll();
        return umsMemberList;
    }

    @Override
    public UmsMember login(UmsMember umsMember) {
        Jedis jedis = null;
        StringBuffer sb = new StringBuffer();
        sb.append(Constants.user);
        //redis数据结构要改，此为偷懒的方法
        sb.append(umsMember.getPassword());
        sb.append(umsMember.getUsername());
        sb.append(Constants.info);
        try {
            jedis = redisUtil.getJedis();
            if (jedis != null) {
                String s = jedis.get(sb.toString());
                if (StringUtils.isNotBlank(s)) {
                    //密码正确
                    UmsMember umsMember1 = JSON.parseObject(s, UmsMember.class);
                    //finally中语句会在return前执行
                    return umsMember1;
                }
            }
            //redis宕机
            //缓存没有，查询数据库
            //分布式锁
            UmsMember umsMember1 = loginFromDB(umsMember);
            if (umsMember1 != null) {
                jedis.setex(sb.toString(), 60 * 60 * 60, JSON.toJSONString(umsMember1));
                return umsMember1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
        return null;
    }

    @Override
    public void addUser(UmsMember umsMember) {
        userMapper.insert(umsMember);
    }

    @Override
    public void addUserToken(String id, String token) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            StringBuffer sb = new StringBuffer();
            sb.append(Constants.user);
            sb.append(id);
            sb.append(Constants.token);
            jedis.setex(sb.toString(), 60 * 60 * 2, token);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
    }

    @Override
    public void deleteUserById(String memberId) {
        UmsMember umsMember = new UmsMember();
        umsMember.setId(memberId);
        userMapper.delete(umsMember);
    }

    @Override
    public void updateUserById(UmsMember umsMember) {
        userMapper.updateByPrimaryKey(umsMember);
    }

    @Override
    public List<UmsMemberReceiveAddress> getAddressById(String memberId) {
        Example e = new Example(UmsMemberReceiveAddress.class);
        e.createCriteria().andEqualTo("id", memberId);
        List<UmsMemberReceiveAddress> addressList = umsMemberReceiveAddressMapper.selectByExample(e);
        return addressList;
    }

    @Override
    public UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setId(receiveAddressId);
        UmsMemberReceiveAddress umsMemberReceiveAddress1 = umsMemberReceiveAddressMapper.selectOne(umsMemberReceiveAddress);
        return umsMemberReceiveAddress1;
    }

    private UmsMember loginFromDB(UmsMember umsMember) {
        Example e = new Example(UmsMember.class);
        e.createCriteria().andEqualTo("username", umsMember.getUsername()).andEqualTo("password", umsMember.getPassword());
        UmsMember umsMember1 = userMapper.selectOneByExample(e);
        return umsMember1;
    }
}
