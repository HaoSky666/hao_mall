package hao.you.mall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import hao.you.mall.bean.*;
import hao.you.mall.manage.mapper.*;
import hao.you.mall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class SpuServiceImpl implements SpuService {
    @Autowired
    ProductInfoMapper productInfoMapper;
    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    ProductSaleAttrMapper productSaleAttrMapper;
    @Autowired
    ProductSaleAttrValueMapper productSaleAttrValueMapper;
    @Autowired
    ProductImageMapper productImageMapper;

    @Override
    public List<PmsProductInfo> getSpuByCatalog3Id(String catalog3Id) {
        Example e = new Example(PmsProductInfo.class);
        Example.Criteria criteria = e.createCriteria();
        criteria.andEqualTo("catalog3Id",catalog3Id);
        List<PmsProductInfo> pmsProductInfoList = productInfoMapper.selectByExample(e);
        return pmsProductInfoList;
    }

    @Override
    public List<PmsBaseSaleAttr> getBaseSaleAttrList() {
        List<PmsBaseSaleAttr> pmsBaseSaleAttrLists = baseSaleAttrMapper.selectAll();
        return pmsBaseSaleAttrLists;
    }

    @Override
    public String addProductInfo(PmsProductInfo pmsProductInfo) {
        productInfoMapper.insertSelective(pmsProductInfo);
        return pmsProductInfo.getId();
    }

    @Override
    public String addProductSaleAttr(PmsProductSaleAttr pmsProductSaleAttr) {
        productSaleAttrMapper.insertSelective(pmsProductSaleAttr);
        return pmsProductSaleAttr.getId();
    }

    @Override
    public void addProductSaleAttrValue(PmsProductSaleAttrValue pmsProductSaleAttrValue) {
        productSaleAttrValueMapper.insertSelective(pmsProductSaleAttrValue);
    }

    @Override
    public List<PmsProductSaleAttr> getProductSaleAttrListBySpuId(String spuId) {
        Example e = new Example(PmsProductSaleAttr.class);
        Example.Criteria criteria = e.createCriteria();
        criteria.andEqualTo("productId",spuId);
        List<PmsProductSaleAttr> pmsProductSaleAttrLists = productSaleAttrMapper.selectByExample(e);
        return pmsProductSaleAttrLists;
    }

    @Override
    public List<PmsProductImage> getProductImageBySpuId(String spuId) {
        Example e = new Example(PmsProductImage.class);
        Example.Criteria criteria = e.createCriteria();
        criteria.andEqualTo("productId",spuId);
        List<PmsProductImage> pmsProductImageLists = productImageMapper.selectByExample(e);
        return pmsProductImageLists;
    }

    @Override
    public void addProductImage(PmsProductImage pmsProductImage) {
         productImageMapper.insertSelective(pmsProductImage);
    }

    @Override
    public List<PmsProductSaleAttrValue> getProductSaleAttrValueListBySpuId(String spuId) {
        Example e = new Example(PmsProductSaleAttrValue.class);
        Example.Criteria criteria = e.createCriteria();
        criteria.andEqualTo("productId", spuId);
        List<PmsProductSaleAttrValue> pmsProductSaleAttrValues = productSaleAttrValueMapper.selectByExample(e);
        return pmsProductSaleAttrValues;
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String spuId, String skuId) {
        List<PmsProductSaleAttr> pmsProductSaleAttrList = productSaleAttrMapper.getSpuSaleAttrListCheckBySku(spuId, skuId);
        return pmsProductSaleAttrList;
    }
}
