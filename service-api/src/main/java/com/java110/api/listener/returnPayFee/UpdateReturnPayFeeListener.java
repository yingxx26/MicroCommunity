package com.java110.api.listener.returnPayFee;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.fee.IFeeBMO;
import com.java110.api.bmo.payFeeDetailDiscount.IPayFeeDetailDiscountBMO;
import com.java110.api.bmo.returnPayFee.IReturnPayFeeBMO;
import com.java110.api.listener.AbstractServiceApiPlusListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.dto.fee.FeeDetailDto;
import com.java110.dto.fee.FeeDto;
import com.java110.dto.payFeeDetailDiscount.PayFeeDetailDiscountDto;
import com.java110.intf.fee.IFeeDetailInnerServiceSMO;
import com.java110.intf.fee.IFeeInnerServiceSMO;
import com.java110.intf.fee.IPayFeeDetailDiscountInnerServiceSMO;
import com.java110.utils.constant.ServiceCodeReturnPayFeeConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.DateUtil;
import com.java110.utils.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.text.SimpleDateFormat;
import java.util.List;


/**
 * 保存退费表侦听
 * add by wuxw 2019-06-30
 */
@Java110Listener("updateReturnPayFeeListener")
public class UpdateReturnPayFeeListener extends AbstractServiceApiPlusListener {

    @Autowired
    private IReturnPayFeeBMO returnPayFeeBMOImpl;
    @Autowired
    private IFeeInnerServiceSMO feeInnerServiceSMOImpl;

    @Autowired
    private IFeeDetailInnerServiceSMO feeDetailInnerServiceSMOImpl;

    @Autowired
    private IPayFeeDetailDiscountInnerServiceSMO payFeeDetailDiscountInnerServiceSMOImpl;

    @Autowired
    private IFeeBMO feeBMOImpl;

