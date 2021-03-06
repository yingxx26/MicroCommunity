package com.java110.api.smo.@@templateCode@@.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.component.AbstractComponentSMO;
import com.java110.core.context.IPageData;
import com.java110.entity.component.ComponentValidateResult;
import com.java110.utils.constant.PrivilegeCodeConstant;
import com.java110.utils.constant.ServiceConstant;
import com.java110.utils.util.Assert;
import com.java110.api.smo.@@templateCode@@.IAdd@@TemplateCode@@SMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
/**
 * 添加小区服务实现类
 * add by wuxw 2019-06-30
 */
@Service("add@@TemplateCode@@SMOImpl")
public class Add@@TemplateCode@@SMOImpl extends DefaultAbstractComponentSMO implements IAdd@@TemplateCode@@SMO {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    protected void validate(IPageData pd, JSONObject paramIn) {

        //super.validatePageInfo(pd);

        //Assert.hasKeyAndValue(paramIn, "xxx", "xxx");
        @@validateTemplateColumns@@


        //super.checkUserHasPrivilege(pd, restTemplate, PrivilegeCodeConstant.AGENT_HAS_LIST_@@TEMPLATECODE@@);

    }

    @Override
    protected ResponseEntity<String> doBusinessProcess(IPageData pd, JSONObject paramIn) {
        ResponseEntity<String> responseEntity = null;
        super.validateStoreStaffCommunityRelationship(pd, restTemplate);

        responseEntity = this.callCenterService(restTemplate, pd, paramIn.toJSONString(),
        "@@templateCode@@.save@@TemplateCode@@",
                HttpMethod.POST);
        return responseEntity;
    }

    @Override
    public ResponseEntity<String> save@@TemplateCode@@(IPageData pd) {
        return super.businessProcess(pd);
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
