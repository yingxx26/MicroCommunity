package com.java110.api.listener.fee;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.account.IAccountDetailBMO;
import com.java110.api.bmo.fee.IFeeBMO;
import com.java110.api.bmo.payFeeDetailDiscount.IPayFeeDetailDiscountBMO;
import com.java110.api.listener.AbstractServiceApiDataFlowListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.fee.FeeAttrDto;
import com.java110.dto.fee.FeeConfigDto;
import com.java110.dto.fee.FeeDto;
import com.java110.dto.feeReceipt.FeeReceiptDetailDto;
import com.java110.dto.owner.OwnerCarDto;
import com.java110.dto.parking.ParkingSpaceDto;
import com.java110.dto.repair.RepairDto;
import com.java110.dto.repair.RepairUserDto;
import com.java110.entity.center.AppService;
import com.java110.intf.community.IParkingSpaceInnerServiceSMO;
import com.java110.intf.community.IRepairInnerServiceSMO;
import com.java110.intf.community.IRepairUserInnerServiceSMO;
import com.java110.intf.community.IRoomInnerServiceSMO;
import com.java110.intf.fee.*;
import com.java110.intf.user.IOwnerCarInnerServiceSMO;
import com.java110.po.applyRoomDiscount.ApplyRoomDiscountPo;
import com.java110.po.car.OwnerCarPo;
import com.java110.po.feeReceipt.FeeReceiptPo;
import com.java110.po.feeReceiptDetail.FeeReceiptDetailPo;
import com.java110.po.owner.RepairPoolPo;
import com.java110.po.owner.RepairUserPo;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.CommonConstant;
import com.java110.utils.constant.ServiceCodeConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.DateUtil;
import com.java110.utils.util.StringUtil;
import com.java110.vo.ResultVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * @ClassName PayFeeListener
 * @Description TODO ????????????
 * @Author wuxw
 * @Date 2019/6/3 13:46
 * @Version 1.0
 * add by wuxw 2019/6/3
 **/
@Java110Listener("payFeeListener")
public class PayFeeListener extends AbstractServiceApiDataFlowListener {

    private static Logger logger = LoggerFactory.getLogger(PayFeeListener.class);

    @Autowired
    private IFeeBMO feeBMOImpl;

    @Autowired
    private IFeeInnerServiceSMO feeInnerServiceSMOImpl;

    @Autowired
    private IFeeAttrInnerServiceSMO feeAttrInnerServiceSMOImpl;

    @Autowired
    private IRoomInnerServiceSMO roomInnerServiceSMOImpl;

    @Autowired
    private IFeeConfigInnerServiceSMO feeConfigInnerServiceSMOImpl;

    @Autowired
    private IOwnerCarInnerServiceSMO ownerCarInnerServiceSMOImpl;

    @Autowired
    private IPayFeeDetailDiscountBMO payFeeDetailDiscountBMOImpl;

    @Autowired
    private IFeeReceiptDetailInnerServiceSMO feeReceiptDetailInnerServiceSMOImpl;

    @Autowired
    private IRepairUserInnerServiceSMO repairUserInnerServiceSMO;

    @Autowired
    private IRepairInnerServiceSMO repairInnerServiceSMO;

    @Autowired
    private IApplyRoomDiscountInnerServiceSMO applyRoomDiscountInnerServiceSMOImpl;

    @Autowired
    private IParkingSpaceInnerServiceSMO parkingSpaceInnerServiceSMOImpl;

    @Autowired
    private IAccountDetailBMO accountDetailBMOImpl;

