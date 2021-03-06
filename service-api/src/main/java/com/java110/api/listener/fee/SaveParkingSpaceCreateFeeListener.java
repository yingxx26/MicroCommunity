package com.java110.api.listener.fee;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.fee.IFeeBMO;
import com.java110.api.listener.AbstractServiceApiListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.fee.FeeAttrDto;
import com.java110.dto.fee.FeeConfigDto;
import com.java110.dto.fee.FeeDto;
import com.java110.dto.owner.OwnerCarDto;
import com.java110.dto.parking.ParkingSpaceDto;
import com.java110.dto.payFeeBatch.PayFeeBatchDto;
import com.java110.dto.user.UserDto;
import com.java110.entity.center.AppService;
import com.java110.intf.community.IParkingSpaceInnerServiceSMO;
import com.java110.intf.fee.IFeeConfigInnerServiceSMO;
import com.java110.intf.fee.IPayFeeBatchV1InnerServiceSMO;
import com.java110.intf.user.IOwnerCarInnerServiceSMO;
import com.java110.intf.user.IUserInnerServiceSMO;
import com.java110.po.payFeeBatch.PayFeeBatchPo;
import com.java110.utils.constant.CommonConstant;
import com.java110.utils.constant.ServiceCodeConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.DateUtil;
import com.java110.utils.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @ClassName SaveParkingSpaceCreateFeeListener
 * @Description TODO
 * @Author wuxw
 * @Date 2020/1/31 15:57
 * @Version 1.0
 * add by wuxw 2020/1/31
 **/
@Java110Listener("saveParkingSpaceCreateFeeListener")
public class SaveParkingSpaceCreateFeeListener extends AbstractServiceApiListener {
    private static Logger logger = LoggerFactory.getLogger(SaveParkingSpaceCreateFeeListener.class);


    @Autowired
    private IFeeBMO feeBMOImpl;

    private static final int DEFAULT_ADD_FEE_COUNT = 200;

    @Autowired
    private IParkingSpaceInnerServiceSMO parkingSpaceInnerServiceSMOImpl;

    @Autowired
    private IOwnerCarInnerServiceSMO ownerCarInnerServiceSMOImpl;

    @Autowired
    private IFeeConfigInnerServiceSMO feeConfigInnerServiceSMOImpl;

    @Autowired
    private IPayFeeBatchV1InnerServiceSMO payFeeBatchV1InnerServiceSMOImpl;

    @Autowired
    private IUserInnerServiceSMO userInnerServiceSMOImpl;

