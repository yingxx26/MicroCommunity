package com.java110.api.listener.fee;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.fee.IFeeBMO;
import com.java110.api.bmo.payFeeDetailDiscount.IPayFeeDetailDiscountBMO;
import com.java110.api.listener.AbstractServiceApiDataFlowListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.app.AppDto;
import com.java110.dto.fee.FeeAttrDto;
import com.java110.dto.fee.FeeDetailDto;
import com.java110.dto.fee.FeeDto;
import com.java110.dto.feeDiscount.ComputeDiscountDto;
import com.java110.dto.owner.OwnerCarDto;
import com.java110.dto.repair.RepairDto;
import com.java110.dto.repair.RepairUserDto;
import com.java110.entity.center.AppService;
import com.java110.entity.order.Orders;
import com.java110.intf.community.IRepairUserInnerServiceSMO;
import com.java110.intf.community.IRoomInnerServiceSMO;
import com.java110.intf.fee.*;
import com.java110.intf.user.IOwnerCarInnerServiceSMO;
import com.java110.po.applyRoomDiscount.ApplyRoomDiscountPo;
import com.java110.po.car.OwnerCarPo;
import com.java110.po.owner.RepairPoolPo;
import com.java110.po.owner.RepairUserPo;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.CommonConstant;
import com.java110.utils.constant.ServiceCodeConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.DateUtil;
import com.java110.utils.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @ClassName PayFeeListener
 * @Description TODO ???????????????
 * @Author wuxw
 * @Date 2019/6/3 13:46
 * @Version 1.0
 * add by wuxw 2019/6/3
 **/
@Java110Listener("payFeePreListener")
public class PayFeePreListener extends AbstractServiceApiDataFlowListener {

    private static Logger logger = LoggerFactory.getLogger(PayFeePreListener.class);

    @Autowired
    private IFeeBMO feeBMOImpl;

    @Autowired
    private IFeeInnerServiceSMO feeInnerServiceSMOImpl;

    @Autowired
    private IRoomInnerServiceSMO roomInnerServiceSMOImpl;

    @Autowired
    private IFeeConfigInnerServiceSMO feeConfigInnerServiceSMOImpl;

    @Autowired
    private IFeeDiscountInnerServiceSMO feeDiscountInnerServiceSMOImpl;

    @Autowired
    private IPayFeeDetailDiscountBMO payFeeDetailDiscountBMOImpl;

    @Autowired
    private IFeeAttrInnerServiceSMO feeAttrInnerServiceSMOImpl;

    @Autowired
    private IRepairUserInnerServiceSMO repairUserInnerServiceSMO;

    @Autowired
    private IOwnerCarInnerServiceSMO ownerCarInnerServiceSMOImpl;

    @Autowired
    private IApplyRoomDiscountInnerServiceSMO applyRoomDiscountInnerServiceSMOImpl;

    @Autowired
    private IFeeDetailInnerServiceSMO iFeeDetailInnerServiceSMO;

    @Override
    public String getServiceCode() {
        return ServiceCodeConstant.SERVICE_CODE_PAY_FEE_PRE;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }

