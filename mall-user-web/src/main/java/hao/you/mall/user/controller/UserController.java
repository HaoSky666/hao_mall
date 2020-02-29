package hao.you.mall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import hao.you.mall.bean.UmsMember;
import hao.you.mall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RestController
public class UserController {
    @Reference
    private UserService userService;

    // 查询user_member表所有信息
    @RequestMapping(value = "getAllUser")
    public List<UmsMember> getAllUser(){
        List<UmsMember> umsMemberLists = userService.getAllUser();
        return umsMemberLists;
    }

    // 增加一个user_member
    @RequestMapping(value = "addUser",method = RequestMethod.POST)
    public void addUser(@RequestBody UmsMember umsMember){
        userService.addUser(umsMember);
    }

    // 根据memberId删除user信息
    @RequestMapping(value = "deleteUserById")
    public void deleteUserById(@RequestParam String memberId){
        userService.deleteUserById(memberId);
    }

    // 根据memberID修改user信息
    @RequestMapping(value = "updateUserById",method = RequestMethod.POST)
    public void updateUserById(@RequestBody UmsMember umsMember){
        userService.updateUserById(umsMember);
    }
}
