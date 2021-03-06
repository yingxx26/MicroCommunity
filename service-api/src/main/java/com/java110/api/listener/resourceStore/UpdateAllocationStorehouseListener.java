package com.java110.api.listener.resourceStore;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.allocationStorehouse.IAllocationStorehouseBMO;
import com.java110.api.listener.AbstractServiceApiPlusListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.utils.constant.ServiceCodeAllocationStorehouseConstant;
import com.java110.utils.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

/**
 * 保存仓库调拨侦听
 * add by wuxw 2019-06-30
 */
@Java110Listener("updateAllocationStorehouseListener")
public class UpdateAllocationStorehouseListener extends AbstractServiceApiPlusListener {

    @Autowired
    private IAllocationStorehouseBMO allocationStorehouseBMOImpl;

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "asId", "asId不能为空");
        Assert.hasKeyAndValue(reqJson, "shIda", "请求报文中未包含shIda");
        Assert.hasKeyAndValue(reqJson, "shIdz", "请求报文中未包含shIdz");
        Assert.hasKeyAndValue(reqJson, "resId", "请求报文中未包含resId");
        Assert.hasKeyAndValue(reqJson, "stock", "请求报文中未包含stock");
        Assert.hasKeyAndValue(reqJson, "storeId", "请求报文中未包含storeId");

    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {

        allocationStorehouseBMOImpl.updateAllocationStorehouse(reqJson, context);
    }

    @Override
    public String getServiceCode() {
        return ServiceCodeAllocationStorehouseConstant.UPDATE_ALLOCATIONSTOREHOUSE;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }
}
