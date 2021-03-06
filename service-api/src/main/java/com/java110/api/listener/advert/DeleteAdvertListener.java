package com.java110.api.listener.advert;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.listener.AbstractServiceApiPlusListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.po.advert.AdvertPo;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.ServiceCodeAdvertConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import org.springframework.http.HttpMethod;

/**
 * 保存小区侦听
 * add by wuxw 2019-06-30
 */
@Java110Listener("deleteAdvertListener")
public class DeleteAdvertListener extends AbstractServiceApiPlusListener {


    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        //Assert.hasKeyAndValue(reqJson, "xxx", "xxx");

        Assert.hasKeyAndValue(reqJson, "advertId", "广告ID不能为空");

    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {

        AdvertPo advertPo = BeanConvertUtil.covertBean(reqJson, AdvertPo.class);
        super.delete(context, advertPo, BusinessTypeConstant.BUSINESS_TYPE_DELETE_ADVERT);
    }


    @Override
    public String getServiceCode() {
        return ServiceCodeAdvertConstant.DELETE_ADVERT;
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
