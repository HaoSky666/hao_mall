package hao.you.mall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import hao.you.mall.annotations.LoginRequired;
import hao.you.mall.bean.OmsCartItem;
import hao.you.mall.bean.OmsOrder;
import hao.you.mall.bean.OmsOrderItem;
import hao.you.mall.bean.UmsMemberReceiveAddress;
import hao.you.mall.common.Constants;
import hao.you.mall.service.CartService;
import hao.you.mall.service.OrderService;
import hao.you.mall.service.SkuService;
import hao.you.mall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    private OrderService orderService;

    @Reference
    private UserService userService;

    @Reference
    private CartService cartService;

    @Reference
    private SkuService skuService;

    //有重定向的方法，modelandview
    @RequestMapping("submitOrder")
    @LoginRequired(loginSuccess = true)
    public ModelAndView submitOrder(String receiveAddressId, BigDecimal totalAmount, String tradeCode, HttpServletRequest request, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        //验证交易码
        boolean b = orderService.checkTradeCode(memberId, tradeCode);
        if (b) {
            //订单物品集合
            List<OmsOrderItem> omsOrderItems = new ArrayList<>();
            // 订单对象
            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setAutoConfirmDay(7);
            omsOrder.setCreateTime(new Date());
            omsOrder.setDiscountAmount(null);
            //omsOrder.setFreightAmount(); 运费，支付后，在生成物流信息时
            omsOrder.setMemberId(memberId);
            omsOrder.setMemberUsername(nickname);
            omsOrder.setNote("快点发货");
            String outTradeNo = "mall";
            outTradeNo = outTradeNo + System.currentTimeMillis();// 将毫秒时间戳拼接到外部订单号
            SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMDDHHmmss");
            outTradeNo = outTradeNo + sdf.format(new Date());// 将时间字符串拼接到外部订单号

            omsOrder.setOrderSn(outTradeNo);//外部订单号
            omsOrder.setPayAmount(totalAmount);
            omsOrder.setOrderType(1);
            UmsMemberReceiveAddress umsMemberReceiveAddress = userService.getReceiveAddressById(receiveAddressId);
            omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
            omsOrder.setReceiverPostCode(umsMemberReceiveAddress.getPostCode());
            omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
            omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());
            // 当前日期加一天，一天后配送
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, 1);
            Date time = c.getTime();
            omsOrder.setReceiveTime(time);
            omsOrder.setSourceType(0);
            omsOrder.setStatus(0);
            omsOrder.setOrderType(0);
            omsOrder.setTotalAmount(totalAmount);

            // 根据用户id获得要购买的商品列表(购物车)，和总价格
            List<OmsCartItem> omsCartItems = cartService.cartList(memberId);

            for (OmsCartItem cartItem : omsCartItems) {
                if (cartItem.getIsChecked().equals("1")) {
                    OmsOrderItem omsOrderItem = new OmsOrderItem();
                    //检价
                    boolean flag = skuService.checkPrice(cartItem.getProductSkuId(), cartItem.getPrice());
                    if (!flag) {
                        ModelAndView mv = new ModelAndView("tradeFail");
                        return mv;
                    }
                    //验库存
                    omsOrderItem.setProductPic(cartItem.getProductPic());
                    omsOrderItem.setProductName(cartItem.getProductName());
                    omsOrderItem.setOrderSn(outTradeNo);// 外部订单号，用来和其他系统进行交互，防止重复
                    omsOrderItem.setProductCategoryId(cartItem.getProductCategoryId());
                    omsOrderItem.setProductPrice(cartItem.getPrice());
                    omsOrderItem.setRealAmount(cartItem.getTotalPrice());
                    omsOrderItem.setProductQuantity(cartItem.getQuantity());
                    omsOrderItem.setProductSkuCode("111111111111");
                    omsOrderItem.setProductSkuId(cartItem.getProductSkuId());
                    omsOrderItem.setProductId(cartItem.getProductId());
                    omsOrderItem.setProductSn("仓库对应的商品编号");// 在仓库中的skuId

                    omsOrderItems.add(omsOrderItem);
                }
            }
            omsOrder.setOmsOrderItems(omsOrderItems);

            //存入订单信息
            // 删除购物车物品
            orderService.saveOrder(omsOrder);

            //重定向到支付系统
            ModelAndView mv = new ModelAndView("redirect:http://payment.mall.com:8087/index");
            //可以不同传递这些信息
            mv.addObject("outTradeNo", outTradeNo);
            mv.addObject("totalAmount", totalAmount);
            return mv;
        } else {
            ModelAndView mv = new ModelAndView("tradeFail");
            return mv;
        }
    }

    @RequestMapping("toTrade")
    @LoginRequired(loginSuccess = true)
    public String toTrade(HttpServletRequest request, ModelMap modelMap) {
        String memberId = (String) request.getAttribute(Constants.memberId);
        String nickName = (String) request.getAttribute(Constants.nickname);

        //收货地址
        List<UmsMemberReceiveAddress> addressList = null;
        addressList = userService.getAddressById(memberId);
        //获取购物车列表
        List<OmsCartItem> cartItemList = cartService.cartList(memberId);
        //将购物车列表转化为订单列表
        List<OmsOrderItem> orderItemList = new ArrayList<>();
        for (OmsCartItem cartItem : cartItemList) {
            //购物车选中
            if (cartItem.getIsChecked().equals("1")) {
                OmsOrderItem item = new OmsOrderItem();
                item.setProductPic(cartItem.getProductPic());
                item.setProductName(cartItem.getProductName());
                orderItemList.add(item);
            }
        }
        BigDecimal totalAmount = getTotalPrice(cartItemList);
        modelMap.put("userAddressList", addressList);
        modelMap.put("omsOrderItems", orderItemList);
        modelMap.put("totalAmount", totalAmount);
        modelMap.put("nickName", nickName);

        //生成交易码，进行提交订单时的校验
        String tradeCode = orderService.genTradeCode(memberId);
        modelMap.put("tradeCode", tradeCode);

        return "trade";
    }

    // 返回我的订单页面
    @RequestMapping(value = "toOrderList", method = RequestMethod.GET)
    @LoginRequired(loginSuccess = true)
    public String toOrderList(){
        return "list";
    }

    private BigDecimal getTotalPrice(List<OmsCartItem> omsCartItemList) {
        BigDecimal totalPrice = new BigDecimal(0);
        for (OmsCartItem omsCartItem : omsCartItemList) {
            //被选中
            if (omsCartItem.getIsChecked().equals("1")) {
                totalPrice = totalPrice.add(omsCartItem.getTotalPrice());
            }
        }
        return totalPrice;
    }

}
