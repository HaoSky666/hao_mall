package hao.you.mall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import hao.you.mall.bean.*;
import hao.you.mall.service.SkuService;
import hao.you.mall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {
    @Reference
    SkuService skuService;

    @Reference
    SpuService spuService;

    @RequestMapping(value = "{skuId}.html", method = RequestMethod.GET)
    public String item(@PathVariable String skuId, ModelMap modelMap) {
//        PmsSkuInfo skuInfo = skuService.getSkuById(skuId);
//        skuInfo.setSkuImageList(skuService.getSkuImage(skuId));
        PmsSkuInfo skuInfo = skuService.getSkuByIdFromRedis(skuId);
        List<PmsProductSaleAttr> pmsProductSaleAttrList = spuService.spuSaleAttrListCheckBySku(skuInfo.getSpuId(), skuInfo.getId());
        modelMap.put("skuInfo", skuInfo);
        modelMap.put("spuSaleAttrListCheckBySku", pmsProductSaleAttrList);

        //查询当前sku所属spu的其余sku集合hash表
        List<PmsSkuInfo> pmsSkuInfoList = skuService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());
        Map<String, String> hashMap = new HashMap<String, String>();
        for (PmsSkuInfo p : pmsSkuInfoList) {
            //"属性值1|属性值2……"
            String k = "";
            //v:skuId
            String v = String.valueOf(p.getId());
            List<PmsSkuSaleAttrValue> skuAttrValueList = p.getSkuSaleAttrValueList();
            for (PmsSkuSaleAttrValue sav : skuAttrValueList) {
                k += String.valueOf(sav.getSaleAttrValueId()) + "|";
            }
            hashMap.put(k, v);
        }

        //将销售属性放到页面
        String skuSaleAttrHashJsonStr = JSON.toJSONString(hashMap);
        modelMap.put("skuSaleAttrHashJsonStr", skuSaleAttrHashJsonStr);

        return "item";
    }

    @RequestMapping(value = "test", method = RequestMethod.GET)
    @ResponseBody
    public String testRedis(@RequestParam("skuId") String skuId) {
        PmsSkuInfo skuByIdFromRedis = skuService.getSkuByIdFromRedis(skuId);
        return "success";
    }
}
