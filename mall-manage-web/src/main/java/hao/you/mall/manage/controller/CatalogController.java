package hao.you.mall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import hao.you.mall.bean.PmsBaseCatalog1;
import hao.you.mall.bean.PmsBaseCatalog2;
import hao.you.mall.bean.PmsBaseCatalog3;
import hao.you.mall.service.CatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class CatalogController {

    @Reference
    CatalogService catalogService;

    @RequestMapping(value = "getCatalog1", method = RequestMethod.POST)
    public List<PmsBaseCatalog1> getCatalog1(){
        List<PmsBaseCatalog1> catalog1 = catalogService.getCatalog1();
        return catalog1;
    }

    @RequestMapping(value = "getCatalog2", method = RequestMethod.POST)
    public List<PmsBaseCatalog2> getCatalog2(@RequestParam("catalog1Id") String id){
        List<PmsBaseCatalog2> catalog2ByCatalog1Id = catalogService.getCatalog2ByCatalog1Id(id);
        return catalog2ByCatalog1Id;
    }

    @RequestMapping(value = "getCatalog3", method = RequestMethod.POST)
    public List<PmsBaseCatalog3> getCatalog3(@RequestParam("catalog2Id") String id){
        List<PmsBaseCatalog3> catalog3ByCatalog12Id = catalogService.getCatalog3ByCatalog12Id(id);
        return catalog3ByCatalog12Id;
    }

}
