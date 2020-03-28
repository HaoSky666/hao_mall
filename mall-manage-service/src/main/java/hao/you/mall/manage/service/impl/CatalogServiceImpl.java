package hao.you.mall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import hao.you.mall.bean.PmsBaseCatalog1;
import hao.you.mall.bean.PmsBaseCatalog2;
import hao.you.mall.bean.PmsBaseCatalog3;
import hao.you.mall.manage.mapper.BaseCatalog1Mapper;
import hao.you.mall.manage.mapper.BaseCatalog2Mapper;
import hao.you.mall.manage.mapper.BaseCatalog3Mapper;
import hao.you.mall.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class CatalogServiceImpl implements CatalogService {
    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    BaseCatalog3Mapper catalog3Mapper;

    @Override
    public List<PmsBaseCatalog1> getCatalog1() {
        List<PmsBaseCatalog1> pmsBaseCatalog1s = baseCatalog1Mapper.selectAll();
        return pmsBaseCatalog1s;
    }

    @Override
    public List<PmsBaseCatalog2> getCatalog2ByCatalog1Id(String id) {
        PmsBaseCatalog2 pmsBaseCatalog2 = new PmsBaseCatalog2();
        pmsBaseCatalog2.setCatalog1Id(id);
        List<PmsBaseCatalog2> pmsBaseCatalog2s = baseCatalog2Mapper.select(pmsBaseCatalog2);
        return pmsBaseCatalog2s;
    }

    @Override
    public List<PmsBaseCatalog3> getCatalog3ByCatalog12Id(String id) {
        PmsBaseCatalog3 pmsBaseCatalog3 = new PmsBaseCatalog3();
        pmsBaseCatalog3.setCatalog2Id(id);
        List<PmsBaseCatalog3> pmsBaseCatalog3s = catalog3Mapper.select(pmsBaseCatalog3);
        return pmsBaseCatalog3s;
    }


}
