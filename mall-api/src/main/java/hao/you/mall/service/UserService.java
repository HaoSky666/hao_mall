package hao.you.mall.service;

import hao.you.mall.bean.UmsMember;
import hao.you.mall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {

    List<UmsMember> getAllUser();

    UmsMember login(UmsMember umsMember);

    void addUser(UmsMember umsMember);

    void addUserToken(String id, String token);

    void deleteUserById(String memberId);

    void updateUserById(UmsMember umsMember);

    List<UmsMemberReceiveAddress> getAddressById(String memberId);

    UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId);
}
