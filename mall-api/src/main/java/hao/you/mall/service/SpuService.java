package hao.you.mall.service;

import hao.you.mall.bean.*;

import java.util.List;

public interface SpuService {
    List<PmsProductInfo> getSpuByCatalog3Id(String catalog3Id);

    List<PmsBaseSaleAttr> getBaseSaleAttrList();

    String addProductInfo(PmsProductInfo pmsProductInfo);

    String addProductSaleAttr(PmsProductSaleAttr pmsProductSaleAttr);

    void addProductSaleAttrValue(PmsProductSaleAttrValue pmsProductSaleAttrValue);

    List<PmsProductSaleAttr> getProductSaleAttrListBySpuId(String spuId);

    List<PmsProductImage> getProductImageBySpuId(String spuId);

    void addProductImage(PmsProductImage pmsProductImage);

    List<PmsProductSaleAttrValue> getProductSaleAttrValueListBySpuId(String spuId);

    List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String spuId, String skuId);
}
