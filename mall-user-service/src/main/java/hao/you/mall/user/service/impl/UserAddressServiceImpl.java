package hao.you.mall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import hao.you.mall.bean.UmsMemberReceiveAddress;
import hao.you.mall.service.UserAddressService;
import hao.you.mall.user.mapper.UserAdressMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserAddressServiceImpl implements UserAddressService {
    @Autowired
    UserAdressMapper userAdressMapper;

    @Override
    public List<UmsMemberReceiveAddress> getUserMemberAddressByMemberId(String memberId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);

        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = userAdressMapper.select(umsMemberReceiveAddress);
        return umsMemberReceiveAddresses;
    }
}
