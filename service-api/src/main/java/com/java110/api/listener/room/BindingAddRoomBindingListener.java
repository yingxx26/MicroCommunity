package com.java110.api.listener.room;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.room.IRoomBMO;
import com.java110.api.listener.AbstractServiceApiPlusListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.FloorDto;
import com.java110.dto.RoomDto;
import com.java110.dto.UnitDto;
import com.java110.intf.community.IFloorInnerServiceSMO;
import com.java110.intf.community.IRoomInnerServiceSMO;
import com.java110.intf.community.IUnitInnerServiceSMO;
import com.java110.utils.constant.CommonConstant;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.constant.ServiceCodeAddRoomBindingConstant;
import com.java110.utils.exception.ListenerExecuteException;
import com.java110.utils.util.Assert;
import com.java110.utils.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * 保存小区侦听
 * add by wuxw 2019-06-30
 */
@Java110Listener("bindingAddRoomBindingListener")
public class BindingAddRoomBindingListener extends AbstractServiceApiPlusListener {

    @Autowired
    private IRoomBMO roomBMOImpl;

    @Autowired
    private IUnitInnerServiceSMO unitInnerServiceSMOImpl;

    @Autowired
    private IRoomInnerServiceSMO roomInnerServiceSMOImpl;

    @Autowired
    private IFloorInnerServiceSMO floorInnerServiceSMOImpl;

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        //Assert.hasKeyAndValue(reqJson, "xxx", "xxx");
        JSONArray infos = reqJson.getJSONArray("data");

        Assert.hasKeyByFlowData(infos, "addRoomView", "roomNum", "必填，请填写房屋编号");
        Assert.hasKeyByFlowData(infos, "addRoomView", "communityId", "必填，请填写房屋小区信息");
        Assert.hasKeyByFlowData(infos, "addRoomView", "layer", "必填，请填写房屋楼层");
        Assert.hasKeyByFlowData(infos, "addRoomView", "section", "必填，请填写房屋楼层");
        Assert.hasKeyByFlowData(infos, "addRoomView", "apartment", "必填，请选择房屋户型");
        Assert.hasKeyByFlowData(infos, "addRoomView", "builtUpArea", "必填，请填写房屋建筑面积");
        Assert.hasKeyByFlowData(infos, "addRoomView", "feeCoefficient", "必填，请填写房屋每平米单价");
        Assert.hasKeyByFlowData(infos, "addRoomView", "state", "必填，请选择房屋状态");

        JSONObject addRoomView = null;
        for (int roomIndex = 0; roomIndex < infos.size(); roomIndex++) {
            JSONObject _info = infos.getJSONObject(roomIndex);
            if (_info.containsKey("addRoomView") && _info.getString("flowComponent").equals("addRoomView")) {
                addRoomView = _info;
                break;
            }
        }

        if (addRoomView == null) {
            return;
        }

