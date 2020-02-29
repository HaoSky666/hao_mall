package hao.you.mall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import hao.you.mall.bean.UmsMember;
import hao.you.mall.service.UserService;
import hao.you.mall.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;

    @Override
    public List<UmsMember> getAllUser() {
        List<UmsMember> umsMemberList = userMapper.selectAll();
        return umsMemberList;
    }

    @Override
    public void addUser(UmsMember umsMember) {
        userMapper.insert(umsMember);
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
}
