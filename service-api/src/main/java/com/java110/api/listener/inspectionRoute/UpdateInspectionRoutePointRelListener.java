package com.java110.api.listener.inspectionRoute;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.inspection.IInspectionBMO;
import com.java110.api.listener.AbstractServiceApiPlusListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.utils.constant.ServiceCodeInspectionRouteConstant;
import com.java110.utils.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

/**
 * 保存巡检路线侦听
 * add by wuxw 2019-06-30
 */
@Java110Listener("updateInspectionRoutePointRelListener")
public class UpdateInspectionRoutePointRelListener extends AbstractServiceApiPlusListener {
    @Autowired
    private IInspectionBMO inspectionBMOImpl;

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "irpRelId", "路线巡检点ID不能为空");
        Assert.hasKeyAndValue(reqJson, "inspectionRouteId", "路线ID不能为空");
        Assert.hasKeyAndValue(reqJson, "communityId", "小区ID不能为空");

    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {

        inspectionBMOImpl.updateInspectionRoutePointRel(reqJson, context);
    }

    @Override
    public String getServiceCode() {
        return ServiceCodeInspectionRouteConstant.UPDATE_INSPECTIONROUTE_POINT_REL;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

}
