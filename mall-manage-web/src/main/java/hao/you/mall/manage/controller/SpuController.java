package hao.you.mall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import hao.you.mall.bean.*;
import hao.you.mall.service.SpuService;
import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin
public class SpuController {
    @Reference
    SpuService spuService;

    @Value("${fileServer.url}")
    String fileUrl;

    // 根据三级目录获取product_info的值
    @RequestMapping(value = "spuList", method = RequestMethod.GET)
    public List<PmsProductInfo> spuList(@RequestParam("catalog3Id") String catalog3Id) {
        List<PmsProductInfo> pmsProductInfoLists = spuService.getSpuByCatalog3Id(catalog3Id);
        return pmsProductInfoLists;
    }

    // 获取base销售属性
    @RequestMapping(value = "baseSaleAttrList", method = RequestMethod.POST)
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        List<PmsBaseSaleAttr> pmsBaseSaleAttrLists = spuService.getBaseSaleAttrList();
        return pmsBaseSaleAttrLists;
    }

    // 根据spuId获取商品的销售属性
    @RequestMapping(value = "spuSaleAttrList", method = RequestMethod.GET)
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        List<PmsProductSaleAttr> pmsProductSaleAttrLists = spuService.getProductSaleAttrListBySpuId(spuId);
        for (PmsProductSaleAttr pmsProductSaleAttr : pmsProductSaleAttrLists) {
            List<PmsProductSaleAttrValue> pmsProductSaleAttrValues = spuService.getProductSaleAttrValueListBySpuId(spuId);
            pmsProductSaleAttr.setSpuSaleAttrValueList(pmsProductSaleAttrValues);
        }
        return pmsProductSaleAttrLists;
    }

    // 根据spuId获取商品的销售属性值
    public List<PmsProductSaleAttrValue> spuSaleAttrValueList(String spuId) {

        return null;
    }

    // 根据spuId获取商品的图片
    @RequestMapping(value = "spuImageList", method = RequestMethod.GET)
    public List<PmsProductImage> spuImageList(String spuId) {
        List<PmsProductImage> pmsProductImageLists = spuService.getProductImageBySpuId(spuId);
        return pmsProductImageLists;
    }

    // 添加spu及spu销售属性和图片
    @RequestMapping(value = "saveSpuInfo", method = RequestMethod.POST)
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo) {
        // 添加product_info
        String productInfoId = spuService.addProductInfo(pmsProductInfo);

        if (pmsProductInfo.getSpuSaleAttrList() != null) {
            // 添加spu销售属性及属性值
            for (PmsProductSaleAttr pmsProductSaleAttr : pmsProductInfo.getSpuSaleAttrList()) {
                pmsProductSaleAttr.setProductId(productInfoId);
                spuService.addProductSaleAttr(pmsProductSaleAttr);
                if (pmsProductSaleAttr.getSpuSaleAttrValueList() != null) {
                    for (PmsProductSaleAttrValue pmsProductSaleAttrValue : pmsProductSaleAttr.getSpuSaleAttrValueList()) {
                        pmsProductSaleAttrValue.setProductId(productInfoId);
                        spuService.addProductSaleAttrValue(pmsProductSaleAttrValue);
                    }
                }
            }
        }

        if (pmsProductInfo.getSpuImageList() != null) {
            // 添加图片属性
            for (PmsProductImage pmsProductImage : pmsProductInfo.getSpuImageList()) {
                pmsProductImage.setProductId(productInfoId);
                spuService.addProductImage(pmsProductImage);
            }
        }
        return "success";
    }

    // 图片上传
    @RequestMapping(value = "fileUpload", method = RequestMethod.POST)
    public String fileUpload(@RequestParam("file") MultipartFile file) throws IOException, MyException {
        // 将图片或者影音视频上传到分布式的文件存储系统
        String imgUrl = fileUrl;
        if (file != null) {
            System.out.println("multipartFile = " + file.getName() + "|" + file.getSize());

            String configFile = this.getClass().getResource("/tracker.conf").getFile();
            ClientGlobal.init(configFile);
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getTrackerServer();
            StorageClient storageClient = new StorageClient(trackerServer, null);
            String filename = file.getOriginalFilename();
            String extName = StringUtils.substringAfterLast(filename, ".");

            String[] upload_file = storageClient.upload_file(file.getBytes(), extName, null);
            imgUrl = fileUrl;
            for (int i = 0; i < upload_file.length; i++) {
                String path = upload_file[i];
                imgUrl += "/" + path;
            }
        }
        // 将图片的存储路径返回给页面
        return imgUrl;
    }
}
