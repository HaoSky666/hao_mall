package hao.you.mall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import hao.you.mall.bean.UmsMemberReceiveAddress;
import hao.you.mall.service.UserAddressService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserAddressController {
    @Reference
    private UserAddressService userAddressService;

    // 根据member_id查询用户地址信息
    @RequestMapping("getUserMemberAddress")
    @ResponseBody
    public List<UmsMemberReceiveAddress> getUserMemberAddress(String memberId){
        List<UmsMemberReceiveAddress> userMemberAddress = userAddressService.getUserMemberAddressByMemberId(memberId);
        return userMemberAddress;
    }

}
