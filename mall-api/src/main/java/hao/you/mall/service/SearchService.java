package hao.you.mall.service;

import hao.you.mall.bean.PmsSearchParam;
import hao.you.mall.bean.PmsSearchSkuInfo;

import java.util.List;

public interface SearchService {

    List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam);

}
