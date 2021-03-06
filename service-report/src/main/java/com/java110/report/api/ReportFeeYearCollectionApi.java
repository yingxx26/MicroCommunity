package com.java110.report.api;

import com.alibaba.fastjson.JSONObject;
import com.java110.dto.reportFeeYearCollection.ReportFeeYearCollectionDto;
import com.java110.dto.reportFeeYearCollectionDetail.ReportFeeYearCollectionDetailDto;
import com.java110.po.reportFeeYearCollection.ReportFeeYearCollectionPo;
import com.java110.po.reportFeeYearCollectionDetail.ReportFeeYearCollectionDetailPo;
import com.java110.report.bmo.reportFeeYearCollection.IDeleteReportFeeYearCollectionBMO;
import com.java110.report.bmo.reportFeeYearCollection.IGetReportFeeYearCollectionBMO;
import com.java110.report.bmo.reportFeeYearCollection.ISaveReportFeeYearCollectionBMO;
import com.java110.report.bmo.reportFeeYearCollection.IUpdateReportFeeYearCollectionBMO;
import com.java110.report.bmo.reportFeeYearCollectionDetail.IDeleteReportFeeYearCollectionDetailBMO;
import com.java110.report.bmo.reportFeeYearCollectionDetail.IGetReportFeeYearCollectionDetailBMO;
import com.java110.report.bmo.reportFeeYearCollectionDetail.ISaveReportFeeYearCollectionDetailBMO;
import com.java110.report.bmo.reportFeeYearCollectionDetail.IUpdateReportFeeYearCollectionDetailBMO;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/reportFeeYearCollection")
public class ReportFeeYearCollectionApi {

    @Autowired
    private ISaveReportFeeYearCollectionBMO saveReportFeeYearCollectionBMOImpl;
    @Autowired
    private IUpdateReportFeeYearCollectionBMO updateReportFeeYearCollectionBMOImpl;
    @Autowired
    private IDeleteReportFeeYearCollectionBMO deleteReportFeeYearCollectionBMOImpl;

    @Autowired
    private IGetReportFeeYearCollectionBMO getReportFeeYearCollectionBMOImpl;

    @Autowired
    private ISaveReportFeeYearCollectionDetailBMO saveReportFeeYearCollectionDetailBMOImpl;
    @Autowired
    private IUpdateReportFeeYearCollectionDetailBMO updateReportFeeYearCollectionDetailBMOImpl;
    @Autowired
    private IDeleteReportFeeYearCollectionDetailBMO deleteReportFeeYearCollectionDetailBMOImpl;

    @Autowired
    private IGetReportFeeYearCollectionDetailBMO getReportFeeYearCollectionDetailBMOImpl;

    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /reportFeeYearCollection/saveReportFeeYearCollection
     * @path /app/reportFeeYearCollection/saveReportFeeYearCollection
     */
    @RequestMapping(value = "/saveReportFeeYearCollection", method = RequestMethod.POST)
    public ResponseEntity<String> saveReportFeeYearCollection(@RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "communityId", "????????????????????????communityId");
        Assert.hasKeyAndValue(reqJson, "configId", "????????????????????????configId");
        Assert.hasKeyAndValue(reqJson, "objType", "????????????????????????objType");
        Assert.hasKeyAndValue(reqJson, "objId", "????????????????????????objId");


