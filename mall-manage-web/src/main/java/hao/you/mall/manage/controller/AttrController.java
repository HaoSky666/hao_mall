package hao.you.mall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import hao.you.mall.bean.PmsBaseAttrInfo;
import hao.you.mall.bean.PmsBaseAttrValue;
import hao.you.mall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class AttrController {
    @Reference
    AttrService attrService;

    // 根据三级目录查询属性名称
    @RequestMapping(value = "attrInfoList", method = RequestMethod.GET)
    public List<PmsBaseAttrInfo> getAttrInfoListByCateLog3Id(@RequestParam("catalog3Id") String catalog3Id) {
        List<PmsBaseAttrInfo> attrInfoListByCateLog3Id = attrService.getAttrInfoListByCateLog3Id(catalog3Id);
        for(PmsBaseAttrInfo pmsBaseAttrInfo : attrInfoListByCateLog3Id){
            List<PmsBaseAttrValue> attrValueListByInfoIds = attrService.getAttrValueListByInfoId(pmsBaseAttrInfo.getId());
            pmsBaseAttrInfo.setAttrValueList(attrValueListByInfoIds);
        }
        return attrInfoListByCateLog3Id;
    }

    // 根据属性名称id查询属性值
    @RequestMapping(value = "getAttrValueList", method = RequestMethod.POST)
    public List<PmsBaseAttrValue> getAttrValueListByInfoId(@RequestParam("attrId") String id) {
        List<PmsBaseAttrValue> attrValueListByInfoId = attrService.getAttrValueListByInfoId(id);
        return attrValueListByInfoId;
    }

    // 添加属性值
    @RequestMapping(value = "saveAttrInfo", method = RequestMethod.POST)
    public String saveAttr(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo) {

        String attrInfoId = pmsBaseAttrInfo.getId();

        if(StringUtils.isEmpty(attrInfoId)){
            String attrInfoIdAdd = attrService.addAttrInfoList(pmsBaseAttrInfo);
            for (PmsBaseAttrValue pmsBaseAttrValue : pmsBaseAttrInfo.getAttrValueList()) {
                pmsBaseAttrValue.setAttrId(attrInfoIdAdd);
                attrService.addAttrValueList(pmsBaseAttrValue);
            }
            return "insert success";
        }

        attrService.updateAttrInfoById(pmsBaseAttrInfo);

        attrService.deleteAttrValue(attrInfoId);
        for (PmsBaseAttrValue pmsBaseAttrValue : pmsBaseAttrInfo.getAttrValueList()) {
            pmsBaseAttrValue.setId(null);
            pmsBaseAttrValue.setAttrId(attrInfoId);
            attrService.addAttrValueList(pmsBaseAttrValue);
        }
        return "update success";
    }
}