    @Override
    public void soService(ServiceDataFlowEvent event) throws ParseException {

        logger.debug("ServiceDataFlowEvent : {}", event);

        DataFlowContext dataFlowContext = event.getDataFlowContext();
        AppService service = event.getAppService();

        String paramIn = dataFlowContext.getReqData();

        //????????????
        validate(paramIn);
        JSONObject paramObj = JSONObject.parseObject(paramIn);

        dataFlowContext.getRequestCurrentHeaders().put(CommonConstant.HTTP_ORDER_TYPE_CD, "D");
        JSONArray businesses = new JSONArray();
        //???????????????????????????
        judgeDiscount(paramObj);

        String appId = event.getDataFlowContext().getAppId();

        if (AppDto.WECHAT_MINA_OWNER_APP_ID.equals(appId)) {  //?????????????????????
            paramObj.put("primeRate", "6");
            paramObj.put("remark", "?????????????????????");
        } else if (AppDto.WECHAT_OWNER_APP_ID.equals(appId)) {  //?????????????????????
            paramObj.put("primeRate", "5");
            paramObj.put("remark", "?????????????????????");
        } else {
            paramObj.put("primeRate", "6");
            paramObj.put("remark", "?????????????????????");
        }

        //??????????????????
        businesses.add(feeBMOImpl.addFeePreDetail(paramObj, dataFlowContext));
        businesses.add(feeBMOImpl.modifyPreFee(paramObj, dataFlowContext));

        double discountPrice = paramObj.getDouble("discountPrice");
        if (discountPrice > 0) {
            addDiscount(paramObj, businesses, dataFlowContext);
        }

        dealOwnerCartEndTime(paramObj, businesses);

        //???????????????????????????ID
        FeeAttrDto feeAttrDto = new FeeAttrDto();
        feeAttrDto.setCommunityId(paramObj.getString("communityId"));
        feeAttrDto.setFeeId(paramObj.getString("feeId"));
        feeAttrDto.setSpecCd(FeeAttrDto.SPEC_CD_REPAIR);
        List<FeeAttrDto> feeAttrDtos = feeAttrInnerServiceSMOImpl.queryFeeAttrs(feeAttrDto);
        //?????? ????????????
        if (feeAttrDtos != null && feeAttrDtos.size() > 0) {
            JSONObject business = JSONObject.parseObject("{\"datas\":{}}");
            business.put(CommonConstant.HTTP_BUSINESS_TYPE_CD, BusinessTypeConstant.BUSINESS_TYPE_UPDATE_REPAIR);
            business.put(CommonConstant.HTTP_SEQ, DEFAULT_SEQ + 2);
            business.put(CommonConstant.HTTP_INVOKE_MODEL, CommonConstant.HTTP_INVOKE_MODEL_S);
            RepairPoolPo repairPoolPo = new RepairPoolPo();
            repairPoolPo.setRepairId(feeAttrDtos.get(0).getValue());
            repairPoolPo.setCommunityId(paramObj.getString("communityId"));
            repairPoolPo.setState(RepairDto.STATE_APPRAISE);
            business.getJSONObject(CommonConstant.HTTP_BUSINESS_DATAS).put(RepairPoolPo.class.getSimpleName(), BeanConvertUtil.beanCovertMap(repairPoolPo));
            businesses.add(business);
        }
        //????????????????????????
        if (feeAttrDtos != null && feeAttrDtos.size() > 0) {
            JSONObject business = JSONObject.parseObject("{\"datas\":{}}");
            business.put(CommonConstant.HTTP_BUSINESS_TYPE_CD, BusinessTypeConstant.BUSINESS_TYPE_UPDATE_REPAIR_USER);
            business.put(CommonConstant.HTTP_SEQ, DEFAULT_SEQ + 3);
            business.put(CommonConstant.HTTP_INVOKE_MODEL, CommonConstant.HTTP_INVOKE_MODEL_S);
            RepairUserDto repairUserDto = new RepairUserDto();
            repairUserDto.setRepairId(feeAttrDtos.get(0).getValue());
            repairUserDto.setState(RepairUserDto.STATE_PAY_FEE);
            //??????????????????????????????
            List<RepairUserDto> repairUserDtoList = repairUserInnerServiceSMO.queryRepairUsers(repairUserDto);
            Assert.listOnlyOne(repairUserDtoList, "???????????????");
            RepairUserPo repairUserPo = new RepairUserPo();
            repairUserPo.setRuId(repairUserDtoList.get(0).getRuId());
            repairUserPo.setState(RepairUserDto.STATE_FINISH_PAY_FEE);
            //????????????????????????????????????????????????
            repairUserPo.setEndTime(DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
            DecimalFormat df = new DecimalFormat("0.00");
            BigDecimal payment_amount = new BigDecimal(paramObj.getString("receivableAmount"));
            repairUserPo.setContext("?????????" + df.format(payment_amount) + "???");
            //?????????????????????
            JSONObject object = JSONObject.parseObject("{\"datas\":{}}");
            object.put(CommonConstant.HTTP_BUSINESS_TYPE_CD, BusinessTypeConstant.BUSINESS_TYPE_SAVE_REPAIR_USER);
            object.put(CommonConstant.HTTP_SEQ, DEFAULT_SEQ + 3);
            object.put(CommonConstant.HTTP_INVOKE_MODEL, CommonConstant.HTTP_INVOKE_MODEL_S);
            RepairUserPo repairUser = new RepairUserPo();
            repairUser.setRuId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_ruId));
            repairUser.setStartTime(repairUserPo.getEndTime());
            repairUser.setState(RepairUserDto.STATE_EVALUATE);
            repairUser.setContext("?????????");
            repairUser.setCommunityId(paramObj.getString("communityId"));
            repairUser.setCreateTime(DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
            repairUser.setRepairId(repairUserDtoList.get(0).getRepairId());
            repairUser.setStaffId(repairUserDtoList.get(0).getStaffId());
            repairUser.setStaffName(repairUserDtoList.get(0).getStaffName());
            repairUser.setPreStaffId(repairUserDtoList.get(0).getStaffId());
            repairUser.setPreStaffName(repairUserDtoList.get(0).getStaffName());
            repairUser.setPreRuId(repairUserDtoList.get(0).getRuId());
            repairUser.setRepairEvent("auditUser");
            object.getJSONObject(CommonConstant.HTTP_BUSINESS_DATAS).put(RepairUserPo.class.getSimpleName(), BeanConvertUtil.beanCovertMap(repairUser));
            business.getJSONObject(CommonConstant.HTTP_BUSINESS_DATAS).put(RepairUserPo.class.getSimpleName(), BeanConvertUtil.beanCovertMap(repairUserPo));
            businesses.add(object);
            businesses.add(business);
        }

        dataFlowContext.getRequestCurrentHeaders().put(CommonConstant.ORDER_PROCESS, Orders.ORDER_PROCESS_ORDER_PRE_SUBMIT);
        ResponseEntity<String> responseEntity = feeBMOImpl.callService(dataFlowContext, service.getServiceCode(), businesses);
        //?????? pay_fee_detail ????????????
        FeeDetailDto feeDetailDto = new FeeDetailDto();
        feeDetailDto.setDetailId(paramObj.getString("detailId"));
        List<FeeDetailDto> feeDetailDtoList = iFeeDetailInnerServiceSMO.queryBusinessFeeDetails(feeDetailDto);
        if (feeDetailDtoList != null && feeDetailDtoList.size() == 1) {
            //??????bId
            String bId = feeDetailDtoList.get(0).getbId();
            //????????????
            List<ComputeDiscountDto> computeDiscountDtos = (List<ComputeDiscountDto>) paramObj.get("computeDiscountDtos");
            if (computeDiscountDtos != null) {
                for (ComputeDiscountDto computeDiscountDto : computeDiscountDtos) {
                    if (!StringUtil.isEmpty(computeDiscountDto.getArdId())) {
                        ApplyRoomDiscountPo applyRoomDiscountPo = new ApplyRoomDiscountPo();
                        //?????????id??????????????????????????????
                        applyRoomDiscountPo.setbId(bId);
                        applyRoomDiscountPo.setArdId(computeDiscountDto.getArdId());
                        applyRoomDiscountInnerServiceSMOImpl.updateApplyRoomDiscount(applyRoomDiscountPo);
                    }
                }
            }

        }
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            dataFlowContext.setResponseEntity(responseEntity);
            return;
        }

