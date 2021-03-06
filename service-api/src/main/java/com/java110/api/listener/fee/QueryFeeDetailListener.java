package com.java110.api.listener.fee;


import com.alibaba.fastjson.JSONObject;
import com.java110.api.listener.AbstractServiceApiDataFlowListener;
import com.java110.utils.constant.ServiceCodeConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.DateUtil;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.intf.fee.IFeeDetailInnerServiceSMO;
import com.java110.dto.fee.FeeDetailDto;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.utils.util.StringUtil;
import com.java110.vo.api.ApiFeeDetailDataVo;
import com.java110.vo.api.ApiFeeDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName FloorDto
 * @Description 小区楼数据层侦听类
 * @Author wuxw
 * @Date 2019/4/24 8:52
 * @Version 1.0
 * add by wuxw 2019/4/24
 **/
@Java110Listener("queryFeeDetail")
public class QueryFeeDetailListener extends AbstractServiceApiDataFlowListener {

    @Autowired
    private IFeeDetailInnerServiceSMO feeDetailInnerServiceSMOImpl;

    @Override
    public String getServiceCode() {
        return ServiceCodeConstant.SERVICE_CODE_QUERY_FEE_DETAIL;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }

    /**
     * 业务层数据处理
     *
     * @param event 时间对象
     */
    @Override
    public void soService(ServiceDataFlowEvent event) {
        DataFlowContext dataFlowContext = event.getDataFlowContext();
        //获取请求数据
        JSONObject reqJson = dataFlowContext.getReqJson();
        //获取开始时间
        if (!StringUtil.isEmpty(reqJson.getString("startTime"))) {
            String startTime = reqJson.getString("startTime") + " 00:00:00";
            reqJson.put("startTime", startTime);
        }
        //获取结束时间
        if (!StringUtil.isEmpty(reqJson.getString("endTime"))) {
            String endTime = reqJson.getString("endTime") + " 23:59:59";
            reqJson.put("endTime", endTime);
        }
        validateFeeConfigData(reqJson);

        //查询总记录数
        ApiFeeDetailVo apiFeeDetailVo = new ApiFeeDetailVo();
        FeeDetailDto feeDetailDto = BeanConvertUtil.covertBean(reqJson, FeeDetailDto.class);

        int total = feeDetailInnerServiceSMOImpl.queryFeeDetailsCount(feeDetailDto);
        apiFeeDetailVo.setTotal(total);
        if (total > 0) {
            List<FeeDetailDto> feeDetailDtos = feeDetailInnerServiceSMOImpl.queryFeeDetails(BeanConvertUtil.covertBean(reqJson, FeeDetailDto.class));
            List<FeeDetailDto> feeDetailList = new ArrayList<>();
            for (FeeDetailDto feeDetail : feeDetailDtos) {
                //获取状态
                String state = feeDetail.getState();
                if (!StringUtil.isEmpty(state) && (state.equals("1300") || state.equals("1100") || state.equals("1200"))) { //退费单、已退费、退费失败状态
                    //获取周期
                    String cycles = feeDetail.getCycles();
                    if (!StringUtil.isEmpty(cycles) && cycles.contains("-")) {
                        feeDetail.setCycles(cycles.substring(1));
                    }
                    //获取应收金额
                    String receivableAmount = feeDetail.getReceivableAmount();
                    if (!StringUtil.isEmpty(receivableAmount) && receivableAmount.contains("-")) {
                        feeDetail.setReceivableAmount(receivableAmount.substring(1));
                    }
                    //获取实收金额
                    String receivedAmount = feeDetail.getReceivedAmount();
                    if (!StringUtil.isEmpty(receivedAmount) && receivedAmount.contains("-")) {
                        feeDetail.setReceivedAmount(receivedAmount.substring(1));
                    }
                }
                feeDetailList.add(feeDetail);
            }
            List<ApiFeeDetailDataVo> feeDetails = BeanConvertUtil.covertBeanList(feeDetailList, ApiFeeDetailDataVo.class);

            //reFreshCreateTime(feeDetails, feeDetailDtos);

            apiFeeDetailVo.setFeeDetails(feeDetails);
        }
        int row = reqJson.getInteger("row");
        apiFeeDetailVo.setRecords((int) Math.ceil((double) total / (double) row));
        ResponseEntity<String> responseEntity = new ResponseEntity<String>(JSONObject.toJSONString(apiFeeDetailVo), HttpStatus.OK);

        dataFlowContext.setResponseEntity(responseEntity);
    }

    /**
     * 刷新 创建时间
     *
     * @param feeDetails    返回对象
     * @param feeDetailDtos 数据传输对象
     */
    private void reFreshCreateTime(List<ApiFeeDetailDataVo> feeDetails, List<FeeDetailDto> feeDetailDtos) {
        for (ApiFeeDetailDataVo feeDetailDataVo : feeDetails) {
            for (FeeDetailDto feeDetailDto : feeDetailDtos) {
                if (feeDetailDataVo.getDetailId().equals(feeDetailDto.getDetailId())) {
                    feeDetailDataVo.setCreateTime(DateUtil.getFormatTimeString(feeDetailDto.getCreateTime(), DateUtil.DATE_FORMATE_STRING_A));
                }
            }
        }
    }

    /**
     * 校验查询条件是否满足条件
     *
     * @param reqJson 包含查询条件
     */
    private void validateFeeConfigData(JSONObject reqJson) {
        Assert.jsonObjectHaveKey(reqJson, "communityId", "请求中未包含communityId信息");
        // Assert.jsonObjectHaveKey(reqJson, "feeId", "请求中未包含feeId信息");


    }

    @Override
    public int getOrder() {
        return super.DEFAULT_ORDER;
    }

    public IFeeDetailInnerServiceSMO getFeeDetailInnerServiceSMOImpl() {
        return feeDetailInnerServiceSMOImpl;
    }

    public void setFeeDetailInnerServiceSMOImpl(IFeeDetailInnerServiceSMO feeDetailInnerServiceSMOImpl) {
        this.feeDetailInnerServiceSMOImpl = feeDetailInnerServiceSMOImpl;
    }
}
