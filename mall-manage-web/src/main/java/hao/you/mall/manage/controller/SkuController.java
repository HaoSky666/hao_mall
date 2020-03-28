package hao.you.mall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import hao.you.mall.bean.PmsSkuAttrValue;
import hao.you.mall.bean.PmsSkuImage;
import hao.you.mall.bean.PmsSkuInfo;
import hao.you.mall.bean.PmsSkuSaleAttrValue;
import hao.you.mall.service.SkuService;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class SkuController {
    @Reference
    SkuService skuService;

    @RequestMapping(value = "saveSkuInfo" , method = RequestMethod.POST)
    public String saveSkuInfo(@RequestBody PmsSkuInfo pmsSkuInfo){
        if(pmsSkuInfo != null){
            String skuInfoId = skuService.addSkuInfo(pmsSkuInfo);
            if(pmsSkuInfo.getSkuImageList() != null){
                for(PmsSkuImage pmsSkuImage : pmsSkuInfo.getSkuImageList()){
                    pmsSkuImage.setSkuId(skuInfoId);
                    skuService.addSkuImage(pmsSkuImage);
                }
            }
            if(pmsSkuInfo.getSkuAttrValueList() != null){
                for(PmsSkuAttrValue pmsSkuAttrValue : pmsSkuInfo.getSkuAttrValueList()){
                    pmsSkuAttrValue.setSkuId(skuInfoId);
                    skuService.addSkuAttrValue(pmsSkuAttrValue);
                }
            }
            if(pmsSkuInfo.getSkuSaleAttrValueList() != null){
                for(PmsSkuSaleAttrValue pmsSkuSaleAttrValue : pmsSkuInfo.getSkuSaleAttrValueList()){
                    pmsSkuSaleAttrValue.setSkuId(skuInfoId);
                    skuService.addSkuSaleAttrValue(pmsSkuSaleAttrValue);
                }
            }
            return "success";
        }
        return "fail";
    }
}
