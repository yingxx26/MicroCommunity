package com.java110.api.listener.owner;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.parkingSpace.IParkingSpaceBMO;
import com.java110.api.listener.AbstractServiceApiPlusListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.owner.OwnerCarDto;
import com.java110.intf.community.IParkingSpaceInnerServiceSMO;
import com.java110.intf.fee.IFeeConfigInnerServiceSMO;
import com.java110.intf.user.IOwnerCarInnerServiceSMO;
import com.java110.po.car.OwnerCarPo;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.ServiceCodeConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.util.List;

/**
 * @ClassName SaveParkingSpaceListener
 * @Description 保存小区楼信息
 * @Author wuxw
 * @Date 2019/4/26 14:51
 * @Version 1.0
 * add by wuxw 2019/4/26
 **/

@Java110Listener("saveOwnerCarMemberListener")
public class SaveOwnerCarMemberListener extends AbstractServiceApiPlusListener {


    private static Logger logger = LoggerFactory.getLogger(SaveOwnerCarMemberListener.class);

    @Autowired
    private IParkingSpaceBMO parkingSpaceBMOImpl;

    @Autowired
    private IFeeConfigInnerServiceSMO feeConfigInnerServiceSMOImpl;

    @Autowired
    private IParkingSpaceInnerServiceSMO parkingSpaceInnerServiceSMOImpl;

    @Autowired
    private IOwnerCarInnerServiceSMO ownerCarInnerServiceSMOImpl;

    @Override
    public String getServiceCode() {
        return ServiceCodeConstant.SERVICE_CODE_SAVE_OWNER_CAR_MEMBER;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }


    /**
     * {"communityId":"7020181217000001",
     * "data":[
     * {
     * "flowComponent":"viewSelectParkingSpace","parkingSpaceFlag":"","num":"lbwnb","area":"11.00","state":"F",
     * "stateName":"空闲 ","remark":"","areaNum":"3","psId":"792020082657940123","typeCd":"1001"},
     * {"flowComponent":"viewOwnerInfo","viewOwnerFlag":"","ownerId":"772020082849180061","name":"王鹏飞","age":"28",
     * "sex":"0","userName":"wuxw","remark":"","idCard":"340803199211182134","link":"17721036947","ownerPhoto":"/img/noPhoto.jpg",
     * "showCallBackButton":"false"},
     * {"flowComponent":"addCar","carNum":"青AGK916","carBrand":"传祺","carType":"9901","carColor":"白色","carRemark":"",
     * "startTime":"2020-08-29 14:55:04","endTime":"2021-08-29 14:55:04","carNumType":"H","index":2}
     * ]}
     *
     * @param event   事件对象
     * @param reqJson 请求报文数据
     */
    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        Assert.jsonObjectHaveKey(reqJson, "communityId", "未包含小区ID");
        Assert.jsonObjectHaveKey(reqJson, "carId", "请求报文中未包含carId");
        Assert.jsonObjectHaveKey(reqJson, "carNum", "请求报文中未包含carNum");
        Assert.jsonObjectHaveKey(reqJson, "carBrand", "请求报文中未包含carBrand");
        Assert.jsonObjectHaveKey(reqJson, "carType", "请求报文中未包含carType");
        Assert.jsonObjectHaveKey(reqJson, "carColor", "未包含carColor");

        //校验车牌号是否存在
        OwnerCarDto ownerCarDto = new OwnerCarDto();
        ownerCarDto.setCommunityId(reqJson.getString("communityId"));
        ownerCarDto.setCarNum(reqJson.getString("carNum"));
        int count = ownerCarInnerServiceSMOImpl.queryOwnerCarsCount(ownerCarDto);

        if (count > 0) {
            throw new IllegalArgumentException("车辆已存在");
        }
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {

        //校验车牌号是否存在
        OwnerCarDto ownerCarDto = new OwnerCarDto();
        ownerCarDto.setCommunityId(reqJson.getString("communityId"));
        ownerCarDto.setCarTypeCd("1001"); //业主车辆
        ownerCarDto.setCarId(reqJson.getString("carId"));
        ownerCarDto.setStatusCd("0");
        List<OwnerCarDto> ownerCarDtos = ownerCarInnerServiceSMOImpl.queryOwnerCars(ownerCarDto);
        if (ownerCarDtos.isEmpty() || ownerCarDtos.size() == 0) {
            throw new IllegalArgumentException("主车辆不存在");
        }

        JSONObject tmpOwnerCar = JSONObject.parseObject(JSONObject.toJSONString(ownerCarDtos.get(0)));
        tmpOwnerCar.putAll(reqJson);
        tmpOwnerCar.put("startTime", DateUtil.getFormatTimeString(ownerCarDtos.get(0).getStartTime(), DateUtil.DATE_FORMATE_STRING_A));
        tmpOwnerCar.put("endTime", DateUtil.getFormatTimeString(ownerCarDtos.get(0).getEndTime(), DateUtil.DATE_FORMATE_STRING_A));
        tmpOwnerCar.put("memberId", GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_carId));

        OwnerCarPo ownerCarPo = BeanConvertUtil.covertBean(tmpOwnerCar, OwnerCarPo.class);
        ownerCarPo.setState(OwnerCarDto.STATE_NORMAL);
        ownerCarPo.setCarTypeCd(OwnerCarDto.CAR_TYPE_MEMBER);
        super.insert(context, ownerCarPo, BusinessTypeConstant.BUSINESS_TYPE_SAVE_OWNER_CAR);

    }


    @Override
    public int getOrder() {
        return 0;
    }

    public IFeeConfigInnerServiceSMO getFeeConfigInnerServiceSMOImpl() {
        return feeConfigInnerServiceSMOImpl;
    }

    public void setFeeConfigInnerServiceSMOImpl(IFeeConfigInnerServiceSMO feeConfigInnerServiceSMOImpl) {
        this.feeConfigInnerServiceSMOImpl = feeConfigInnerServiceSMOImpl;
    }

    public IParkingSpaceInnerServiceSMO getParkingSpaceInnerServiceSMOImpl() {
        return parkingSpaceInnerServiceSMOImpl;
    }

    public void setParkingSpaceInnerServiceSMOImpl(IParkingSpaceInnerServiceSMO parkingSpaceInnerServiceSMOImpl) {
        this.parkingSpaceInnerServiceSMOImpl = parkingSpaceInnerServiceSMOImpl;
    }
}