    @Override
    public String getServiceCode() {
        return ServiceCodeConstant.SERVICE_CODE_PAY_FEE;
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

        HttpHeaders header = new HttpHeaders();
        dataFlowContext.getRequestCurrentHeaders().put(CommonConstant.HTTP_ORDER_TYPE_CD, "D");
        JSONArray businesses = new JSONArray();

        //??????????????????
        FeeReceiptPo feeReceiptPo = new FeeReceiptPo();
        FeeReceiptDetailPo feeReceiptDetailPo = new FeeReceiptDetailPo();
        businesses.add(feeBMOImpl.addFeeDetail(paramObj, dataFlowContext, feeReceiptDetailPo, feeReceiptPo));
        businesses.add(feeBMOImpl.modifyFee(paramObj, dataFlowContext));
        //????????????
        accountDetailBMOImpl.dealAccount(paramObj, dataFlowContext,businesses);
        //????????????
        if (paramObj.containsKey("selectDiscount")) {
            JSONObject discountBusiness = null;
            JSONArray selectDiscounts = paramObj.getJSONArray("selectDiscount");
            for (int discountIndex = 0; discountIndex < selectDiscounts.size(); discountIndex++) {
                discountBusiness = payFeeDetailDiscountBMOImpl.addPayFeeDetailDiscount(paramObj,
                        selectDiscounts.getJSONObject(discountIndex), dataFlowContext);
                if (discountBusiness != null) {
                    businesses.add(discountBusiness);
                }
            }
        }

        //????????????????????????
        if (paramObj.containsKey("carPayerObjType") && FeeDto.PAYER_OBJ_TYPE_CAR.equals(paramObj.getString("carPayerObjType"))) {
            Date feeEndTime = (Date) paramObj.get("carFeeEndTime");
            OwnerCarDto ownerCarDto = new OwnerCarDto();
            ownerCarDto.setCommunityId(paramObj.getString("communityId"));
            ownerCarDto.setCarId(paramObj.getString("carPayerObjId"));
            ownerCarDto.setCarTypeCd("1001"); //????????????
            List<OwnerCarDto> ownerCarDtos = ownerCarInnerServiceSMOImpl.queryOwnerCars(ownerCarDto);
            Assert.listOnlyOne(ownerCarDtos, "?????????????????????");
            //????????????id
            String psId = ownerCarDtos.get(0).getPsId();
            //??????????????????(1001 ???????????????2002 ????????????  3003 ????????????)
            String carState = ownerCarDtos.get(0).getState();
            if (!StringUtil.isEmpty(psId) && !StringUtil.isEmpty(carState) && carState.equals("3003")) {
                ParkingSpaceDto parkingSpaceDto = new ParkingSpaceDto();
                parkingSpaceDto.setPsId(psId);
                List<ParkingSpaceDto> parkingSpaceDtos = parkingSpaceInnerServiceSMOImpl.queryParkingSpaces(parkingSpaceDto);
                Assert.listOnlyOne(parkingSpaceDtos, "???????????????????????????");
                //??????????????????(?????? S????????? H ????????? F)
                String state = parkingSpaceDtos.get(0).getState();
                if (!StringUtil.isEmpty(state) && !state.equals("F")) {
                    throw new IllegalArgumentException("???????????????????????????????????????");
                }
            }
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
            RepairDto repairDto = new RepairDto();
            repairDto.setRepairId(feeAttrDtos.get(0).getValue());
            //??????????????????
            List<RepairDto> repairDtos = repairInnerServiceSMO.queryRepairs(repairDto);
            Assert.listOnlyOne(repairDtos, "?????????????????????");
            //??????????????????
            String repairChannel = repairDtos.get(0).getRepairChannel();
            RepairPoolPo repairPoolPo = new RepairPoolPo();
            repairPoolPo.setRepairId(feeAttrDtos.get(0).getValue());
            repairPoolPo.setCommunityId(paramObj.getString("communityId"));
            if (repairChannel.equals("Z")) { //??????????????????????????????????????????????????????
                repairPoolPo.setState(RepairDto.STATE_APPRAISE);
            } else { //?????????????????????????????????????????????????????????????????????
                repairPoolPo.setState(RepairDto.STATE_RETURN_VISIT);
            }
            business.getJSONObject(CommonConstant.HTTP_BUSINESS_DATAS).put(RepairPoolPo.class.getSimpleName(), BeanConvertUtil.beanCovertMap(repairPoolPo));
            businesses.add(business);
        }

        //????????????????????????
        if (feeAttrDtos != null && feeAttrDtos.size() > 0) {
            JSONObject business = JSONObject.parseObject("{\"datas\":{}}");
            business.put(CommonConstant.HTTP_BUSINESS_TYPE_CD, BusinessTypeConstant.BUSINESS_TYPE_UPDATE_REPAIR_USER);
            business.put(CommonConstant.HTTP_SEQ, DEFAULT_SEQ + 3);
            business.put(CommonConstant.HTTP_INVOKE_MODEL, CommonConstant.HTTP_INVOKE_MODEL_S);
            RepairDto repairDto = new RepairDto();
            repairDto.setRepairId(feeAttrDtos.get(0).getValue());
            //??????????????????
            List<RepairDto> repairDtos = repairInnerServiceSMO.queryRepairs(repairDto);
            Assert.listOnlyOne(repairDtos, "?????????????????????");
            //??????????????????
            String repairChannel = repairDtos.get(0).getRepairChannel();
            RepairUserDto repairUserDto = new RepairUserDto();
            repairUserDto.setRepairId(feeAttrDtos.get(0).getValue());
            repairUserDto.setState(RepairUserDto.STATE_PAY_FEE);
            //??????????????????????????????
            List<RepairUserDto> repairUserDtoList = repairUserInnerServiceSMO.queryRepairUsers(repairUserDto);
            Assert.listOnlyOne(repairUserDtoList, "???????????????");
            RepairUserPo repairUserPo = new RepairUserPo();
            repairUserPo.setRuId(repairUserDtoList.get(0).getRuId());
            if (repairChannel.equals("Z")) {  //???????????????????????????????????????????????????????????????????????????????????????
                repairUserPo.setState(RepairUserDto.STATE_FINISH_PAY_FEE);
                //????????????????????????????????????????????????
                repairUserPo.setEndTime(DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
                repairUserPo.setContext("?????????" + paramObj.getString("feePrice") + "???");
                //?????????????????????
                JSONObject object = JSONObject.parseObject("{\"datas\":{}}");
                object.put(CommonConstant.HTTP_BUSINESS_TYPE_CD, BusinessTypeConstant.BUSINESS_TYPE_SAVE_REPAIR_USER);
                object.put(CommonConstant.HTTP_SEQ, DEFAULT_SEQ + 4);
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
                businesses.add(object);
            } else {  //?????????????????????????????????????????????????????????????????????
                repairUserPo.setState(RepairUserDto.STATE_FINISH_PAY_FEE);
                //????????????????????????????????????????????????
                repairUserPo.setEndTime(DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
                repairUserPo.setContext("?????????" + paramObj.getString("feePrice") + "???");
            }
            business.getJSONObject(CommonConstant.HTTP_BUSINESS_DATAS).put(RepairUserPo.class.getSimpleName(), BeanConvertUtil.beanCovertMap(repairUserPo));
            businesses.add(business);
        }

        ResponseEntity<String> responseEntity = feeBMOImpl.callService(dataFlowContext, service.getServiceCode(), businesses);
        dataFlowContext.setResponseEntity(responseEntity);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return;
        }

        //????????????ID ??????????????????
        FeeReceiptDetailDto feeReceiptDetailDto = new FeeReceiptDetailDto();
        feeReceiptDetailDto.setDetailId(paramObj.getString("detailId"));
        feeReceiptDetailDto.setCommunityId(paramObj.getString("communityId"));
        List<FeeReceiptDetailDto> feeReceiptDetailDtos = feeReceiptDetailInnerServiceSMOImpl.queryFeeReceiptDetails(feeReceiptDetailDto);

        if (feeReceiptDetailDtos != null && feeReceiptDetailDtos.size() > 0) {
            dataFlowContext.setResponseEntity(ResultVo.createResponseEntity(feeReceiptDetailDtos.get(0)));
            return;
        }
        //?????????????????????????????????????????????????????????
        String selectDiscount = paramObj.getString("selectDiscount");
        JSONArray params = JSONArray.parseArray(selectDiscount);
        for (int index = 0; index < params.size(); index++) {
            JSONObject param = params.getJSONObject(index);
            if (!StringUtil.isEmpty(param.getString("ardId"))) {
                ApplyRoomDiscountPo applyRoomDiscountPo = new ApplyRoomDiscountPo();
                //????????????????????????
                applyRoomDiscountPo.setInUse("1");
                applyRoomDiscountPo.setArdId(param.getString("ardId"));
                applyRoomDiscountInnerServiceSMOImpl.updateApplyRoomDiscount(applyRoomDiscountPo);
            }
        }
        dataFlowContext.setResponseEntity(ResultVo.createResponseEntity(feeReceiptDetailDto));

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

        JSONObject paramInObj = JSONObject.parseObject(paramIn);
        Assert.hasLength(paramInObj.getString("communityId"), "??????ID????????????");
        Assert.hasLength(paramInObj.getString("cycles"), "??????????????????");
        Assert.hasLength(paramInObj.getString("receivedAmount"), "????????????????????????");
        Assert.hasLength(paramInObj.getString("feeId"), "??????ID????????????");

        //???????????? ???????????????????????????
        FeeDto feeDto = new FeeDto();
        feeDto.setFeeId(paramInObj.getString("feeId"));
        feeDto.setCommunityId(paramInObj.getString("communityId"));
        List<FeeDto> feeDtos = feeInnerServiceSMOImpl.queryFees(feeDto);

        Assert.listOnlyOne(feeDtos, "????????????ID??????");

        feeDto = feeDtos.get(0);

        if (FeeDto.STATE_FINISH.equals(feeDto.getState())) {
            throw new IllegalArgumentException("????????????????????????????????????");
        }

        Date endTime = feeDto.getEndTime();

        FeeConfigDto feeConfigDto = new FeeConfigDto();
        feeConfigDto.setConfigId(feeDto.getConfigId());
        feeConfigDto.setCommunityId(paramInObj.getString("communityId"));
        List<FeeConfigDto> feeConfigDtos = feeConfigInnerServiceSMOImpl.queryFeeConfigs(feeConfigDto);

        if (feeConfigDtos != null && feeConfigDtos.size() == 1) {
            try {
                Date configEndTime = DateUtil.getDateFromString(feeConfigDtos.get(0).getEndTime(), DateUtil.DATE_FORMATE_STRING_A);

                Date newDate = DateUtil.stepMonth(endTime, paramInObj.getInteger("cycles") - 1);

                if (newDate.getTime() > configEndTime.getTime()) {
                    throw new IllegalArgumentException("?????????????????? ??????????????????");
                }

            } catch (Exception e) {
                logger.error("????????????????????????", e);
            }
        }


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
