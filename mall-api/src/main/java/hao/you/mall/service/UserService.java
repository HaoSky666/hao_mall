package hao.you.mall.service;

import hao.you.mall.bean.UmsMember;

import java.util.List;

public interface UserService {

    List<UmsMember> getAllUser();

    void addUser(UmsMember umsMember);

    void deleteUserById(String memberId);

    void updateUserById(UmsMember umsMember);
}