        ReportFeeYearCollectionPo reportFeeYearCollectionPo = BeanConvertUtil.covertBean(reqJson, ReportFeeYearCollectionPo.class);
        return saveReportFeeYearCollectionBMOImpl.save(reportFeeYearCollectionPo);
    }

    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /reportFeeYearCollection/updateReportFeeYearCollection
     * @path /app/reportFeeYearCollection/updateReportFeeYearCollection
     */
    @RequestMapping(value = "/updateReportFeeYearCollection", method = RequestMethod.POST)
    public ResponseEntity<String> updateReportFeeYearCollection(@RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "communityId", "????????????????????????communityId");
        Assert.hasKeyAndValue(reqJson, "configId", "????????????????????????configId");
        Assert.hasKeyAndValue(reqJson, "objType", "????????????????????????objType");
        Assert.hasKeyAndValue(reqJson, "objId", "????????????????????????objId");
        Assert.hasKeyAndValue(reqJson, "collectionId", "collectionId????????????");


        ReportFeeYearCollectionPo reportFeeYearCollectionPo = BeanConvertUtil.covertBean(reqJson, ReportFeeYearCollectionPo.class);
        return updateReportFeeYearCollectionBMOImpl.update(reportFeeYearCollectionPo);
    }

    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /reportFeeYearCollection/deleteReportFeeYearCollection
     * @path /app/reportFeeYearCollection/deleteReportFeeYearCollection
     */
    @RequestMapping(value = "/deleteReportFeeYearCollection", method = RequestMethod.POST)
    public ResponseEntity<String> deleteReportFeeYearCollection(@RequestBody JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "communityId", "??????ID????????????");

        Assert.hasKeyAndValue(reqJson, "collectionId", "collectionId????????????");


        ReportFeeYearCollectionPo reportFeeYearCollectionPo = BeanConvertUtil.covertBean(reqJson, ReportFeeYearCollectionPo.class);
        return deleteReportFeeYearCollectionBMOImpl.delete(reportFeeYearCollectionPo);
    }

    /**
     * ????????????????????????
     *
     * @param communityId ??????ID
     * @return
     * @serviceCode /reportFeeYearCollection/queryReportFeeYear
     * @path /app/reportFeeYearCollection/queryReportFeeYear
     */
    @RequestMapping(value = "/queryReportFeeYear", method = RequestMethod.GET)
    public ResponseEntity<String> queryReportFeeYear(@RequestParam(value = "communityId") String communityId,
                                                     @RequestParam(value = "objType", required = false) String objType,
                                                     @RequestParam(value = "feeTypeCd", required = false) String feeTypeCd,
                                                     @RequestParam(value = "objName", required = false) String objName,
                                                     @RequestParam(value = "configId", required = false) String configId,
                                                     @RequestParam(value = "page") int page,
                                                     @RequestParam(value = "row") int row) {
        ReportFeeYearCollectionDto reportFeeYearCollectionDto = new ReportFeeYearCollectionDto();
        reportFeeYearCollectionDto.setPage(page);
        reportFeeYearCollectionDto.setRow(row);
        reportFeeYearCollectionDto.setCommunityId(communityId);
        reportFeeYearCollectionDto.setObjType(objType);
        reportFeeYearCollectionDto.setObjName(objName);
        reportFeeYearCollectionDto.setConfigId(configId);
        reportFeeYearCollectionDto.setFeeTypeCd(feeTypeCd);
        return getReportFeeYearCollectionBMOImpl.get(reportFeeYearCollectionDto);
    }


    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /reportFeeYearCollectionDetail/saveReportFeeYearCollectionDetail
     * @path /app/reportFeeYearCollectionDetail/saveReportFeeYearCollectionDetail
     */
    @RequestMapping(value = "/saveReportFeeYearCollectionDetail", method = RequestMethod.POST)
    public ResponseEntity<String> saveReportFeeYearCollectionDetail(@RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "communityId", "????????????????????????communityId");
        Assert.hasKeyAndValue(reqJson, "collectionId", "????????????????????????collectionId");
        Assert.hasKeyAndValue(reqJson, "receivedAmount", "????????????????????????receivedAmount");
        Assert.hasKeyAndValue(reqJson, "receivableAmount", "????????????????????????receivableAmount");


        ReportFeeYearCollectionDetailPo reportFeeYearCollectionDetailPo = BeanConvertUtil.covertBean(reqJson, ReportFeeYearCollectionDetailPo.class);
        return saveReportFeeYearCollectionDetailBMOImpl.save(reportFeeYearCollectionDetailPo);
    }

    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /reportFeeYearCollection/updateReportFeeYearCollectionDetail
     * @path /app/reportFeeYearCollection/updateReportFeeYearCollectionDetail
     */
    @RequestMapping(value = "/updateReportFeeYearCollectionDetail", method = RequestMethod.POST)
    public ResponseEntity<String> updateReportFeeYearCollectionDetail(@RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "communityId", "????????????????????????communityId");
        Assert.hasKeyAndValue(reqJson, "collectionId", "????????????????????????collectionId");
        Assert.hasKeyAndValue(reqJson, "receivedAmount", "????????????????????????receivedAmount");
        Assert.hasKeyAndValue(reqJson, "receivableAmount", "????????????????????????receivableAmount");
        Assert.hasKeyAndValue(reqJson, "detailId", "detailId????????????");


        ReportFeeYearCollectionDetailPo reportFeeYearCollectionDetailPo = BeanConvertUtil.covertBean(reqJson, ReportFeeYearCollectionDetailPo.class);
        return updateReportFeeYearCollectionDetailBMOImpl.update(reportFeeYearCollectionDetailPo);
    }

    /**
     * ????????????????????????
     *
     * @param reqJson
     * @return
     * @serviceCode /reportFeeYearCollection/deleteReportFeeYearCollectionDetail
     * @path /app/reportFeeYearCollection/deleteReportFeeYearCollectionDetail
     */
    @RequestMapping(value = "/deleteReportFeeYearCollectionDetail", method = RequestMethod.POST)
    public ResponseEntity<String> deleteReportFeeYearCollectionDetail(@RequestBody JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "communityId", "??????ID????????????");

        Assert.hasKeyAndValue(reqJson, "detailId", "detailId????????????");


        ReportFeeYearCollectionDetailPo reportFeeYearCollectionDetailPo = BeanConvertUtil.covertBean(reqJson, ReportFeeYearCollectionDetailPo.class);
        return deleteReportFeeYearCollectionDetailBMOImpl.delete(reportFeeYearCollectionDetailPo);
    }

    /**
     * ????????????????????????
     *
     * @param communityId ??????ID
     * @return
     * @serviceCode /reportFeeYearCollection/queryReportFeeYearCollectionDetail
     * @path /app/reportFeeYearCollection/queryReportFeeYearCollectionDetail
     */
    @RequestMapping(value = "/queryReportFeeYearCollectionDetail", method = RequestMethod.GET)
    public ResponseEntity<String> queryReportFeeYearCollectionDetail(@RequestParam(value = "communityId") String communityId,
                                                                     @RequestParam(value = "page") int page,
                                                                     @RequestParam(value = "row") int row) {
        ReportFeeYearCollectionDetailDto reportFeeYearCollectionDetailDto = new ReportFeeYearCollectionDetailDto();
        reportFeeYearCollectionDetailDto.setPage(page);
        reportFeeYearCollectionDetailDto.setRow(row);
        reportFeeYearCollectionDetailDto.setCommunityId(communityId);
        return getReportFeeYearCollectionDetailBMOImpl.get(reportFeeYearCollectionDetailDto);
    }
}