    @Autowired
    private IPayFeeDetailDiscountBMO payFeeDetailDiscountBMOImpl;

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "returnFeeId", "returnFeeId不能为空");
        Assert.hasKeyAndValue(reqJson, "state", "state不能为空");
        Assert.hasKeyAndValue(reqJson, "feeId", "feeId不能为空");

        if(reqJson.containsKey("cycles")){
            String cycles = reqJson.getString("cycles");
            if(!cycles.startsWith("-")){
                throw new IllegalArgumentException("退费周期必须负数");// 这里必须传入负数，否则费用自动相加不会退费
            }
        }

        FeeDetailDto feeDetailDto = new FeeDetailDto();
        feeDetailDto.setDetailId(reqJson.getString("detailId"));
        feeDetailDto.setFeeId(reqJson.getString("feeId"));
        feeDetailDto.setCommunityId(reqJson.getString("communityId"));
        List<FeeDetailDto> feeDetailDtos = feeDetailInnerServiceSMOImpl.queryFeeDetails(feeDetailDto);

        Assert.listOnlyOne(feeDetailDtos, "不存在缴费记录");
        reqJson.put("feeDetailDto", feeDetailDtos.get(0));
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {
        FeeDetailDto feeDetailDto = (FeeDetailDto) reqJson.get("feeDetailDto");
        returnPayFeeBMOImpl.updateReturnPayFee(reqJson, context);

        //退费审核通过
        if ("1100".equals(reqJson.getString("state"))) {
            //判断退费周期是否为负数如果不是 抛出异常
            String cycles = reqJson.getString("cycles");
            reqJson.put("state", "1300");
            reqJson.put("startTime", DateUtil.getFormatTimeString(feeDetailDto.getStartTime(), DateUtil.DATE_FORMATE_STRING_A));
            reqJson.put("endTime", DateUtil.getFormatTimeString(feeDetailDto.getEndTime(), DateUtil.DATE_FORMATE_STRING_A));
            returnPayFeeBMOImpl.addFeeDetail(reqJson, context);

            reqJson.put("state", "1100");
            String receivableAmount = (String) reqJson.get("receivableAmount");
            String receivedAmount = (String) reqJson.get("receivedAmount");
            reqJson.put("cycles", unum(cycles));
            reqJson.put("receivableAmount", unum(receivableAmount));
            reqJson.put("receivedAmount", unum(receivedAmount));
            reqJson.put("createTime", reqJson.get("payTime"));
            returnPayFeeBMOImpl.updateFeeDetail(reqJson, context);
            //修改pay_fee 费用到期时间  以及如果是押金则修改状态为结束收费
            FeeDto feeDto = new FeeDto();
            feeDto.setFeeId((String) reqJson.get("feeId"));
            List<FeeDto> feeDtos = feeInnerServiceSMOImpl.queryFees(feeDto);

            Assert.listOnlyOne(feeDtos, "费用不存在");
            FeeDto feeDto1 = feeDtos.get(0);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            reqJson.put("endTime", DateUtil.getFormatTimeString(feeDetailDto.getStartTime(), DateUtil.DATE_FORMATE_STRING_A));
            reqJson.put("amount", feeDto1.getAmount());
            reqJson.put("feeTypeCd", feeDto1.getFeeTypeCd());
            reqJson.put("communityId", feeDto1.getCommunityId());
            reqJson.put("payerObjId", feeDto1.getPayerObjId());
            reqJson.put("incomeObjId", feeDto1.getIncomeObjId());
            reqJson.put("startTime", sdf.format(feeDto1.getStartTime()));
            reqJson.put("userId", feeDto1.getUserId());
            reqJson.put("feeFlag", feeDto1.getFeeFlag());
            reqJson.put("statusCd", feeDto1.getStatusCd());
            reqJson.put("state", feeDto1.getState());
            reqJson.put("configId", feeDto1.getConfigId());
            reqJson.put("payerObjType", feeDto1.getPayerObjType());
            reqJson.put("feeId", feeDto1.getFeeId());
            if ("888800010006".equals(feeDto1.getFeeTypeCds())) {
                reqJson.put("state", "2009001");
            } else {
                reqJson.put("state", "2008001");
            }
            feeBMOImpl.updateFee(reqJson, context);

            //检查是否有优惠
            PayFeeDetailDiscountDto payFeeDetailDiscountDto = new PayFeeDetailDiscountDto();
            payFeeDetailDiscountDto.setCommunityId(feeDto1.getCommunityId());
            payFeeDetailDiscountDto.setDetailId(reqJson.getString("detailId"));
            List<PayFeeDetailDiscountDto> payFeeDetailDiscountDtos = payFeeDetailDiscountInnerServiceSMOImpl.queryPayFeeDetailDiscounts(payFeeDetailDiscountDto);
            if (payFeeDetailDiscountDtos != null && payFeeDetailDiscountDtos.size() > 0) {
                JSONObject discountJson = new JSONObject();
                discountJson.put("discountId", payFeeDetailDiscountDtos.get(0).getDiscountId());
                discountJson.put("discountPrice", unum(payFeeDetailDiscountDtos.get(0).getDiscountPrice()));
                payFeeDetailDiscountBMOImpl.addPayFeeDetailDiscountTwo(reqJson,
                        discountJson, context);
            }

        }
        //不通过
        if ("1200".equals(reqJson.getString("state"))) {
            reqJson.put("state", "1200");
            returnPayFeeBMOImpl.updateFeeDetail(reqJson, context);
            reqJson.put("state", "1200");
            String cycles = (String) reqJson.get("cycles");
            String receivableAmount = (String) reqJson.get("receivableAmount");
            String receivedAmount = (String) reqJson.get("receivedAmount");
            reqJson.put("cycles", unum(cycles));
            reqJson.put("receivableAmount", unum(receivableAmount));
            reqJson.put("receivedAmount", unum(receivedAmount));
            reqJson.put("createTime", reqJson.get("payTime"));
            returnPayFeeBMOImpl.updateFeeDetail(reqJson, context);
        }


    }

    private double unum(String value) {
        double dValue = Double.parseDouble(value);

        return dValue * -1;
    }

    @Override
    public String getServiceCode() {
        return ServiceCodeReturnPayFeeConstant.UPDATE_RETURNPAYFEE;
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
