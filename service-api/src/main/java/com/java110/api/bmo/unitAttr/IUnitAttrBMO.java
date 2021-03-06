package com.java110.api.bmo.unitAttr;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.IApiBaseBMO;
import com.java110.core.context.DataFlowContext;

public interface IUnitAttrBMO extends IApiBaseBMO {


    /**
     * 添加单元属性
     * @param paramInJson
     * @param dataFlowContext
     * @return
     */
     void addUnitAttr(JSONObject paramInJson, DataFlowContext dataFlowContext);

    /**
     * 添加单元属性信息
     *
     * @param paramInJson     接口调用放传入入参
     * @param dataFlowContext 数据上下文
     * @return 订单服务能够接受的报文
     */
     void updateUnitAttr(JSONObject paramInJson, DataFlowContext dataFlowContext);

    /**
     * 删除单元属性
     *
     * @param paramInJson     接口调用放传入入参
     * @param dataFlowContext 数据上下文
     * @return 订单服务能够接受的报文
     */
     void deleteUnitAttr(JSONObject paramInJson, DataFlowContext dataFlowContext);



}
