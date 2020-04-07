package hao.you.mall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import hao.you.mall.annotations.LoginRequired;
import hao.you.mall.bean.*;
import hao.you.mall.common.Constants;
import hao.you.mall.service.AttrService;
import hao.you.mall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
public class SearchController {

    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping(value = "index", method = RequestMethod.GET )
    @LoginRequired(loginSuccess = false)
    public String index(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        String memberId = (String) request.getAttribute(Constants.memberId);
        String nickName = (String) request.getAttribute(Constants.nickname);

        modelMap.put("memberId", memberId);
        modelMap.put("nickName", nickName);
        return "index";
    }

    @RequestMapping("list.html")
    @LoginRequired(loginSuccess = false)
    public String list(HttpServletRequest request, HttpServletResponse response, PmsSearchParam pmsSearchParam, ModelMap modelMap) {
        String memberId = (String) request.getAttribute(Constants.memberId);
        String nickName = (String) request.getAttribute(Constants.nickname);
        modelMap.put("memberId", memberId);
        modelMap.put("nickName", nickName);
        //根据查询参数查询sku集合
        List<PmsSearchSkuInfo> list = searchService.list(pmsSearchParam);
        if (list == null || list.size() == 0) {
            return "index";
        }
        modelMap.put("skuLsInfoList", list);

        //抽取检索结果包含的平台属性
        Set<String> valueIdSet = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : list) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                valueIdSet.add(pmsSkuAttrValue.getValueId());
            }
        }

        //es中平台属性值为空时，这里会报错，这个是防止错误数据
        if (valueIdSet.size() == 0) {
            return "index";
        }

        //根据属性ID查询属性列表
        List<PmsBaseAttrInfo> pmsBaseAttrInfoList = attrService.getAttrValueListByValueId(valueIdSet);
        modelMap.put("attrList", pmsBaseAttrInfoList);

        //每选择一个属性删除当前属性所在的属性组
        //迭代器动态检查删除
        String[] valueId = pmsSearchParam.getValueId();

        if (valueId != null) {
            //面包屑
            List<SearchCrumb> crumbs = new ArrayList<>();
            //valueId为当前页面包含的属性值，面包屑中url需要删除该属性值

            for (String value : valueId) {
                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfoList.iterator();
                SearchCrumb searchCrumb = new SearchCrumb();
                searchCrumb.setValueId(value);
                searchCrumb.setUrlParam(getUrlParam(pmsSearchParam, value));
                while (iterator.hasNext()) {
                    PmsBaseAttrInfo next = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = next.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        String attrId = pmsBaseAttrValue.getId();
                        if (value.equals(attrId)) {
                            searchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            iterator.remove();
                        }
                    }
                }
                crumbs.add(searchCrumb);
            }
            //筛选条件，面包屑
            modelMap.put("attrValueSelectedList", crumbs);
        }

        String urlParam = getUrlParam(pmsSearchParam);
        modelMap.put("urlParam", urlParam);
        return "list";
    }

    String getUrlParam(PmsSearchParam pmsSearchParam, String... delValueId) {
        StringBuffer sb = new StringBuffer();
        String catalog3Id = null;
        if (pmsSearchParam.getCatalog3Id() != null)
            catalog3Id = String.valueOf(pmsSearchParam.getCatalog3Id());
        String keyword = pmsSearchParam.getKeyword();
        String[] valueId = pmsSearchParam.getValueId();
        String[] valueIds = null;
        if (valueId != null) {
            valueIds = new String[valueId.length];
            for (int i = 0; i < valueId.length; i++) {
                valueIds[i] = String.valueOf(valueId[i]);
            }
        }
        if (catalog3Id != null && StringUtils.isNotBlank(catalog3Id)) {
            if (sb.length() != 0) {
                sb.append('&');
            }
            sb.append("catalog3Id=");
            sb.append(catalog3Id);
        }
        if (StringUtils.isNotBlank(keyword)) {
            if (sb.length() != 0) {
                sb.append('&');
            }
            sb.append("keyword=");
            sb.append(keyword);
        }
        if (valueIds != null) {
            for (int i = 0; i < valueIds.length; i++) {
                //面包屑中删除自己的属性值,变参注意事项
                if (delValueId.length > 0 && delValueId[0].equals(valueIds[i])) {
                    continue;
                }
                if (sb.length() != 0) {
                    sb.append('&');
                }
                sb.append("valueId=");
                sb.append(valueIds[i]);
            }
        }
        return sb.toString();
    }
}