    @Override
    public String getServiceCode() {
        return ServiceCodeConstant.SERVICE_CODE_SAVE_PARKING_SPEC_CREATE_FEE;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        // super.validatePageInfo(pd);
        Assert.hasKeyAndValue(reqJson, "communityId", "???????????????ID");
        Assert.hasKeyAndValue(reqJson, "locationTypeCd", "?????????????????????");
        Assert.hasKeyAndValue(reqJson, "locationObjId", "?????????????????????");
        Assert.hasKeyAndValue(reqJson, "configId", "?????????????????????");
        Assert.hasKeyAndValue(reqJson, "storeId", "???????????????ID");
        //Assert.hasKeyAndValue(reqJson, "startTime", "???????????????????????????");
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {
        logger.debug("ServiceDataFlowEvent : {}", event);
        List<OwnerCarDto> ownerCarDtos = null;
        FeeConfigDto feeConfigDto = new FeeConfigDto();
        feeConfigDto.setCommunityId(reqJson.getString("communityId"));
        feeConfigDto.setConfigId(reqJson.getString("configId"));
        List<FeeConfigDto> feeConfigDtos = feeConfigInnerServiceSMOImpl.queryFeeConfigs(feeConfigDto);
        Assert.listOnlyOne(feeConfigDtos, "???????????????ID????????????????????????" + reqJson.getString("configId"));
        reqJson.put("feeTypeCd", feeConfigDtos.get(0).getFeeTypeCd());
        reqJson.put("feeFlag", feeConfigDtos.get(0).getFeeFlag());
        reqJson.put("configEndTime", feeConfigDtos.get(0).getEndTime());

        if (FeeDto.FEE_FLAG_ONCE.equals(feeConfigDtos.get(0).getFeeFlag()) && reqJson.containsKey("endTime")) {
            Date endTime = null;
            Date configEndTime = null;
            try {
                endTime = DateUtil.getDateFromString(reqJson.getString("endTime"), DateUtil.DATE_FORMATE_STRING_B);
                configEndTime = DateUtil.getDateFromString(feeConfigDtos.get(0).getEndTime(), DateUtil.DATE_FORMATE_STRING_A);
                if (endTime.getTime() > configEndTime.getTime()) {
                    throw new IllegalArgumentException("???????????????????????????????????????");
                }
            } catch (ParseException e) {
                throw new IllegalArgumentException("??????????????????" + reqJson.getString("endTime"));
            }
        }
        //????????????
        generatorBatch(reqJson);
        //??????????????????
        OwnerCarDto ownerCarDto = new OwnerCarDto();

        if ("1000".equals(reqJson.getString("locationTypeCd"))) {//??????
//            ownerCarDto.setCommunityId(reqJson.getString("communityId"));
//            ownerCarDto.setValid("1");
//            ownerCarDtos = ownerCarInnerServiceSMOImpl.queryOwnerCars(ownerCarDto);
            reqJson.put("locationObjId", "");//?????????
            ownerCarDtos = getOwnerCarByParkingArea(reqJson);
        } else if ("2000".equals(reqJson.getString("locationTypeCd"))) {//??????
            //ParkingSpaceDto parkingSpaceDto = new ParkingSpaceDto();
            ownerCarDto.setCarTypeCd("1001"); //1001 ????????????   1002 ????????????
            ownerCarDto.setCommunityId(reqJson.getString("communityId"));
            ownerCarDto.setCarId(reqJson.getString("locationObjId"));
            ownerCarDtos = ownerCarInnerServiceSMOImpl.queryOwnerCars(ownerCarDto);
        } else if ("3000".equals(reqJson.getString("locationTypeCd"))) {//?????????
            //ParkingSpaceDto parkingSpaceDto = new ParkingSpaceDto();
            ownerCarDtos = getOwnerCarByParkingArea(reqJson);
        } else {
            throw new IllegalArgumentException("??????????????????");
        }

        if (ownerCarDtos == null || ownerCarDtos.size() < 1) {
            throw new IllegalArgumentException("??????????????????????????????");
        }

        dealParkingSpaceFee(ownerCarDtos, context, reqJson, event);
    }

    private List<OwnerCarDto> getOwnerCarByParkingArea(JSONObject reqJson) {
        List<OwnerCarDto> ownerCarDtos = new ArrayList<>();
        ParkingSpaceDto parkingSpaceDto = new ParkingSpaceDto();
        parkingSpaceDto.setCommunityId(reqJson.getString("communityId"));
        List<String> states = new ArrayList<>();
        JSONArray carStates = reqJson.getJSONArray("carState");
        if (carStates.size() < 1) {
            throw new IllegalArgumentException("?????????????????????");
        }

        for (int carStateIndex = 0; carStateIndex < carStates.size(); carStateIndex++) {
            states.add(carStates.getString(carStateIndex));
        }
        parkingSpaceDto.setStates(states.toArray(new String[states.size()]));
        parkingSpaceDto.setPaId(reqJson.getString("locationObjId"));
        List<ParkingSpaceDto> parkingSpaceDtos = parkingSpaceInnerServiceSMOImpl.queryParkingSpaces(parkingSpaceDto);

        if (parkingSpaceDtos == null || parkingSpaceDtos.size() < 1) {
            return null;
        }
        List<String> psIds = new ArrayList<>();
        for (int parkingSpaceIndex = 0; parkingSpaceIndex < parkingSpaceDtos.size(); parkingSpaceIndex++) {

            psIds.add(parkingSpaceDtos.get(parkingSpaceIndex).getPsId());

            if (parkingSpaceIndex % DEFAULT_ADD_FEE_COUNT == 0 && parkingSpaceIndex != 0) {

                queryOwnerCar(reqJson, psIds, ownerCarDtos);

                psIds = new ArrayList<>();
            }
        }
        if (psIds.size() > 0) {
            queryOwnerCar(reqJson, psIds, ownerCarDtos);
        }

        return ownerCarDtos;
    }

    /**
     * ???????????????
     *
     * @param reqJson
     */
    private void generatorBatch(JSONObject reqJson) {
        PayFeeBatchPo payFeeBatchPo = new PayFeeBatchPo();
        payFeeBatchPo.setBatchId(GenerateCodeFactory.getGeneratorId("12"));
        payFeeBatchPo.setCommunityId(reqJson.getString("communityId"));
        payFeeBatchPo.setCreateUserId(reqJson.getString("userId"));
        UserDto userDto = new UserDto();
        userDto.setUserId(reqJson.getString("userId"));
        List<UserDto> userDtos = userInnerServiceSMOImpl.getUsers(userDto);

        Assert.listOnlyOne(userDtos, "???????????????");
        payFeeBatchPo.setCreateUserName(userDtos.get(0).getUserName());
        payFeeBatchPo.setState(PayFeeBatchDto.STATE_NORMAL);
        payFeeBatchPo.setMsg("??????");
        int flag = payFeeBatchV1InnerServiceSMOImpl.savePayFeeBatch(payFeeBatchPo);

        if (flag < 1) {
            throw new IllegalArgumentException("??????????????????");
        }

        reqJson.put("batchId", payFeeBatchPo.getBatchId());
    }

    private void queryOwnerCar(JSONObject reqJson, List<String> psIds, List<OwnerCarDto> ownerCarDtos) {
        OwnerCarDto ownerCarDto = new OwnerCarDto();
        ownerCarDto.setCommunityId(reqJson.getString("communityId"));
        ownerCarDto.setPsIds(psIds.toArray(new String[psIds.size()]));
        List<OwnerCarDto> townerCarDtos = ownerCarInnerServiceSMOImpl.queryOwnerCars(ownerCarDto);
        ownerCarDtos.addAll(townerCarDtos);
    }

    private void dealParkingSpaceFee(List<OwnerCarDto> ownerCarDtos, DataFlowContext context, JSONObject reqJson, ServiceDataFlowEvent event) {

        AppService service = event.getAppService();


        HttpHeaders header = new HttpHeaders();
        context.getRequestCurrentHeaders().put(CommonConstant.HTTP_ORDER_TYPE_CD, "D");
        JSONArray businesses = new JSONArray();
        JSONObject paramInObj = null;
        ResponseEntity<String> responseEntity = null;
        int failParkingSpaces = 0;
        //??????????????????
        for (int ownerCarIndex = 0; ownerCarIndex < ownerCarDtos.size(); ownerCarIndex++) {

            businesses.add(feeBMOImpl.addFee(ownerCarDtos.get(ownerCarIndex), reqJson, context));
            if (!StringUtil.isEmpty(ownerCarDtos.get(ownerCarIndex).getOwnerId())) {
                if (FeeDto.FEE_FLAG_ONCE.equals(reqJson.getString("feeFlag"))) {
                    businesses.add(feeBMOImpl.addFeeAttr(reqJson, context, FeeAttrDto.SPEC_CD_ONCE_FEE_DEADLINE_TIME,
                            reqJson.containsKey("endTime") ? reqJson.getString("endTime") : reqJson.getString("configEndTime")));
                }
                businesses.add(feeBMOImpl.addFeeAttr(reqJson, context, FeeAttrDto.SPEC_CD_OWNER_ID, ownerCarDtos.get(ownerCarIndex).getOwnerId()));
                businesses.add(feeBMOImpl.addFeeAttr(reqJson, context, FeeAttrDto.SPEC_CD_OWNER_LINK, ownerCarDtos.get(ownerCarIndex).getLink()));
                businesses.add(feeBMOImpl.addFeeAttr(reqJson, context, FeeAttrDto.SPEC_CD_OWNER_NAME, ownerCarDtos.get(ownerCarIndex).getOwnerName()));
            }

            if (ownerCarIndex % DEFAULT_ADD_FEE_COUNT == 0 && ownerCarIndex != 0) {

                responseEntity = feeBMOImpl.callService(context, service.getServiceCode(), businesses);

                if (responseEntity.getStatusCode() != HttpStatus.OK) {
                    failParkingSpaces += businesses.size();
                }

                businesses = new JSONArray();
            }
        }
        if (businesses != null && businesses.size() > 0) {

            responseEntity = feeBMOImpl.callService(context, service.getServiceCode(), businesses);
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                failParkingSpaces += businesses.size();
            }
        }

        JSONObject paramOut = new JSONObject();
        paramOut.put("totalCar", ownerCarDtos.size());
        paramOut.put("successCar", ownerCarDtos.size() - failParkingSpaces);
        paramOut.put("errorCar", failParkingSpaces);

        responseEntity = new ResponseEntity<>(paramOut.toJSONString(), HttpStatus.OK);

        context.setResponseEntity(responseEntity);
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    public IParkingSpaceInnerServiceSMO getParkingSpaceInnerServiceSMOImpl() {
        return parkingSpaceInnerServiceSMOImpl;
    }

    public void setParkingSpaceInnerServiceSMOImpl(IParkingSpaceInnerServiceSMO parkingSpaceInnerServiceSMOImpl) {
        this.parkingSpaceInnerServiceSMOImpl = parkingSpaceInnerServiceSMOImpl;
    }
}