        Assert.judgeAttrValue(addRoomView);
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {
        JSONArray infos = reqJson.getJSONArray("data");
        JSONObject viewFloorInfo = getObj(infos, "viewFloorInfo");
        JSONObject viewUnitInfo = getObj(infos, "viewUnitInfo");
        JSONObject addRoomView = getObj(infos, "addRoomView");
        //楼栋id
        String floorId = "";
        //单元id
        String unitId = "";
        //房屋编号
        String roomNum = "";
        if (viewFloorInfo.containsKey("floorId") && StringUtil.isEmpty(viewFloorInfo.getString("floorId"))
                && viewUnitInfo.containsKey("unitId") && StringUtil.isEmpty(viewUnitInfo.getString("unitId"))
                && addRoomView.containsKey("roomNum") && StringUtil.isEmpty(addRoomView.getString("roomNum"))) {
            floorId = viewFloorInfo.getString("floorId");
            unitId = viewUnitInfo.getString("unitId");
            roomNum = addRoomView.getString("roomNum");
            //查询楼栋下单元信息
            UnitDto unitDto = new UnitDto();
            unitDto.setFloorId(floorId);
            unitDto.setUnitId(unitId);
            List<UnitDto> unitDtos = unitInnerServiceSMOImpl.queryUnits(unitDto);
            Assert.listOnlyOne(unitDtos, "查询单元信息错误！");
            RoomDto roomDto = new RoomDto();
            roomDto.setUnitId(unitDtos.get(0).getUnitId());
            roomDto.setRoomNum(roomNum);
            List<RoomDto> roomDtos = roomInnerServiceSMOImpl.queryRooms(roomDto);
            if (roomDtos != null && roomDtos.size() > 0) {
                throw new IllegalArgumentException("该房屋已经存在！");
            }
        }
        if (!hasKey(viewFloorInfo, "floorId")) {
            //获取楼栋编码
            String floorNum = viewFloorInfo.getString("floorNum");
            //获取小区id
            String communityId = viewFloorInfo.getString("communityId");
            //判断楼栋编号是否重复
            FloorDto floorDto = new FloorDto();
            floorDto.setFloorNum(floorNum);
            floorDto.setCommunityId(communityId);
            int floorCount = floorInnerServiceSMOImpl.queryFloorsCount(floorDto);
            if (floorCount > 0) {
                throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_ERROR, "楼栋已经存在");
            }
            viewFloorInfo.put("floorId", GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_floorId));
            viewFloorInfo.put("userId", context.getRequestCurrentHeaders().get(CommonConstant.HTTP_USER_ID));
            roomBMOImpl.addBusinessFloor(viewFloorInfo, context);
            roomBMOImpl.addCommunityMember(viewFloorInfo, context);
        }
        if (!hasKey(viewUnitInfo, "unitId")) {
            if (viewFloorInfo.containsKey("floorId") && !StringUtil.isEmpty(viewFloorInfo.getString("floorId"))) { //如果前端选择的楼栋，而不是新增楼栋，就判断该楼栋下单元是否重复
                //获取楼栋id
                String floorId1 = viewFloorInfo.getString("floorId");
                //获取小区id
                String communityId = viewUnitInfo.getString("communityId");
                //获取单元编号
                String unitNum = viewUnitInfo.getString("unitNum");
                UnitDto unitDto = new UnitDto();
                unitDto.setFloorId(floorId1);
                unitDto.setCommunityId(communityId);
                unitDto.setUnitNum(unitNum);
                int unitCount = unitInnerServiceSMOImpl.queryUnitsCount(unitDto);
                if (unitCount > 0) {
                    throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_ERROR, "单元已经存在");
                }
            }
            viewUnitInfo.put("unitId", GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_unitId));
            viewUnitInfo.put("userId", context.getRequestCurrentHeaders().get(CommonConstant.HTTP_USER_ID));
            viewUnitInfo.put("floorId", viewFloorInfo.getString("floorId"));
            roomBMOImpl.addBusinessUnit(viewUnitInfo, context);
        }
        if (!hasKey(addRoomView, "roomId")) {
            if (viewUnitInfo.containsKey("unitId") && !StringUtil.isEmpty(viewUnitInfo.getString("unitId"))) { //如果前端选择的单元，而不是添加的，就判断该楼栋单元下房屋是否重复
                //获取单元id
                String unitId1 = viewUnitInfo.getString("unitId");
                RoomDto roomDto = new RoomDto();
                roomDto.setUnitId(unitId1);
                roomDto.setRoomNum(addRoomView.getString("roomNum"));
                roomDto.setCommunityId(addRoomView.getString("communityId"));
                int roomCount = roomInnerServiceSMOImpl.queryRoomsCount(roomDto);
                if (roomCount > 0) {
                    throw new ListenerExecuteException(ResponseConstant.RESULT_CODE_ERROR, "房屋已经存在");
                }
            }
            addRoomView.put("roomId", GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_roomId));
            addRoomView.put("userId", context.getRequestCurrentHeaders().get(CommonConstant.HTTP_USER_ID));
            addRoomView.put("unitId", viewUnitInfo.getString("unitId"));
            addRoomView.put("roomType", RoomDto.ROOM_TYPE_ROOM);
            roomBMOImpl.addBusinessRoom(addRoomView, context);
            //处理房屋属性
            dealRoomAttr(addRoomView, context);
        }

        commit(context);

        JSONObject paramOutObj = new JSONObject();
        paramOutObj.put("floorId", viewFloorInfo.getString("floorId"));
        paramOutObj.put("unitId", viewUnitInfo.getString("unitId"));
        paramOutObj.put("roomId", addRoomView.getString("roomId"));
        ResponseEntity<String> responseEntity = null;
        if (context.getResponseEntity().getStatusCode() == HttpStatus.OK) {
            responseEntity = new ResponseEntity<String>(paramOutObj.toJSONString(), HttpStatus.OK);
        }
        context.setResponseEntity(responseEntity);
    }

    private void dealRoomAttr(JSONObject addRoomView, DataFlowContext context) {

        if (!addRoomView.containsKey("attrs")) {
            return;
        }

        JSONArray attrs = addRoomView.getJSONArray("attrs");
        if (attrs.size() < 1) {
            return;
        }


        JSONObject attr = null;
        for (int attrIndex = 0; attrIndex < attrs.size(); attrIndex++) {
            attr = attrs.getJSONObject(attrIndex);
            attr.put("roomId", addRoomView.getString("roomId"));
            roomBMOImpl.addRoomAttr(attr, context);
        }

    }

    @Override
    public String getServiceCode() {
        return ServiceCodeAddRoomBindingConstant.BINDING_ADDROOMBINDING;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    private boolean hasKey(JSONObject info, String key) {
        if (!info.containsKey(key)
                || StringUtil.isEmpty(info.getString(key))
                || info.getString(key).startsWith("-")) {
            return false;
        }
        return true;

    }

    private JSONObject getObj(JSONArray infos, String flowComponent) {

        JSONObject serviceInfo = null;

        for (int infoIndex = 0; infoIndex < infos.size(); infoIndex++) {

            Assert.hasKeyAndValue(infos.getJSONObject(infoIndex), "flowComponent", "未包含服务流程组件名称");

            if (flowComponent.equals(infos.getJSONObject(infoIndex).getString("flowComponent"))) {
                serviceInfo = infos.getJSONObject(infoIndex);
                Assert.notNull(serviceInfo, "未包含服务信息");
                return serviceInfo;
            }
        }

        throw new IllegalArgumentException("未找到组件编码为【" + flowComponent + "】数据");
    }


}
