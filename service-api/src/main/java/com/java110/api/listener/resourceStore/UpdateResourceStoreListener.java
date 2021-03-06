package com.java110.api.listener.resourceStore;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.resourceStore.IResourceStoreBMO;
import com.java110.api.listener.AbstractServiceApiPlusListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.intf.store.IResourceStoreInnerServiceSMO;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.utils.constant.ServiceCodeResourceStoreConstant;
import com.java110.utils.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

/**
 * 保存物品管理侦听
 * add by wuxw 2019-06-30
 */
@Java110Listener("updateResourceStoreListener")
public class UpdateResourceStoreListener extends AbstractServiceApiPlusListener {

    @Autowired
    private IResourceStoreInnerServiceSMO resourceStoreInnerServiceSMOImpl;

    @Autowired
    private IResourceStoreBMO resourceStoreBMOImpl;

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "resId", "物品ID不能为空");
        Assert.hasKeyAndValue(reqJson, "resName", "必填，请填写物品名称");
        Assert.hasKeyAndValue(reqJson, "price", "必填，请填写物品价格");
        Assert.hasKeyAndValue(reqJson, "storeId", "商户信息不能为空");
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {
        //获取最低收费标准
        double outLowPrice = Double.parseDouble(reqJson.getString("outLowPrice"));
        //获取最高收费标准
        double outHighPrice = Double.parseDouble(reqJson.getString("outHighPrice"));
        if (outLowPrice > outHighPrice) {
            throw new IllegalArgumentException("最低收费标准不能大于最高收费标准！");
        }
        resourceStoreBMOImpl.updateResourceStore(reqJson, context);
    }

    @Override
    public String getServiceCode() {
        return ServiceCodeResourceStoreConstant.UPDATE_RESOURCESTORE;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }

    public IResourceStoreInnerServiceSMO getResourceStoreInnerServiceSMOImpl() {
        return resourceStoreInnerServiceSMOImpl;
    }

    public void setResourceStoreInnerServiceSMOImpl(IResourceStoreInnerServiceSMO resourceStoreInnerServiceSMOImpl) {
        this.resourceStoreInnerServiceSMOImpl = resourceStoreInnerServiceSMOImpl;
    }
}
