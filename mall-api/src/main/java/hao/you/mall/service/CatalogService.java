package hao.you.mall.service;

import hao.you.mall.bean.PmsBaseCatalog1;
import hao.you.mall.bean.PmsBaseCatalog2;
import hao.you.mall.bean.PmsBaseCatalog3;

import java.util.List;

public interface CatalogService {
    List<PmsBaseCatalog1> getCatalog1();

    List<PmsBaseCatalog2> getCatalog2ByCatalog1Id(String id);

    List<PmsBaseCatalog3> getCatalog3ByCatalog12Id(String id);
}
