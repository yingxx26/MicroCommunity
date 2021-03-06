package com.java110.api.listener.purchaseApplyDetail;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.listener.AbstractServiceApiListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.dto.basePrivilege.BasePrivilegeDto;
import com.java110.intf.community.IMenuInnerServiceSMO;
import com.java110.intf.store.IPurchaseApplyDetailInnerServiceSMO;
import com.java110.dto.purchaseApplyDetail.PurchaseApplyDetailDto;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.utils.constant.ServiceCodePurchaseApplyDetailConstant;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.vo.api.purchaseApplyDetail.ApiPurchaseApplyDetailDataVo;
import com.java110.vo.api.purchaseApplyDetail.ApiPurchaseApplyDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 查询小区侦听类
 */
@Java110Listener("listPurchaseApplyDetailsListener")
public class ListPurchaseApplyDetailsListener extends AbstractServiceApiListener {

    @Autowired
    private IPurchaseApplyDetailInnerServiceSMO purchaseApplyDetailInnerServiceSMOImpl;

    @Autowired
    private IMenuInnerServiceSMO menuInnerServiceSMOImpl;

    @Override
    public String getServiceCode() {
        return ServiceCodePurchaseApplyDetailConstant.LIST_PURCHASEAPPLYDETAILS;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }


    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }


    public IPurchaseApplyDetailInnerServiceSMO getPurchaseApplyDetailInnerServiceSMOImpl() {
        return purchaseApplyDetailInnerServiceSMOImpl;
    }

    public void setPurchaseApplyDetailInnerServiceSMOImpl(IPurchaseApplyDetailInnerServiceSMO purchaseApplyDetailInnerServiceSMOImpl) {
        this.purchaseApplyDetailInnerServiceSMOImpl = purchaseApplyDetailInnerServiceSMOImpl;
    }

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        super.validatePageInfo(reqJson);
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {

        PurchaseApplyDetailDto purchaseApplyDetailDto = BeanConvertUtil.covertBean(reqJson, PurchaseApplyDetailDto.class);

        //是否具有查看所有出入库明细的权限
        String userId = reqJson.getString("userId");
        BasePrivilegeDto basePrivilegeDto = new BasePrivilegeDto();
        basePrivilegeDto.setResource("/viewAllPurchaseApplyDetail");
        basePrivilegeDto.setUserId(userId);
        List<Map> privileges = menuInnerServiceSMOImpl.checkUserHasResource(basePrivilegeDto);
        purchaseApplyDetailDto.setUserId("");//申请人
        if (privileges.size()!=0) {
            purchaseApplyDetailDto.setUserId("");//创建人
        }else{
            purchaseApplyDetailDto.setCreateUserId(userId);//创建人
        }


        int count = purchaseApplyDetailInnerServiceSMOImpl.queryPurchaseApplyDetailsCount(purchaseApplyDetailDto);

        List<ApiPurchaseApplyDetailDataVo> purchaseApplyDetails = null;

        if (count > 0) {
            purchaseApplyDetails = BeanConvertUtil.covertBeanList(purchaseApplyDetailInnerServiceSMOImpl.queryPurchaseApplyDetails(purchaseApplyDetailDto), ApiPurchaseApplyDetailDataVo.class);
        } else {
            purchaseApplyDetails = new ArrayList<>();
        }

        ApiPurchaseApplyDetailVo apiPurchaseApplyDetailVo = new ApiPurchaseApplyDetailVo();

        apiPurchaseApplyDetailVo.setTotal(count);
        apiPurchaseApplyDetailVo.setRecords((int) Math.ceil((double) count / (double) reqJson.getInteger("row")));
        apiPurchaseApplyDetailVo.setPurchaseApplyDetails(purchaseApplyDetails);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(JSONObject.toJSONString(apiPurchaseApplyDetailVo), HttpStatus.OK);

        context.setResponseEntity(responseEntity);

    }
}
