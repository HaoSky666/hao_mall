package hao.you.mall.service;

import hao.you.mall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserAddressService {
    List<UmsMemberReceiveAddress> getUserMemberAddressByMemberId(String memberId);
}
