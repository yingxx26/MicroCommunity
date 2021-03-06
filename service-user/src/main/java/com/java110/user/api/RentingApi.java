package com.java110.user.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.utils.StringUtils;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.rentingConfig.RentingConfigDto;
import com.java110.dto.rentingPool.RentingPoolDto;
import com.java110.dto.rentingPoolAttr.RentingPoolAttrDto;
import com.java110.po.rentingConfig.RentingConfigPo;
import com.java110.po.rentingPool.RentingPoolPo;
import com.java110.po.rentingPoolAttr.RentingPoolAttrPo;
import com.java110.po.rentingPoolFlow.RentingPoolFlowPo;
import com.java110.user.bmo.rentingConfig.IDeleteRentingConfigBMO;
import com.java110.user.bmo.rentingConfig.IGetRentingConfigBMO;
import com.java110.user.bmo.rentingConfig.ISaveRentingConfigBMO;
import com.java110.user.bmo.rentingConfig.IUpdateRentingConfigBMO;
import com.java110.user.bmo.rentingPool.*;
import com.java110.user.bmo.rentingPoolAttr.IDeleteRentingPoolAttrBMO;
import com.java110.user.bmo.rentingPoolAttr.IGetRentingPoolAttrBMO;
import com.java110.user.bmo.rentingPoolAttr.ISaveRentingPoolAttrBMO;
import com.java110.user.bmo.rentingPoolAttr.IUpdateRentingPoolAttrBMO;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/renting")
public class RentingApi {

    @Autowired
    private ISaveRentingConfigBMO saveRentingConfigBMOImpl;
    @Autowired
    private IUpdateRentingConfigBMO updateRentingConfigBMOImpl;
    @Autowired
    private IDeleteRentingConfigBMO deleteRentingConfigBMOImpl;

    @Autowired
    private IGetRentingConfigBMO getRentingConfigBMOImpl;


    @Autowired
    private ISaveRentingPoolBMO saveRentingPoolBMOImpl;
    @Autowired
    private IUpdateRentingPoolBMO updateRentingPoolBMOImpl;
    @Autowired
    private IDeleteRentingPoolBMO deleteRentingPoolBMOImpl;

    @Autowired
    private IGetRentingPoolBMO getRentingPoolBMOImpl;

    @Autowired
    private ISaveRentingPoolAttrBMO saveRentingPoolAttrBMOImpl;
    @Autowired
    private IUpdateRentingPoolAttrBMO updateRentingPoolAttrBMOImpl;
    @Autowired
    private IDeleteRentingPoolAttrBMO deleteRentingPoolAttrBMOImpl;

    @Autowired
    private IGetRentingPoolAttrBMO getRentingPoolAttrBMOImpl;

    @Autowired
    private IAuditRentingBMO auditRentingBMOImpl;

    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /renting/saveRentingConfig
     * @path /app/renting/saveRentingConfig
     */
    @RequestMapping(value = "/saveRentingConfig", method = RequestMethod.POST)
    public ResponseEntity<String> saveRentingConfig(@RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "rentingType", "????????????????????????rentingType");
        Assert.hasKeyAndValue(reqJson, "rentingFormula", "????????????????????????rentingFormula");
        Assert.hasKeyAndValue(reqJson, "servicePrice", "????????????????????????servicePrice");
        Assert.hasKeyAndValue(reqJson, "serviceOwnerRate", "????????????????????????serviceOwnerRate");
        Assert.hasKeyAndValue(reqJson, "serviceTenantRate", "????????????????????????serviceTenantRate");
        Assert.hasKeyAndValue(reqJson, "adminSeparateRate", "????????????????????????adminSeparateRate");
        Assert.hasKeyAndValue(reqJson, "proxySeparateRate", "????????????????????????proxySeparateRate");
        Assert.hasKeyAndValue(reqJson, "propertySeparateRate", "????????????????????????propertySeparateRate");


