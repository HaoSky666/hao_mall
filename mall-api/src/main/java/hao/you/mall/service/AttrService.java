package hao.you.mall.service;

import hao.you.mall.bean.PmsBaseAttrInfo;
import hao.you.mall.bean.PmsBaseAttrValue;

import java.util.List;
import java.util.Set;

public interface AttrService {
    List<PmsBaseAttrInfo> getAttrInfoListByCateLog3Id(String catalog3Id);

    List<PmsBaseAttrValue> getAttrValueListByInfoId(String id);

    List<PmsBaseAttrInfo> getAttrValueListByValueId(Set<String> valueIdSet);

    String addAttrInfoList(PmsBaseAttrInfo pmsBaseAttrInfo);

    Integer addAttrValueList(PmsBaseAttrValue pmsBaseAttrValue);

    Integer updateAttrInfoById(PmsBaseAttrInfo pmsBaseAttrInfo);

    Integer updateAttrValueById(PmsBaseAttrValue pmsBaseAttrValue);

    Integer deleteAttrValue(String attrInfoId);
}