        JSONObject paramOut = JSONObject.parseObject(responseEntity.getBody());
        //???????????????????????????
        paramOut.put("receivableAmount", paramObj.getString("receivableAmount"));
        paramOut.put("receivedAmount", paramObj.getString("receivedAmount"));
        responseEntity = new ResponseEntity<>(paramOut.toJSONString(), HttpStatus.OK);
        dataFlowContext.setResponseEntity(responseEntity);
    }

    private void dealOwnerCartEndTime(JSONObject paramObj, JSONArray businesses) {
        //????????????????????????
        if (paramObj.containsKey("carPayerObjType")
                && FeeDto.PAYER_OBJ_TYPE_CAR.equals(paramObj.getString("carPayerObjType"))) {
            Date feeEndTime = (Date) paramObj.get("carFeeEndTime");
            OwnerCarDto ownerCarDto = new OwnerCarDto();
            ownerCarDto.setCommunityId(paramObj.getString("communityId"));
            ownerCarDto.setCarId(paramObj.getString("carPayerObjId"));
            List<OwnerCarDto> ownerCarDtos = ownerCarInnerServiceSMOImpl.queryOwnerCars(ownerCarDto);
            //??????????????????
            if (ownerCarDtos != null) {
                for (OwnerCarDto tmpOwnerCarDto : ownerCarDtos) {
                    if (tmpOwnerCarDto.getEndTime().getTime() < feeEndTime.getTime()) {
                        JSONObject business = JSONObject.parseObject("{\"datas\":{}}");
                        business.put(CommonConstant.HTTP_BUSINESS_TYPE_CD, BusinessTypeConstant.BUSINESS_TYPE_UPDATE_OWNER_CAR);
                        business.put(CommonConstant.HTTP_SEQ, DEFAULT_SEQ + 1);
                        business.put(CommonConstant.HTTP_INVOKE_MODEL, CommonConstant.HTTP_INVOKE_MODEL_S);
                        OwnerCarPo ownerCarPo = new OwnerCarPo();
                        ownerCarPo.setMemberId(tmpOwnerCarDto.getMemberId());
                        ownerCarPo.setEndTime(DateUtil.getFormatTimeString(feeEndTime, DateUtil.DATE_FORMATE_STRING_A));
                        business.getJSONObject(CommonConstant.HTTP_BUSINESS_DATAS).put(OwnerCarPo.class.getSimpleName(), BeanConvertUtil.beanCovertMap(ownerCarPo));
                        businesses.add(business);
                    }
                }
            }
        }
    }

    private void judgeDiscount(JSONObject paramObj) throws ParseException {
        FeeDetailDto feeDetailDto = new FeeDetailDto();
        feeDetailDto.setCommunityId(paramObj.getString("communityId"));
        feeDetailDto.setFeeId(paramObj.getString("feeId"));
        feeDetailDto.setCycles(paramObj.getString("cycles"));
        feeDetailDto.setPayerObjId(paramObj.getString("payerObjId"));
        feeDetailDto.setPayerObjType(paramObj.getString("payerObjType"));
        String endTime = paramObj.getString("endTime");  //????????????????????????
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        feeDetailDto.setStartTime(simpleDateFormat.parse(endTime));

        feeDetailDto.setRow(20);
        feeDetailDto.setPage(1);
        List<ComputeDiscountDto> computeDiscountDtos = feeDiscountInnerServiceSMOImpl.computeDiscount(feeDetailDto);

        if (computeDiscountDtos == null || computeDiscountDtos.size() < 1) {
            paramObj.put("discountPrice", 0.0);
            return;
        }
        BigDecimal discountPrice = new BigDecimal(0);
        for (ComputeDiscountDto computeDiscountDto : computeDiscountDtos) {
            discountPrice = discountPrice.add(new BigDecimal(computeDiscountDto.getDiscountPrice()));
        }
        paramObj.put("discountPrice", discountPrice);
        paramObj.put("computeDiscountDtos", computeDiscountDtos);
    }

    private void addDiscount(JSONObject paramObj, JSONArray businesses, DataFlowContext dataFlowContext) {
        List<ComputeDiscountDto> computeDiscountDtos = (List<ComputeDiscountDto>) paramObj.get("computeDiscountDtos");
        JSONObject discountBusiness = null;
        for (ComputeDiscountDto computeDiscountDto : computeDiscountDtos) {
            if (computeDiscountDto.getDiscountPrice() <= 0) {
                continue;
            }
            JSONObject paramIn = new JSONObject();
            paramIn.put("discountPrice", computeDiscountDto.getDiscountPrice());
            paramIn.put("discountId", computeDiscountDto.getDiscountId());
            paramIn.put("detailId", paramObj.getString("detailId"));
            paramIn.put("communityId", paramObj.getString("communityId"));
            paramIn.put("feeId", paramObj.getString("feeId"));
            discountBusiness = payFeeDetailDiscountBMOImpl.addPayFeeDetailDiscount(paramObj,
                    paramIn, dataFlowContext);
            if (discountBusiness != null) {
                businesses.add(discountBusiness);
            }
        }
    }

    /**
     * ????????????
     *
     * @param paramIn "communityId": "7020181217000001",
     *                "memberId": "3456789",
     *                "memberTypeCd": "390001200001"
     */
    private void validate(String paramIn) {
        Assert.jsonObjectHaveKey(paramIn, "communityId", "????????????????????????communityId??????");
        Assert.jsonObjectHaveKey(paramIn, "cycles", "????????????????????????cycles??????");
        Assert.jsonObjectHaveKey(paramIn, "receivedAmount", "????????????????????????receivedAmount??????");
        Assert.jsonObjectHaveKey(paramIn, "feeId", "????????????????????????feeId??????");
        Assert.jsonObjectHaveKey(paramIn, "appId", "????????????????????????appId??????");

        JSONObject paramInObj = JSONObject.parseObject(paramIn);
        Assert.hasLength(paramInObj.getString("communityId"), "??????ID????????????");
        Assert.hasLength(paramInObj.getString("cycles"), "??????????????????");
        Assert.hasLength(paramInObj.getString("receivedAmount"), "????????????????????????");
        Assert.hasLength(paramInObj.getString("feeId"), "??????ID????????????");
        Assert.hasLength(paramInObj.getString("appId"), "appId????????????");


    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }


    public IFeeInnerServiceSMO getFeeInnerServiceSMOImpl() {
        return feeInnerServiceSMOImpl;
    }

    public void setFeeInnerServiceSMOImpl(IFeeInnerServiceSMO feeInnerServiceSMOImpl) {
        this.feeInnerServiceSMOImpl = feeInnerServiceSMOImpl;
    }

    public IFeeConfigInnerServiceSMO getFeeConfigInnerServiceSMOImpl() {
        return feeConfigInnerServiceSMOImpl;
    }

    public void setFeeConfigInnerServiceSMOImpl(IFeeConfigInnerServiceSMO feeConfigInnerServiceSMOImpl) {
        this.feeConfigInnerServiceSMOImpl = feeConfigInnerServiceSMOImpl;
    }

    public IRoomInnerServiceSMO getRoomInnerServiceSMOImpl() {
        return roomInnerServiceSMOImpl;
    }

    public void setRoomInnerServiceSMOImpl(IRoomInnerServiceSMO roomInnerServiceSMOImpl) {
        this.roomInnerServiceSMOImpl = roomInnerServiceSMOImpl;
    }
}
