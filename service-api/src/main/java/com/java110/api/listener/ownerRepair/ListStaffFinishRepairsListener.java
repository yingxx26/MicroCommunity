package com.java110.api.listener.ownerRepair;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.listener.AbstractServiceApiListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.dto.basePrivilege.BasePrivilegeDto;
import com.java110.dto.user.UserDto;
import com.java110.intf.community.IMenuInnerServiceSMO;
import com.java110.intf.community.IRepairInnerServiceSMO;
import com.java110.intf.community.IRepairUserInnerServiceSMO;
import com.java110.dto.repair.RepairDto;
import com.java110.dto.repair.RepairUserDto;
import com.java110.intf.user.IUserInnerServiceSMO;
import com.java110.utils.cache.MappingCache;
import com.java110.utils.constant.ServiceCodeOwnerRepairConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 查询小区侦听类
 */
@Java110Listener("listStaffFinishRepairsListener")
public class ListStaffFinishRepairsListener extends AbstractServiceApiListener {

    @Autowired
    private IRepairInnerServiceSMO repairInnerServiceSMOImpl;

    @Autowired
    private IRepairUserInnerServiceSMO repairUserInnerServiceSMOImpl;

    @Autowired
    private IMenuInnerServiceSMO menuInnerServiceSMOImpl;

    @Override
    public String getServiceCode() {
        return ServiceCodeOwnerRepairConstant.LIST_STAFF_FINISH_REPAIRS;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    public IRepairInnerServiceSMO getRepairInnerServiceSMOImpl() {
        return repairInnerServiceSMOImpl;
    }

    public void setRepairInnerServiceSMOImpl(IRepairInnerServiceSMO repairInnerServiceSMOImpl) {
        this.repairInnerServiceSMOImpl = repairInnerServiceSMOImpl;
    }

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        super.validatePageInfo(reqJson);
        Assert.hasKeyAndValue(reqJson, "communityId", "请求中未包含小区ID");
        Assert.hasKeyAndValue(reqJson, "userId", "请求中未包含员工信息");
    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {
        RepairDto ownerRepairDto = BeanConvertUtil.covertBean(reqJson, RepairDto.class);
        String userId = reqJson.getString("userId");
        String viewListStaffRepairs = MappingCache.getValue("viewListStaffRepairs");
        List<Map> privileges = null;
        //这里加开关 其实让管理员看到所有单子这么做，不太优雅，建议 单独开发页面功能
        // 不要影响已办功能，add by 吴学文 2021-09-09
        if("ON".equals(viewListStaffRepairs)) {//是否让管理员看到所有工单
            BasePrivilegeDto basePrivilegeDto = new BasePrivilegeDto();
            basePrivilegeDto.setResource("/listStaffFinishRepairs");
            basePrivilegeDto.setUserId(userId);
            privileges = menuInnerServiceSMOImpl.checkUserHasResource(basePrivilegeDto);
        }
        if (privileges == null || privileges.size()==0) {
            ownerRepairDto.setStaffId(reqJson.getString("userId"));
        }
        if (reqJson.containsKey("repairStates")) {
            String[] states = reqJson.getString("repairStates").split(",");
            ownerRepairDto.setStates(Arrays.asList(states));
        }else{
            //Pc WEB维修已办
//            String[] states={RepairDto.STATE_BACK, RepairDto.STATE_TRANSFER,RepairDto.STATE_PAY, RepairDto.STATE_PAY_ERROR, RepairDto.STATE_APPRAISE, RepairDto.STATE_RETURN_VISIT, RepairDto.STATE_COMPLATE};
//            ownerRepairDto.setStates(Arrays.asList(states));
        }
        int count = repairInnerServiceSMOImpl.queryStaffFinishRepairsCount(ownerRepairDto);
        List<RepairDto> ownerRepairs = null;
        if (count > 0) {
            ownerRepairs = repairInnerServiceSMOImpl.queryStaffFinishRepairs(ownerRepairDto);
            //refreshStaffName(ownerRepairs);
        } else {
            ownerRepairs = new ArrayList<>();
        }
        ResponseEntity<String> responseEntity = ResultVo.createResponseEntity((int) Math.ceil((double) count / (double) reqJson.getInteger("row")), count, ownerRepairs);
        context.setResponseEntity(responseEntity);
    }

    private void refreshStaffName(List<RepairDto> ownerRepairs) {
        List<String> repairIds = new ArrayList<>();
        for (RepairDto apiOwnerRepairDataVo : ownerRepairs) {
            repairIds.add(apiOwnerRepairDataVo.getRepairId());
        }
        if (repairIds.size() < 1) {
            return;
        }
        RepairUserDto repairUserDto = new RepairUserDto();
        repairUserDto.setRepairIds(repairIds.toArray(new String[repairIds.size()]));
        List<RepairUserDto> repairUserDtos = repairUserInnerServiceSMOImpl.queryRepairUsers(repairUserDto);
        for (RepairUserDto tmpRepairUserDto : repairUserDtos) {
            for (RepairDto apiOwnerRepairDataVo : ownerRepairs) {
                if (tmpRepairUserDto.getRepairId().equals(apiOwnerRepairDataVo.getRepairId())) {
                    apiOwnerRepairDataVo.setStaffId(tmpRepairUserDto.getUserId());
                    //apiOwnerRepairDataVo.setStatmpRepairUserDto.getUserName());
                }
            }
        }
    }
}
