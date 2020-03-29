package hao.you.mall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import hao.you.mall.bean.OmsCartItem;
import hao.you.mall.bean.PmsSkuInfo;
import hao.you.mall.common.Constants;
import hao.you.mall.service.CartService;
import hao.you.mall.service.SkuService;
import hao.you.mall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {

    @Reference
    SkuService skuService;

    @Reference
    CartService cartService;

    //交易界面
    @RequestMapping("toTrade")
//    @LoginRequired(loginSuccess = true)
    public String toTrade(HttpServletResponse response, HttpServletRequest request, ModelMap modelMap) {
        //使用强转，防止产生 null
        String memberId = (String) request.getAttribute(Constants.memberId);
        String nickname = (String) request.getAttribute(Constants.nickname);

        return "toTrade";
    }


    @RequestMapping("cartList")
//    @LoginRequired(loginSuccess = false)
    public String cartList(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        List<OmsCartItem> omsCartItemList = new ArrayList<>();
        String memberId = (String) request.getAttribute(Constants.memberId);
        if (StringUtils.isNotBlank(memberId)) {
            omsCartItemList = cartService.cartList(memberId);
        } else {
            //查询cookies
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)) {
                omsCartItemList = JSON.parseArray(cartListCookie, OmsCartItem.class);
            }
        }
        modelMap.put("cartList", omsCartItemList);
//        //被勾选商品总额
        BigDecimal totalAmount = getTotalPrice(omsCartItemList);
        modelMap.put("totalAmount", totalAmount);
        return "cartList";
    }

    //购物车添加数量功能，需要修改前端页面
    @RequestMapping("checkCart")
//    @LoginRequired(loginSuccess = false)
    public String checkCart(String isChecked, String skuId, HttpServletRequest request,HttpServletResponse response, ModelMap modelMap) {
        String memberId = (String) request.getAttribute(Constants.memberId);
        List<OmsCartItem> omsCartItemList = null;
        if (StringUtils.isNotBlank(memberId)) {
            //对购物车信息更新
            OmsCartItem omsCartItem = new OmsCartItem();
            omsCartItem.setIsChecked(isChecked);
            omsCartItem.setProductSkuId(skuId);
            omsCartItem.setMemberId(memberId);
            cartService.checkCart(omsCartItem);
            //将最新数据从缓存中取出
            omsCartItemList = cartService.cartList(memberId);
        } else {
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            omsCartItemList = JSON.parseArray(cartListCookie, OmsCartItem.class);
            for (OmsCartItem omsCartItem : omsCartItemList) {
                if(skuId.equals(omsCartItem.getProductSkuId())){
                    omsCartItem.setIsChecked(isChecked);
                }
            }
            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItemList), 60 * 60 * 72, true);
        }
        modelMap.put("cartList", omsCartItemList);
        //被勾选商品总额
        BigDecimal totalAmount = getTotalPrice(omsCartItemList);
        modelMap.put("totalAmount", totalAmount);
        //返回内嵌页
        return "cartListInner";
    }

    @RequestMapping("addToCart")
//    @LoginRequired(loginSuccess = false)
    public String addToCart(String skuId, int quantity, HttpServletRequest request, HttpServletResponse response) {
        String memberId = (String) request.getAttribute(Constants.memberId);
        //购物车列表
        List<OmsCartItem> omsCartItemList = new ArrayList<>();
        //调用商品服务查询商品信息
        PmsSkuInfo skuInfo = skuService.getSkuByIdFromRedis(skuId);

        //将商品信息封装成购物车信息
        OmsCartItem omsCartItem = new OmsCartItem();
        buildCartItem(skuInfo, omsCartItem, quantity);

        if (StringUtils.isNotBlank(memberId)) {
            //登录使用DB+Redis
            //用户登录，查询购物车数据
            OmsCartItem omsCartFromDB = cartService.ifExistInDB(memberId, skuId);
            if (omsCartFromDB != null) {
                omsCartFromDB.setQuantity(omsCartFromDB.getQuantity() + quantity);
                cartService.updateCart(omsCartFromDB);
            } else {
                omsCartItem.setMemberId(memberId);
                omsCartItem.setMemberNickname((String) request.getAttribute(Constants.nickname));
                omsCartItem.setQuantity(quantity);
                cartService.addCart(omsCartItem);
            }
            // 同步缓存
            cartService.flushCartCache(memberId);
        } else {
            //未登录使用cookies
            //cookie原有数据
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            //cookie不为空
            if (StringUtils.isNotBlank(cartListCookie)) {
                // omsCartItemList cookie中数据
                omsCartItemList = JSON.parseArray(cartListCookie, OmsCartItem.class);
                boolean exist = if_exist(omsCartItemList, omsCartItem);
                if (exist) {
                    for (OmsCartItem omsCartItem1 : omsCartItemList) {
                        if (omsCartItem1.getProductSkuId().equals(skuId)) {
                            omsCartItem1.setQuantity(omsCartItem1.getQuantity() + quantity);
                            omsCartItem1.setTotalPrice(omsCartItem1.getPrice().multiply(new BigDecimal(omsCartItem1.getQuantity())));
                        }
                    }
                } else {
                    omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(new BigDecimal(quantity)));
                    omsCartItemList.add(omsCartItem);
                }
            } else {
                omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(new BigDecimal(quantity)));
                omsCartItemList.add(omsCartItem);
            }

            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItemList), 60 * 60 * 72, true);
        }

        //重定向的静态页面
        return "redirect:/success.html";
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

    private boolean if_exist(List<OmsCartItem> omsCartItemList, OmsCartItem omsCartItem) {
        if (omsCartItemList != null && omsCartItem != null) {
            for (OmsCartItem omsCartItem1 : omsCartItemList) {
                //存在
                if (omsCartItem1.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void buildCartItem(PmsSkuInfo skuInfo, OmsCartItem omsCartItem, Integer quantity) {
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setProductId(skuInfo.getSpuId());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("");
        omsCartItem.setProductSkuId(skuInfo.getId());
        omsCartItem.setQuantity(quantity);
        omsCartItem.setIsChecked("1");
    }

}