        RentingConfigPo rentingConfigPo = BeanConvertUtil.covertBean(reqJson, RentingConfigPo.class);
        return saveRentingConfigBMOImpl.save(rentingConfigPo);
    }

    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /renting/updateRentingConfig
     * @path /app/renting/updateRentingConfig
     */
    @RequestMapping(value = "/updateRentingConfig", method = RequestMethod.POST)
    public ResponseEntity<String> updateRentingConfig(@RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "rentingType", "????????????????????????rentingType");
        Assert.hasKeyAndValue(reqJson, "rentingFormula", "????????????????????????rentingFormula");
        Assert.hasKeyAndValue(reqJson, "servicePrice", "????????????????????????servicePrice");
        Assert.hasKeyAndValue(reqJson, "serviceOwnerRate", "????????????????????????serviceOwnerRate");
        Assert.hasKeyAndValue(reqJson, "serviceTenantRate", "????????????????????????serviceTenantRate");
        Assert.hasKeyAndValue(reqJson, "adminSeparateRate", "????????????????????????adminSeparateRate");
        Assert.hasKeyAndValue(reqJson, "proxySeparateRate", "????????????????????????proxySeparateRate");
        Assert.hasKeyAndValue(reqJson, "propertySeparateRate", "????????????????????????propertySeparateRate");
        Assert.hasKeyAndValue(reqJson, "rentingConfigId", "rentingConfigId????????????");


        RentingConfigPo rentingConfigPo = BeanConvertUtil.covertBean(reqJson, RentingConfigPo.class);
        return updateRentingConfigBMOImpl.update(rentingConfigPo);
    }

    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /renting/deleteRentingConfig
     * @path /app/renting/deleteRentingConfig
     */
    @RequestMapping(value = "/deleteRentingConfig", method = RequestMethod.POST)
    public ResponseEntity<String> deleteRentingConfig(@RequestBody JSONObject reqJson) {
        ;

        Assert.hasKeyAndValue(reqJson, "rentingConfigId", "rentingConfigId????????????");


        RentingConfigPo rentingConfigPo = BeanConvertUtil.covertBean(reqJson, RentingConfigPo.class);
        return deleteRentingConfigBMOImpl.delete(rentingConfigPo);
    }

    /**
     * ????????????????????????
     *
     * @return
     * @serviceCode /renting/queryRentingConfig
     * @path /app/renting/queryRentingConfig
     */
    @RequestMapping(value = "/queryRentingConfig", method = RequestMethod.GET)
    public ResponseEntity<String> queryRentingConfig(
            @RequestParam(value = "page") int page,
            @RequestParam(value = "row") int row,
            @RequestParam(value = "rentingConfigId", required = false) String rentingConfigId) {
        RentingConfigDto rentingConfigDto = new RentingConfigDto();
        rentingConfigDto.setPage(page);
        rentingConfigDto.setRow(row);
        rentingConfigDto.setRentingConfigId(rentingConfigId);
        return getRentingConfigBMOImpl.get(rentingConfigDto);
    }


    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /renting/saveRentingPool
     * @path /app/renting/saveRentingPool
     */
    @RequestMapping(value = "/saveRentingPool", method = RequestMethod.POST)
    public ResponseEntity<String> saveRentingPool(@RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "rentingTitle", "????????????????????????rentingTitle");
        Assert.hasKeyAndValue(reqJson, "roomId", "????????????????????????roomId");
        Assert.hasKeyAndValue(reqJson, "communityId", "????????????????????????communityId");
        Assert.hasKeyAndValue(reqJson, "communityName", "????????????????????????communityName");
        Assert.hasKeyAndValue(reqJson, "price", "????????????????????????price");
        Assert.hasKeyAndValue(reqJson, "paymentType", "????????????????????????paymentType");
        Assert.hasKeyAndValue(reqJson, "checkIn", "????????????????????????checkIn");
        Assert.hasKeyAndValue(reqJson, "rentingConfigId", "????????????????????????rentingConfigId");
        Assert.hasKeyAndValue(reqJson, "ownerName", "????????????????????????ownerName");
        Assert.hasKeyAndValue(reqJson, "ownerTel", "????????????????????????ownerTel");
        JSONArray photos = null;
        if (reqJson.containsKey("photos")) {
            photos = reqJson.getJSONArray("photos");
        }else{
            photos = new JSONArray();
        }


        RentingPoolPo rentingPoolPo = BeanConvertUtil.covertBean(reqJson, RentingPoolPo.class);
        return saveRentingPoolBMOImpl.save(rentingPoolPo,photos);
    }

    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /renting/updateRentingPool
     * @path /app/renting/updateRentingPool
     */
    @RequestMapping(value = "/updateRentingPool", method = RequestMethod.POST)
    public ResponseEntity<String> updateRentingPool(@RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "rentingTitle", "????????????????????????rentingTitle");
        Assert.hasKeyAndValue(reqJson, "roomId", "????????????????????????roomId");
        Assert.hasKeyAndValue(reqJson, "communityId", "????????????????????????communityId");
        Assert.hasKeyAndValue(reqJson, "price", "????????????????????????price");
        Assert.hasKeyAndValue(reqJson, "checkIn", "????????????????????????checkInDate");
        Assert.hasKeyAndValue(reqJson, "rentingConfigId", "????????????????????????rentingConfigId");
        Assert.hasKeyAndValue(reqJson, "ownerName", "????????????????????????ownerName");
        Assert.hasKeyAndValue(reqJson, "ownerTel", "????????????????????????ownerTel");
        Assert.hasKeyAndValue(reqJson, "rentingId", "rentingId????????????");


        RentingPoolPo rentingPoolPo = BeanConvertUtil.covertBean(reqJson, RentingPoolPo.class);
        return updateRentingPoolBMOImpl.update(rentingPoolPo);
    }

    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /renting/deleteRentingPool
     * @path /app/renting/deleteRentingPool
     */
    @RequestMapping(value = "/deleteRentingPool", method = RequestMethod.POST)
    public ResponseEntity<String> deleteRentingPool(@RequestBody JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "communityId", "??????ID????????????");

        Assert.hasKeyAndValue(reqJson, "rentingId", "rentingId????????????");


        RentingPoolPo rentingPoolPo = BeanConvertUtil.covertBean(reqJson, RentingPoolPo.class);
        return deleteRentingPoolBMOImpl.delete(rentingPoolPo);
    }

    /**
     * ????????????????????????
     *
     * @param communityId ??????ID
     * @return
     * @serviceCode /renting/queryRentingPool
     * @path /app/renting/queryRentingPool
     */
    @RequestMapping(value = "/queryRentingPool", method = RequestMethod.GET)
    public ResponseEntity<String> queryRentingPool(@RequestParam(value = "communityId", required = false) String communityId,
                                                   @RequestParam(value = "communityName", required = false) String communityName,
                                                   @RequestParam(value = "page") int page,
                                                   @RequestParam(value = "row") int row,
                                                   @RequestParam(value = "state", required = false) String state,
                                                   @RequestParam(value = "rentingType", required = false) String rentingType,
                                                   @RequestParam(value = "rentingId", required = false) String rentingId
    ) {
        RentingPoolDto rentingPoolDto = new RentingPoolDto();
        rentingPoolDto.setPage(page);
        rentingPoolDto.setRow(row);
        rentingPoolDto.setCommunityId(communityId);
        rentingPoolDto.setCommunityName(communityName);
        rentingPoolDto.setRentingId(rentingId);
        rentingPoolDto.setRentingType(rentingType);
        if (!StringUtils.isEmpty(state) && state.contains(",")) {
            rentingPoolDto.setStates(state.split(","));
        } else {
            rentingPoolDto.setState(state);
        }
        return getRentingPoolBMOImpl.get(rentingPoolDto);
    }


    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /renting/saveRentingPoolAttr
     * @path /app/renting/saveRentingPoolAttr
     */
    @RequestMapping(value = "/saveRentingPoolAttr", method = RequestMethod.POST)
    public ResponseEntity<String> saveRentingPoolAttr(@RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "rentingId", "????????????????????????rentingId");
        Assert.hasKeyAndValue(reqJson, "specCd", "????????????????????????specCd");


        RentingPoolAttrPo rentingPoolAttrPo = BeanConvertUtil.covertBean(reqJson, RentingPoolAttrPo.class);
        return saveRentingPoolAttrBMOImpl.save(rentingPoolAttrPo);
    }

    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /renting/updateRentingPoolAttr
     * @path /app/renting/updateRentingPoolAttr
     */
    @RequestMapping(value = "/updateRentingPoolAttr", method = RequestMethod.POST)
    public ResponseEntity<String> updateRentingPoolAttr(@RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "rentingId", "????????????????????????rentingId");
        Assert.hasKeyAndValue(reqJson, "specCd", "????????????????????????specCd");
        Assert.hasKeyAndValue(reqJson, "attrId", "attrId????????????");


        RentingPoolAttrPo rentingPoolAttrPo = BeanConvertUtil.covertBean(reqJson, RentingPoolAttrPo.class);
        return updateRentingPoolAttrBMOImpl.update(rentingPoolAttrPo);
    }

    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /renting/deleteRentingPoolAttr
     * @path /app/renting/deleteRentingPoolAttr
     */
    @RequestMapping(value = "/deleteRentingPoolAttr", method = RequestMethod.POST)
    public ResponseEntity<String> deleteRentingPoolAttr(@RequestBody JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "communityId", "??????ID????????????");

        Assert.hasKeyAndValue(reqJson, "attrId", "attrId????????????");


        RentingPoolAttrPo rentingPoolAttrPo = BeanConvertUtil.covertBean(reqJson, RentingPoolAttrPo.class);
        return deleteRentingPoolAttrBMOImpl.delete(rentingPoolAttrPo);
    }

    /**
     * ????????????????????????
     *
     * @param communityId ??????ID
     * @return
     * @serviceCode /renting/queryRentingPoolAttr
     * @path /app/renting/queryRentingPoolAttr
     */
    @RequestMapping(value = "/queryRentingPoolAttr", method = RequestMethod.GET)
    public ResponseEntity<String> queryRentingPoolAttr(@RequestParam(value = "communityId") String communityId,
                                                       @RequestParam(value = "page") int page,
                                                       @RequestParam(value = "row") int row) {
        RentingPoolAttrDto rentingPoolAttrDto = new RentingPoolAttrDto();
        rentingPoolAttrDto.setPage(page);
        rentingPoolAttrDto.setRow(row);
        rentingPoolAttrDto.setCommunityId(communityId);
        return getRentingPoolAttrBMOImpl.get(rentingPoolAttrDto);
    }

    /**
     * ????????? ????????????????????????
     *
     * @param reqJson ????????????
     * @return
     * @serviceCode /renting/auditRenting
     * @path /app/renting/auditRenting
     */
    @RequestMapping(value = "/auditRenting", method = RequestMethod.POST)
    public ResponseEntity<String> auditRenting(@RequestBody JSONObject reqJson,
                                               @RequestHeader(value = "store-id") String storeId,
                                               @RequestHeader(value = "user-id") String userId) {
        Assert.hasKeyAndValue(reqJson, "rentingId", "????????????????????????????????????");
        Assert.hasKeyAndValue(reqJson, "state", "??????????????????????????????");
        Assert.hasKeyAndValue(reqJson, "context", "????????????????????????????????????");
        Assert.hasKeyAndValue(reqJson, "userRole", "??????????????????????????????");
        Assert.hasValue(storeId, "??????????????????????????????ID");
        Assert.hasValue(userId, "??????????????????????????????ID");


        RentingPoolFlowPo rentingPoolFlowPo = new RentingPoolFlowPo();
        rentingPoolFlowPo.setContext(reqJson.getString("context"));
        rentingPoolFlowPo.setDealTime(DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
        rentingPoolFlowPo.setFlowId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_flowId));
        rentingPoolFlowPo.setRentingId(reqJson.getString("rentingId"));
        rentingPoolFlowPo.setState(reqJson.getString("state"));
        rentingPoolFlowPo.setUserRole(reqJson.getString("userRole"));

        return auditRentingBMOImpl.audit(rentingPoolFlowPo, userId);
    }
}
