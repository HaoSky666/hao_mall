package hao.you.mall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import hao.you.mall.bean.PmsBaseAttrInfo;
import hao.you.mall.bean.PmsBaseAttrValue;
import hao.you.mall.manage.mapper.BaseAttrInfoMapper;
import hao.you.mall.manage.mapper.BaseAttrValueMapper;
import hao.you.mall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Set;

@Service
public class AttrServiceImpl implements AttrService {
    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Override
    public List<PmsBaseAttrInfo> getAttrInfoListByCateLog3Id(String catalog3Id) {
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> attrInfoSelect = baseAttrInfoMapper.select(pmsBaseAttrInfo);
        return attrInfoSelect;
    }

    @Override
    public List<PmsBaseAttrValue> getAttrValueListByInfoId(String id) {
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(id);
        List<PmsBaseAttrValue> pmsBaseAttrValues = baseAttrValueMapper.select(pmsBaseAttrValue);
        return pmsBaseAttrValues;
    }

    @Override
    public List<PmsBaseAttrInfo> getAttrValueListByValueId(Set<String> valueIdSet) {
        //拼接字符串
        String valueIdStr = StringUtils.join(valueIdSet, ",");
        List<PmsBaseAttrInfo> attrInfos = baseAttrInfoMapper.selectAttrValueListByValueId(valueIdStr);
        return attrInfos;
    }

    @Override
    public String addAttrInfoList(PmsBaseAttrInfo pmsBaseAttrInfo) {
        baseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);// insertSelective null值不插入数据库
        return pmsBaseAttrInfo.getId();
    }

    @Override
    public Integer addAttrValueList(PmsBaseAttrValue pmsBaseAttrValue) {
        int insert = baseAttrValueMapper.insertSelective(pmsBaseAttrValue);
        return insert;
    }

    @Override
    public Integer updateAttrInfoById(PmsBaseAttrInfo pmsBaseAttrInfo) {
        int update = baseAttrInfoMapper.updateByPrimaryKey(pmsBaseAttrInfo);
        return update;
    }

    @Override
    public Integer updateAttrValueById(PmsBaseAttrValue pmsBaseAttrValue) {
        int update = baseAttrValueMapper.updateByPrimaryKey(pmsBaseAttrValue);
        return update;
    }

    @Override
    public Integer deleteAttrValue(String attrInfoId) {
        Example example = new Example(PmsBaseAttrValue.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("attrId", attrInfoId);
        int deleteByExample = baseAttrValueMapper.deleteByExample(example);
        return deleteByExample;
    }
}
