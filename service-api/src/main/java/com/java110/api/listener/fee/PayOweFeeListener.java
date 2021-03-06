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
import com.java110.dto.feeReceipt.FeeReceiptDetailDto;
import com.java110.dto.repair.RepairDto;
import com.java110.dto.repair.RepairUserDto;
import com.java110.entity.center.AppService;
import com.java110.intf.community.IParkingSpaceInnerServiceSMO;
import com.java110.intf.community.IRepairInnerServiceSMO;
import com.java110.intf.community.IRepairUserInnerServiceSMO;
import com.java110.intf.community.IRoomInnerServiceSMO;
import com.java110.intf.fee.*;
import com.java110.intf.user.IOwnerCarInnerServiceSMO;
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

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName payOweFeeListener
 * @Description TODO ????????????
 * @Author wuxw
 * @Date 2019/6/3 13:46
 * @Version 1.0
 * add by wuxw 2019/6/3
 **/
@Java110Listener("payOweFeeListener")
public class PayOweFeeListener extends AbstractServiceApiDataFlowListener {

    private static Logger logger = LoggerFactory.getLogger(PayOweFeeListener.class);

    @Autowired
    private IFeeBMO feeBMOImpl;

    @Autowired
    private IParkingSpaceInnerServiceSMO parkingSpaceInnerServiceSMOImpl;
    @Autowired
    private IFeeInnerServiceSMO feeInnerServiceSMOImpl;

    @Autowired
    private IFeeAttrInnerServiceSMO feeAttrInnerServiceSMOImpl;

    @Autowired
    private IRoomInnerServiceSMO roomInnerServiceSMOImpl;

    @Autowired
    private IFeeConfigInnerServiceSMO feeConfigInnerServiceSMOImpl;

    @Autowired
    private IFeeReceiptInnerServiceSMO feeReceiptInnerServiceSMOImpl;

    @Autowired
    private IFeeReceiptDetailInnerServiceSMO feeReceiptDetailInnerServiceSMOImpl;

    @Autowired
    private IRepairUserInnerServiceSMO repairUserInnerServiceSMO;

    @Autowired
    private IRepairInnerServiceSMO repairInnerServiceSMO;


    @Override
    public String getServiceCode() {
        return ServiceCodeConstant.SERVICE_CODE_PAY_OWE_FEE;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }

    @Override
    public void soService(ServiceDataFlowEvent event) {

        logger.debug("ServiceDataFlowEvent : {}", event);

        DataFlowContext dataFlowContext = event.getDataFlowContext();
        AppService service = event.getAppService();

        String paramIn = dataFlowContext.getReqData();

        //????????????
        validate(paramIn);
        JSONObject paramObj = JSONObject.parseObject(paramIn);
        logger.info("======??????????????????======???"+JSONArray.toJSONString(paramObj));
        HttpHeaders header = new HttpHeaders();
        dataFlowContext.getRequestCurrentHeaders().put(CommonConstant.HTTP_ORDER_TYPE_CD, "D");
        JSONArray businesses = new JSONArray();
        //??????????????????
        List<FeeReceiptPo> feeReceiptPos = new ArrayList<>();
        List<FeeReceiptDetailPo> feeReceiptDetailPos = new ArrayList<>();
        JSONArray fees = paramObj.getJSONArray("fees");
        JSONObject feeObj = null;
        for (int feeIndex = 0; feeIndex < fees.size(); feeIndex++) {
            feeObj = fees.getJSONObject(feeIndex);
            feeObj.put("communityId", paramObj.getString("communityId"));
            String remark = paramObj.getString("remark");
            String paySource = "?????????????????????";
            if (!StringUtil.isEmpty(remark)) {
                remark = "-" + remark;
            } else {
                remark = "";
            }
            feeObj.put("remark", paySource + remark);

            getFeeReceiptDetailPo(dataFlowContext, feeObj, businesses, feeReceiptDetailPos, feeReceiptPos);
        }

        ResponseEntity<String> responseEntity = feeBMOImpl.callService(dataFlowContext, service.getServiceCode(), businesses);
        dataFlowContext.setResponseEntity(responseEntity);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return;
        }

        //?????????????????? ???????????????????????? ?????????????????????????????????????????? ??????????????? ????????????????????? ???????????? ???????????? ???????????????????????????????????? ?????????????????????
//        feeReceiptDetailInnerServiceSMOImpl.saveFeeReceiptDetails(feeReceiptDetailPos);
//
//        feeReceiptInnerServiceSMOImpl.saveFeeReceipts(feeReceiptPos);

        //????????????ID ??????????????????
        List<String> detailIds = new ArrayList<>();

        for (FeeReceiptDetailPo feeReceiptDetailPo : feeReceiptDetailPos) {
            detailIds.add(feeReceiptDetailPo.getDetailId());
        }

        FeeReceiptDetailDto feeReceiptDetailDto = new FeeReceiptDetailDto();
        feeReceiptDetailDto.setDetailIds(detailIds.toArray(new String[detailIds.size()]));
        feeReceiptDetailDto.setCommunityId(paramObj.getString("communityId"));
        try {
            Thread.currentThread().sleep(2000);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        List<FeeReceiptDetailDto> feeReceiptDetailDtos = feeReceiptDetailInnerServiceSMOImpl.queryFeeReceiptDetails(feeReceiptDetailDto);


        dataFlowContext.setResponseEntity(ResultVo.createResponseEntity(feeReceiptDetailDtos));
    }

    private void getFeeReceiptDetailPo(DataFlowContext dataFlowContext, JSONObject paramObj, JSONArray businesses, List<FeeReceiptDetailPo> feeReceiptDetailPos, List<FeeReceiptPo> feeReceiptPos) {
        if (!paramObj.containsKey("primeRate")) {
            paramObj.put("primeRate", "6");
        }
        String appId = dataFlowContext.getAppId();
        logger.info("======????????????======???" + appId + "+======+" + paramObj.containsKey("primeRate")+"======:"+JSONArray.toJSONString(dataFlowContext));
        if (AppDto.OWNER_WECHAT_PAY.equals(appId)) {  //????????????????????????????????????????????????????????????????????????
            paramObj.put("remark", "????????????");
        }
        paramObj.put("state","1400");
        businesses.add(feeBMOImpl.addOweFeeDetail(paramObj, dataFlowContext, feeReceiptDetailPos, feeReceiptPos));
        businesses.add(feeBMOImpl.modifyOweFee(paramObj, dataFlowContext));

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
        //?????? ??????????????????
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
                repairUserPo.setContext("?????????" + paramObj.getString("receivedAmount") + "???");
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
                repairUserPo.setContext("?????????" + paramObj.getString("receivedAmount") + "???");
            }
            business.getJSONObject(CommonConstant.HTTP_BUSINESS_DATAS).put(RepairUserPo.class.getSimpleName(), BeanConvertUtil.beanCovertMap(repairUserPo));
            businesses.add(business);
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
        JSONObject reqJson = JSONObject.parseObject(paramIn);
        Assert.hasKey(reqJson, "fees", "????????????????????????????????????");

        JSONArray fees = reqJson.getJSONArray("fees");

        JSONObject feeObject = null;

        for (int feeIndex = 0; feeIndex < fees.size(); feeIndex++) {
            feeObject = fees.getJSONObject(feeIndex);
            Assert.hasKeyAndValue(feeObject, "feeId", "?????????????????????");
            Assert.hasKeyAndValue(feeObject, "startTime", "?????????????????????");
            Assert.hasKeyAndValue(feeObject, "endTime", "?????????????????????");
            Assert.hasKeyAndValue(feeObject, "receivedAmount", "?????????????????????");
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
