package hao.you.mall.service;


import hao.you.mall.bean.OmsCartItem;

import java.util.List;

public interface CartService {

    OmsCartItem ifExistInDB(String memberId, String skuId);

    void addCart(OmsCartItem omsCartItem);

    void flushCartCache(String memberId);

    void updateCart(OmsCartItem omsCartFromDB);

    List<OmsCartItem> cartList(String memberId);

    void checkCart(OmsCartItem omsCartItem);
}
