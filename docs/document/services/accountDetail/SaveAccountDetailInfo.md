package com.java110.acct.listener.accountDetail;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.StatusConstant;
import com.java110.utils.util.Assert;
import com.java110.acct.dao.IAccountDetailServiceDao;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.entity.center.Business;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 保存 账户交易信息 侦听
 * Created by wuxw on 2018/5/18.
 */
@Java110Listener("saveAccountDetailInfoListener")
@Transactional
public class SaveAccountDetailInfoListener extends AbstractAccountDetailBusinessServiceDataFlowListener{

    private static Logger logger = LoggerFactory.getLogger(SaveAccountDetailInfoListener.class);

    @Autowired
    private IAccountDetailServiceDao accountDetailServiceDaoImpl;

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public String getBusinessTypeCd() {
        return BusinessTypeConstant.BUSINESS_TYPE_SAVE_ACCT_DETAIL;
    }

    /**
     * 保存账户交易信息 business 表中
     * @param dataFlowContext 数据对象
     * @param business 当前业务对象
     */
    @Override
    protected void doSaveBusiness(DataFlowContext dataFlowContext, Business business) {
        JSONObject data = business.getDatas();
        Assert.notEmpty(data,"没有datas 节点，或没有子节点需要处理");

        //处理 businessAccountDetail 节点
        if(data.containsKey(AccountDetailPo.class.getSimpleName())){
            Object bObj = data.get(AccountDetailPo.class.getSimpleName());
            JSONArray businessAccountDetails = null;
            if(bObj instanceof JSONObject){
                businessAccountDetails = new JSONArray();
                businessAccountDetails.add(bObj);
            }else {
                businessAccountDetails = (JSONArray)bObj;
            }
            //JSONObject businessAccountDetail = data.getJSONObject(AccountDetailPo.class.getSimpleName());
            for (int bAccountDetailIndex = 0; bAccountDetailIndex < businessAccountDetails.size();bAccountDetailIndex++) {
                JSONObject businessAccountDetail = businessAccountDetails.getJSONObject(bAccountDetailIndex);
                doBusinessAccountDetail(business, businessAccountDetail);
                if(bObj instanceof JSONObject) {
                    dataFlowContext.addParamOut("detailId", businessAccountDetail.getString("detailId"));
                }
            }
        }
    }

    /**
     * business 数据转移到 instance
     * @param dataFlowContext 数据对象
     * @param business 当前业务对象
     */
    @Override
    protected void doBusinessToInstance(DataFlowContext dataFlowContext, Business business) {
        JSONObject data = business.getDatas();

        Map info = new HashMap();
        info.put("bId",business.getbId());
        info.put("operate",StatusConstant.OPERATE_ADD);

        //账户交易信息
        List<Map> businessAccountDetailInfo = accountDetailServiceDaoImpl.getBusinessAccountDetailInfo(info);
        if( businessAccountDetailInfo != null && businessAccountDetailInfo.size() >0) {
            reFreshShareColumn(info, businessAccountDetailInfo.get(0));
            accountDetailServiceDaoImpl.saveAccountDetailInfoInstance(info);
            if(businessAccountDetailInfo.size() == 1) {
                dataFlowContext.addParamOut("detailId", businessAccountDetailInfo.get(0).get("detail_id"));
            }
        }
    }


    /**
     * 刷 分片字段
     *
     * @param info         查询对象
     * @param businessInfo 小区ID
     */
    private void reFreshShareColumn(Map info, Map businessInfo) {

        if (info.containsKey("objId")) {
            return;
        }

        if (!businessInfo.containsKey("obj_id")) {
            return;
        }

        info.put("objId", businessInfo.get("obj_id"));
    }
    /**
     * 撤单
     * @param dataFlowContext 数据对象
     * @param business 当前业务对象
     */
    @Override
    protected void doRecover(DataFlowContext dataFlowContext, Business business) {
        String bId = business.getbId();
        //Assert.hasLength(bId,"请求报文中没有包含 bId");
        Map info = new HashMap();
        info.put("bId",bId);
        info.put("statusCd",StatusConstant.STATUS_CD_VALID);
        Map paramIn = new HashMap();
        paramIn.put("bId",bId);
        paramIn.put("statusCd",StatusConstant.STATUS_CD_INVALID);
        //账户交易信息
        List<Map> accountDetailInfo = accountDetailServiceDaoImpl.getAccountDetailInfo(info);
        if(accountDetailInfo != null && accountDetailInfo.size() > 0){
            reFreshShareColumn(paramIn, accountDetailInfo.get(0));
            accountDetailServiceDaoImpl.updateAccountDetailInfoInstance(paramIn);
        }
    }



    /**
     * 处理 businessAccountDetail 节点
     * @param business 总的数据节点
     * @param businessAccountDetail 账户交易节点
     */
    private void doBusinessAccountDetail(Business business,JSONObject businessAccountDetail){

        Assert.jsonObjectHaveKey(businessAccountDetail,"detailId","businessAccountDetail 节点下没有包含 detailId 节点");

        if(businessAccountDetail.getString("detailId").startsWith("-")){
            //刷新缓存
            //flushAccountDetailId(business.getDatas());

            businessAccountDetail.put("detailId",GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_detailId));

        }

        businessAccountDetail.put("bId",business.getbId());
        businessAccountDetail.put("operate", StatusConstant.OPERATE_ADD);
        //保存账户交易信息
        accountDetailServiceDaoImpl.saveBusinessAccountDetailInfo(businessAccountDetail);

    }
    @Override
    public IAccountDetailServiceDao getAccountDetailServiceDaoImpl() {
        return accountDetailServiceDaoImpl;
    }

    public void setAccountDetailServiceDaoImpl(IAccountDetailServiceDao accountDetailServiceDaoImpl) {
        this.accountDetailServiceDaoImpl = accountDetailServiceDaoImpl;
    }
}
