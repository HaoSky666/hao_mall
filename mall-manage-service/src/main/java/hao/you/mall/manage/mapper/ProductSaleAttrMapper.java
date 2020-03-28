package hao.you.mall.manage.mapper;

import hao.you.mall.bean.PmsProductSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface ProductSaleAttrMapper extends Mapper<PmsProductSaleAttr> {
    List<PmsProductSaleAttr> getSpuSaleAttrListCheckBySku(@Param("productId") String productId, @Param("skuId") String skuId);
}
