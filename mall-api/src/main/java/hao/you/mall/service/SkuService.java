package hao.you.mall.service;

import hao.you.mall.bean.PmsSkuAttrValue;
import hao.you.mall.bean.PmsSkuImage;
import hao.you.mall.bean.PmsSkuInfo;
import hao.you.mall.bean.PmsSkuSaleAttrValue;

import java.math.BigDecimal;
import java.util.List;

public interface SkuService {
    String addSkuInfo(PmsSkuInfo pmsSkuInfo);

    void addSkuImage(PmsSkuImage pmsSkuImage);

    void addSkuAttrValue(PmsSkuAttrValue pmsSkuAttrValue);

    void addSkuSaleAttrValue(PmsSkuSaleAttrValue pmsSkuSaleAttrValue);

    PmsSkuInfo getSkuById(String skuId);

    List<PmsSkuInfo> getAllSku();

    PmsSkuInfo getSkuByIdFromRedis(String skuId);

    List<PmsSkuImage> getSkuImage(String skuId);

    List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String spuId);

    boolean checkPrice(String productSkuId, BigDecimal price);
}
