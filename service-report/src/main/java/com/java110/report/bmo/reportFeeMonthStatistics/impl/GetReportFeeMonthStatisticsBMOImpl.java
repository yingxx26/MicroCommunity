package com.java110.report.bmo.reportFeeMonthStatistics.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.smo.IComputeFeeSMO;
import com.java110.dto.PageDto;
import com.java110.dto.RoomDto;
import com.java110.dto.fee.FeeConfigDto;
import com.java110.dto.fee.FeeDto;
import com.java110.dto.owner.OwnerDto;
import com.java110.dto.owner.OwnerRoomRelDto;
import com.java110.dto.repair.RepairDto;
import com.java110.dto.repair.RepairUserDto;
import com.java110.dto.report.ReportDeposit;
import com.java110.dto.reportFeeMonthStatistics.ReportFeeMonthStatisticsDto;
import com.java110.dto.reportFeeMonthStatistics.ReportFeeMonthStatisticsTotalDto;
import com.java110.intf.community.IRepairInnerServiceSMO;
import com.java110.intf.fee.IFeeConfigInnerServiceSMO;
import com.java110.intf.report.IReportFeeMonthStatisticsInnerServiceSMO;
import com.java110.intf.user.IOwnerInnerServiceSMO;
import com.java110.intf.user.IOwnerRoomRelInnerServiceSMO;
import com.java110.report.bmo.reportFeeMonthStatistics.IGetReportFeeMonthStatisticsBMO;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.DateUtil;
import com.java110.utils.util.StringUtil;
import com.java110.vo.ResultVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

@Service("getReportFeeMonthStatisticsBMOImpl")
public class GetReportFeeMonthStatisticsBMOImpl implements IGetReportFeeMonthStatisticsBMO {

    private static final Logger logger = LoggerFactory.getLogger(GetReportFeeMonthStatisticsBMOImpl.class);

    @Autowired
    private IReportFeeMonthStatisticsInnerServiceSMO reportFeeMonthStatisticsInnerServiceSMOImpl;

    @Autowired
    private IFeeConfigInnerServiceSMO feeConfigInnerServiceSMOImpl;

    @Autowired
    private IRepairInnerServiceSMO repairInnerServiceSMOImpl;

    @Autowired
    private IOwnerRoomRelInnerServiceSMO ownerRoomRelInnerServiceSMOImpl;

    @Autowired
    private IOwnerInnerServiceSMO ownerInnerServiceSMOImpl;

    @Autowired
    private IComputeFeeSMO computeFeeSMOImpl;

    /**
     * @param reportFeeMonthStatisticsDto
     * @return ?????????????????????????????????
     */
    public ResponseEntity<String> get(ReportFeeMonthStatisticsDto reportFeeMonthStatisticsDto) {


        int count = reportFeeMonthStatisticsInnerServiceSMOImpl.queryReportFeeMonthStatisticssCount(reportFeeMonthStatisticsDto);

        List<ReportFeeMonthStatisticsDto> reportFeeMonthStatisticsDtos = null;
        if (count > 0) {
            reportFeeMonthStatisticsDtos = reportFeeMonthStatisticsInnerServiceSMOImpl.queryReportFeeMonthStatisticss(reportFeeMonthStatisticsDto);
        } else {
            reportFeeMonthStatisticsDtos = new ArrayList<>();
        }

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) count / (double) reportFeeMonthStatisticsDto.getRow()), count, reportFeeMonthStatisticsDtos);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        return responseEntity;
    }

    @Override
    public ResponseEntity<String> queryReportFeeSummary(ReportFeeMonthStatisticsDto reportFeeMonthStatisticsDto) {
        int count = reportFeeMonthStatisticsInnerServiceSMOImpl.queryReportFeeSummaryCount(reportFeeMonthStatisticsDto);

        List<ReportFeeMonthStatisticsDto> reportFeeMonthStatisticsDtos = new ArrayList<>();
        if (count > 0) {
            List<ReportFeeMonthStatisticsDto> reportFeeMonthStatisticsList = reportFeeMonthStatisticsInnerServiceSMOImpl.queryReportFeeSummary(reportFeeMonthStatisticsDto);
            for (ReportFeeMonthStatisticsDto reportFeeMonthStatistics : reportFeeMonthStatisticsList) {
                //??????????????????
                double receivableAmount = Double.parseDouble(reportFeeMonthStatistics.getReceivableAmount());
                //??????????????????
                double receivedAmount = Double.parseDouble(reportFeeMonthStatistics.getReceivedAmount());
                double chargeRate = (receivedAmount / receivableAmount) * 100.0;
                reportFeeMonthStatistics.setChargeRate(String.format("%.2f", chargeRate) + "%");
                reportFeeMonthStatisticsDtos.add(reportFeeMonthStatistics);
            }
            ReportFeeMonthStatisticsDto tmpReportFeeMonthStatisticsDto = reportFeeMonthStatisticsInnerServiceSMOImpl.queryReportFeeSummaryMajor(reportFeeMonthStatisticsDto);
            if (reportFeeMonthStatisticsList != null && reportFeeMonthStatisticsList.size() > 0) {
                for (ReportFeeMonthStatisticsDto reportFeeMonthStatisticsDto1 : reportFeeMonthStatisticsList) {
                    reportFeeMonthStatisticsDto1.setAllReceivableAmount(tmpReportFeeMonthStatisticsDto.getAllReceivableAmount());
                    reportFeeMonthStatisticsDto1.setAllReceivedAmount(tmpReportFeeMonthStatisticsDto.getAllReceivedAmount());
                    reportFeeMonthStatisticsDto1.setAllOweAmount(tmpReportFeeMonthStatisticsDto.getAllOweAmount());
                }
            }
        } else {
            reportFeeMonthStatisticsDtos = new ArrayList<>();
        }

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) count / (double) reportFeeMonthStatisticsDto.getRow()), count, reportFeeMonthStatisticsDtos);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        return responseEntity;
    }

    @Override
    public ResponseEntity<String> queryReportFloorUnitFeeSummary(ReportFeeMonthStatisticsDto reportFeeMonthStatisticsDto) {
        int count = reportFeeMonthStatisticsInnerServiceSMOImpl.queryReportFloorUnitFeeSummaryCount(reportFeeMonthStatisticsDto);

        List<ReportFeeMonthStatisticsDto> reportFeeMonthStatisticsDtos = null;
        if (count > 0) {
            reportFeeMonthStatisticsDtos = reportFeeMonthStatisticsInnerServiceSMOImpl.queryReportFloorUnitFeeSummary(reportFeeMonthStatisticsDto);
            ReportFeeMonthStatisticsDto tmpReportFeeMonthStatisticsDto = reportFeeMonthStatisticsInnerServiceSMOImpl.queryReportFloorUnitFeeSummaryMajor(reportFeeMonthStatisticsDto);
            if (reportFeeMonthStatisticsDtos != null && reportFeeMonthStatisticsDtos.size() > 0) {
                for (ReportFeeMonthStatisticsDto reportFeeMonthStatisticsDto1 : reportFeeMonthStatisticsDtos) {
                    reportFeeMonthStatisticsDto1.setAllReceivableAmount(tmpReportFeeMonthStatisticsDto.getAllReceivableAmount());
                    reportFeeMonthStatisticsDto1.setAllReceivedAmount(tmpReportFeeMonthStatisticsDto.getAllReceivedAmount());
                    reportFeeMonthStatisticsDto1.setAllOweAmount(tmpReportFeeMonthStatisticsDto.getAllOweAmount());
                }
            }
        } else {
            reportFeeMonthStatisticsDtos = new ArrayList<>();
        }

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) count / (double) reportFeeMonthStatisticsDto.getRow()), count, reportFeeMonthStatisticsDtos);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        return responseEntity;
    }

    @Override
    public ResponseEntity<String> queryFeeBreakdown(ReportFeeMonthStatisticsDto reportFeeMonthStatisticsDto) {
        int count = reportFeeMonthStatisticsInnerServiceSMOImpl.queryFeeBreakdownCount(reportFeeMonthStatisticsDto);

        List<ReportFeeMonthStatisticsDto> reportFeeMonthStatisticsDtos = null;
        if (count > 0) {
            reportFeeMonthStatisticsDtos = reportFeeMonthStatisticsInnerServiceSMOImpl.queryFeeBreakdown(reportFeeMonthStatisticsDto);
            ReportFeeMonthStatisticsDto tmpReportFeeMonthStatisticsDto = reportFeeMonthStatisticsInnerServiceSMOImpl.queryFeeBreakdownMajor(reportFeeMonthStatisticsDto);
            if (reportFeeMonthStatisticsDtos != null && reportFeeMonthStatisticsDtos.size() > 0) {
                for (ReportFeeMonthStatisticsDto reportFeeMonthStatisticsDto1 : reportFeeMonthStatisticsDtos) {
                    reportFeeMonthStatisticsDto1.setAllReceivableAmount(tmpReportFeeMonthStatisticsDto.getAllReceivableAmount());
                    reportFeeMonthStatisticsDto1.setAllReceivedAmount(tmpReportFeeMonthStatisticsDto.getAllReceivedAmount());
                    reportFeeMonthStatisticsDto1.setAllOweAmount(tmpReportFeeMonthStatisticsDto.getAllOweAmount());
                }
            }
        } else {
            reportFeeMonthStatisticsDtos = new ArrayList<>();
        }

        //?????????????????????????????????
        FeeConfigDto feeConfigDto = new FeeConfigDto();
        feeConfigDto.setCommunityId(reportFeeMonthStatisticsDto.getCommunityId());
        List<FeeConfigDto> feeConfigDtos = reportFeeMonthStatisticsInnerServiceSMOImpl.queryFeeConfigs(feeConfigDto);

        List<ReportFeeMonthStatisticsDto> reportList = new ArrayList<>();
        for (ReportFeeMonthStatisticsDto reportFeeMonthStatistics : reportFeeMonthStatisticsDtos) {
            reportFeeMonthStatistics.setFeeConfigDtos(feeConfigDtos);
            reportList.add(reportFeeMonthStatistics);
        }

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) count / (double) reportFeeMonthStatisticsDto.getRow()), count, reportList);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        return responseEntity;
    }

    @Override
    public ResponseEntity<String> queryFeeDetail(ReportFeeMonthStatisticsDto reportFeeMonthStatisticsDto) {
        int count = reportFeeMonthStatisticsInnerServiceSMOImpl.queryFeeDetailCount(reportFeeMonthStatisticsDto);

        List<ReportFeeMonthStatisticsDto> reportFeeMonthStatisticsDtos = null;
        //???????????????(??????)
        Double allReceivableAmount = 0.0;
        //????????????(??????)
        Double allReceivedAmount = 0.0;
        if (count > 0) {
            reportFeeMonthStatisticsDtos = reportFeeMonthStatisticsInnerServiceSMOImpl.queryFeeDetail(reportFeeMonthStatisticsDto);
            List<ReportFeeMonthStatisticsDto> reportFeeMonthStatisticsList = reportFeeMonthStatisticsInnerServiceSMOImpl.queryAllFeeDetail(reportFeeMonthStatisticsDto);
            allReceivableAmount = Double.valueOf(reportFeeMonthStatisticsList.get(0).getAllReceivableAmount());
            allReceivedAmount = Double.valueOf(reportFeeMonthStatisticsList.get(0).getAllReceivedAmount());
        } else {
            reportFeeMonthStatisticsDtos = new ArrayList<>();
        }

        //???????????????(??????)
        Double totalReceivableAmount = 0.0;
        //???????????????(??????)
        Double totalReceivedAmount = 0.0;
        List<ReportFeeMonthStatisticsDto> reportList = new ArrayList<>();
        for (ReportFeeMonthStatisticsDto reportFeeMonthStatistics : reportFeeMonthStatisticsDtos) {
            //????????????
            Double receivableAmount = Double.valueOf(reportFeeMonthStatistics.getReceivableAmount());
            //????????????
            Double receivedAmount = Double.valueOf(reportFeeMonthStatistics.getReceivedAmount());
            totalReceivableAmount = totalReceivableAmount + receivableAmount;
            totalReceivedAmount = totalReceivedAmount + receivedAmount;
        }

        //?????????????????????????????????
        FeeConfigDto feeConfigDto = new FeeConfigDto();
        feeConfigDto.setCommunityId(reportFeeMonthStatisticsDto.getCommunityId());
        List<FeeConfigDto> feeConfigDtos = reportFeeMonthStatisticsInnerServiceSMOImpl.queryFeeConfigs(feeConfigDto);

        for (ReportFeeMonthStatisticsDto reportFeeMonthStatistics : reportFeeMonthStatisticsDtos) {
            reportFeeMonthStatistics.setTotalReceivableAmount(String.format("%.2f", totalReceivableAmount));
            reportFeeMonthStatistics.setTotalReceivedAmount(String.format("%.2f", totalReceivedAmount));
            reportFeeMonthStatistics.setAllReceivableAmount(String.format("%.2f", allReceivableAmount));
            reportFeeMonthStatistics.setAllReceivedAmount(String.format("%.2f", allReceivedAmount));
            reportFeeMonthStatistics.setFeeConfigDtos(feeConfigDtos);
            reportList.add(reportFeeMonthStatistics);
        }

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) count / (double) reportFeeMonthStatisticsDto.getRow()), count, reportList);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        return responseEntity;
    }

    @Override
    public ResponseEntity<String> queryOweFeeDetail(ReportFeeMonthStatisticsDto reportFeeMonthStatisticsDto) {
        int count = reportFeeMonthStatisticsInnerServiceSMOImpl.queryOweFeeDetailCount(reportFeeMonthStatisticsDto);

        List<ReportFeeMonthStatisticsDto> reportFeeMonthStatisticsDtos = null;
        if (count > 0) {
            reportFeeMonthStatisticsDtos = reportFeeMonthStatisticsInnerServiceSMOImpl.queryOweFeeDetail(reportFeeMonthStatisticsDto);
            ReportFeeMonthStatisticsDto tmpReportFeeMonthStatisticsDto = reportFeeMonthStatisticsInnerServiceSMOImpl.queryOweFeeDetailMajor(reportFeeMonthStatisticsDto);
            if (reportFeeMonthStatisticsDtos != null && reportFeeMonthStatisticsDtos.size() > 0) {
                for (ReportFeeMonthStatisticsDto reportFeeMonthStatisticsDto1 : reportFeeMonthStatisticsDtos) {
//                    reportFeeMonthStatisticsDto1.setAllReceivableAmount(tmpReportFeeMonthStatisticsDto.getAllReceivableAmount());
//                    reportFeeMonthStatisticsDto1.setAllReceivedAmount(tmpReportFeeMonthStatisticsDto.getAllReceivedAmount());
                    reportFeeMonthStatisticsDto1.setAllOweAmount(tmpReportFeeMonthStatisticsDto.getOweAmount());
                }
            }
            freshReportOweDay(reportFeeMonthStatisticsDtos);
        } else {
            reportFeeMonthStatisticsDtos = new ArrayList<>();
        }

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) count / (double) reportFeeMonthStatisticsDto.getRow()), count, reportFeeMonthStatisticsDtos);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        return responseEntity;
    }

    @Override
    public ResponseEntity<String> queryPayFeeDetail(ReportFeeMonthStatisticsDto reportFeeMonthStatisticsDto) {

        JSONObject countInfo = reportFeeMonthStatisticsInnerServiceSMOImpl.queryPayFeeDetailCount(reportFeeMonthStatisticsDto);

        int count = Integer.parseInt(countInfo.get("count").toString());

        List<ReportFeeMonthStatisticsDto> reportFeeMonthStatisticsDtos = null;
        ReportFeeMonthStatisticsTotalDto reportFeeMonthStatisticsTotalDto = new ReportFeeMonthStatisticsTotalDto();
        List<ReportFeeMonthStatisticsDto> reportList = new ArrayList<>();
        //?????????????????????????????????
        FeeConfigDto feeConfigDto = new FeeConfigDto();
        feeConfigDto.setCommunityId(reportFeeMonthStatisticsDto.getCommunityId());
        List<FeeConfigDto> feeConfigDtos = reportFeeMonthStatisticsInnerServiceSMOImpl.queryFeeConfigs(feeConfigDto);
        //???????????????(??????)
        Double allReceivableAmount = 0.0;
        //????????????(??????)
        Double allReceivedAmount = 0.0;
        //????????????(??????)
        Double allPreferentialAmount = 0.0;
        //????????????(??????)
        Double allDeductionAmount = 0.0;
        //?????????(??????)
        Double allLateFee = 0.0;
        //???????????????(??????)
        Double allVacantHousingDiscount = 0.0;
        //???????????????(??????)
        Double allVacantHousingReduction = 0.0;
        int size = 0;
        if (count > 0) {
            //??????????????????
            reportFeeMonthStatisticsDtos = reportFeeMonthStatisticsInnerServiceSMOImpl.queryPayFeeDetail(reportFeeMonthStatisticsDto);
            if (reportFeeMonthStatisticsDtos != null && reportFeeMonthStatisticsDtos.size() > 0) {
                //??????????????????????????????
                ReportFeeMonthStatisticsDto reportFeeMonthStatisticsDto1 = BeanConvertUtil.covertBean(reportFeeMonthStatisticsDto, ReportFeeMonthStatisticsDto.class);
                reportFeeMonthStatisticsDto1.setPage(PageDto.DEFAULT_PAGE);
                List<ReportFeeMonthStatisticsDto> reportFeeMonthStatisticsDtos1 = reportFeeMonthStatisticsInnerServiceSMOImpl.queryPayFeeDetail(reportFeeMonthStatisticsDto1);
                size = reportFeeMonthStatisticsDtos1.size();
            }
            //??????????????????????????????(??????)
            List<ReportFeeMonthStatisticsDto> reportFeeMonthStatisticsList = reportFeeMonthStatisticsInnerServiceSMOImpl.queryAllPayFeeDetail(reportFeeMonthStatisticsDto);
            //??????(????????????????????????????????????????????????????????????????????????)???????????????
            List<ReportFeeMonthStatisticsDto> reportFeeMonthStatisticsSum = reportFeeMonthStatisticsInnerServiceSMOImpl.queryPayFeeDetailSum(reportFeeMonthStatisticsDto);
            allReceivableAmount = Double.valueOf(reportFeeMonthStatisticsList.get(0).getAllReceivableAmount());
            allReceivedAmount = Double.valueOf(reportFeeMonthStatisticsList.get(0).getAllReceivedAmount());
            for (ReportFeeMonthStatisticsDto reportFeeMonthStatistics : reportFeeMonthStatisticsSum) {
                //?????????????????????????????????
                String discountPrice = reportFeeMonthStatistics.getDiscountPrice();
                //????????????(??????)
                if (!StringUtil.isEmpty(reportFeeMonthStatistics.getDiscountSmallType()) && reportFeeMonthStatistics.getDiscountSmallType().equals("1")) {
                    allPreferentialAmount = Double.valueOf(discountPrice);
                }
                //????????????(??????)
                if (!StringUtil.isEmpty(reportFeeMonthStatistics.getDiscountSmallType()) && reportFeeMonthStatistics.getDiscountSmallType().equals("2")) {
                    allDeductionAmount = Double.valueOf(discountPrice);
                }
                //?????????(??????)
                if (!StringUtil.isEmpty(reportFeeMonthStatistics.getDiscountSmallType()) && reportFeeMonthStatistics.getDiscountSmallType().equals("3")) {
                    allLateFee = Double.valueOf(discountPrice);
                }
                //?????????????????????(??????)
                if (!StringUtil.isEmpty(reportFeeMonthStatistics.getDiscountSmallType()) && reportFeeMonthStatistics.getDiscountSmallType().equals("4")) {
                    allVacantHousingDiscount = Double.valueOf(discountPrice);
                }
                //?????????????????????(??????)
                if (!StringUtil.isEmpty(reportFeeMonthStatistics.getDiscountSmallType()) && reportFeeMonthStatistics.getDiscountSmallType().equals("5")) {
                    allVacantHousingReduction = Double.valueOf(discountPrice);
                }
            }
            //???????????????(??????)
            Double totalReceivableAmount = 0.0;
            //???????????????(??????)
            Double totalReceivedAmount = 0.0;
            //????????????(??????)
            Double totalPreferentialAmount = 0.0;
            //????????????(??????)
            Double totalDeductionAmount = 0.0;
            //?????????????????????(??????)
            Double totalVacantHousingDiscount = 0.0;
            //?????????????????????(??????)
            Double totalVacantHousingReduction = 0.0;
            //?????????(??????)
            Double totalLateFee = 0.0;
            for (ReportFeeMonthStatisticsDto reportFeeMonthStatistics : reportFeeMonthStatisticsDtos) {
                //????????????
                Double receivableAmount = Double.valueOf(reportFeeMonthStatistics.getReceivableAmount());
                //????????????
                Double receivedAmount = Double.valueOf(reportFeeMonthStatistics.getReceivedAmount());
                totalReceivableAmount = totalReceivableAmount + receivableAmount;
                totalReceivedAmount = totalReceivedAmount + receivedAmount;
                //????????????
                if (!StringUtil.isEmpty(reportFeeMonthStatistics.getDiscountSmallType()) && reportFeeMonthStatistics.getDiscountSmallType().equals("1")) {
                    //??????????????????
                    Double discountPrice = Double.valueOf(reportFeeMonthStatistics.getDiscountPrice());
                    totalPreferentialAmount = totalPreferentialAmount + discountPrice;
                    //????????????
                    reportFeeMonthStatistics.setPreferentialAmount(reportFeeMonthStatistics.getDiscountPrice());
                } else {
                    reportFeeMonthStatistics.setPreferentialAmount("0");
                }
                //????????????
                if (!StringUtil.isEmpty(reportFeeMonthStatistics.getDiscountSmallType()) && reportFeeMonthStatistics.getDiscountSmallType().equals("2")) {
                    //??????????????????
                    Double discountPrice = Double.valueOf(reportFeeMonthStatistics.getDiscountPrice());
                    totalDeductionAmount = totalDeductionAmount + discountPrice;
                    //????????????
                    reportFeeMonthStatistics.setDeductionAmount(reportFeeMonthStatistics.getDiscountPrice());
                } else {
                    reportFeeMonthStatistics.setDeductionAmount("0");
                }
                //?????????
                if (!StringUtil.isEmpty(reportFeeMonthStatistics.getDiscountSmallType()) && reportFeeMonthStatistics.getDiscountSmallType().equals("3")) {
                    //?????????????????????
                    Double discountPrice = (Double.valueOf(reportFeeMonthStatistics.getDiscountPrice())) * (-1);
                    totalLateFee = totalLateFee + discountPrice;
                    //?????????
                    reportFeeMonthStatistics.setLateFee(reportFeeMonthStatistics.getDiscountPrice());
                } else {
                    reportFeeMonthStatistics.setLateFee("0");
                }
                //???????????????
                if (!StringUtil.isEmpty(reportFeeMonthStatistics.getDiscountSmallType()) && reportFeeMonthStatistics.getDiscountSmallType().equals("4")) {
                    //?????????????????????
                    Double discountPrice = Double.valueOf(reportFeeMonthStatistics.getDiscountPrice());
                    totalVacantHousingDiscount = totalVacantHousingDiscount + discountPrice;
                    //???????????????
                    reportFeeMonthStatistics.setVacantHousingDiscount(reportFeeMonthStatistics.getDiscountPrice());
                } else {
                    reportFeeMonthStatistics.setVacantHousingDiscount("0");
                }
                //???????????????
                if (!StringUtil.isEmpty(reportFeeMonthStatistics.getDiscountSmallType()) && reportFeeMonthStatistics.getDiscountSmallType().equals("5")) {
                    //?????????????????????
                    Double discountPrice = Double.valueOf(reportFeeMonthStatistics.getDiscountPrice());
                    totalVacantHousingReduction = totalVacantHousingReduction + discountPrice;
                    //???????????????
                    reportFeeMonthStatistics.setVacantHousingReduction(reportFeeMonthStatistics.getDiscountPrice());
                } else {
                    reportFeeMonthStatistics.setVacantHousingReduction("0");
                }
                if (FeeDto.PAYER_OBJ_TYPE_ROOM.equals(reportFeeMonthStatistics.getPayerObjType())) {
                    reportFeeMonthStatistics.setObjName(reportFeeMonthStatistics.getFloorNum()
                            + "???" + reportFeeMonthStatistics.getUnitNum()
                            + "??????" + reportFeeMonthStatistics.getRoomNum() + "???");
                } else if (FeeDto.PAYER_OBJ_TYPE_CAR.equals(reportFeeMonthStatistics.getPayerObjType())) {
                    reportFeeMonthStatistics.setObjName(reportFeeMonthStatistics.getCarNum());
                } else {
                    reportFeeMonthStatistics.setObjName(reportFeeMonthStatistics.getContractCode());

                }
                if (!StringUtil.isEmpty(reportFeeMonthStatistics.getImportFeeName())) {
                    reportFeeMonthStatistics.setFeeName(reportFeeMonthStatistics.getImportFeeName());
                }
                //????????????
                reportFeeMonthStatistics.setFeeConfigDtos(feeConfigDtos);
                if (!StringUtil.isEmpty(reportFeeMonthStatistics.getRepairId())) {
                    RepairDto repairDto = new RepairDto();
                    repairDto.setRepairId(reportFeeMonthStatistics.getRepairId());
                    //???????????????
                    List<RepairDto> repairDtos = repairInnerServiceSMOImpl.queryRepairs(repairDto);
                    Assert.listOnlyOne(repairDtos, "????????????????????????");
                    if (!StringUtil.isEmpty(repairDtos.get(0).getRepairObjType()) && repairDtos.get(0).getRepairObjType().equals("004")) {
                        OwnerRoomRelDto ownerRoomRelDto = new OwnerRoomRelDto();
                        ownerRoomRelDto.setRoomId(repairDtos.get(0).getRepairObjId());
                        List<OwnerRoomRelDto> ownerRoomRelDtos = ownerRoomRelInnerServiceSMOImpl.queryOwnerRoomRels(ownerRoomRelDto);
                        if (ownerRoomRelDtos != null && ownerRoomRelDtos.size() == 0) { //???????????????0???
                            OwnerRoomRelDto ownerRoomRel = new OwnerRoomRelDto();
                            ownerRoomRel.setRoomId(repairDtos.get(0).getRepairObjId());
                            ownerRoomRel.setStatusCd("1"); //?????????????????????????????????????????????
                            List<OwnerRoomRelDto> ownerRoomRels = ownerRoomRelInnerServiceSMOImpl.queryOwnerRoomRels(ownerRoomRel);
                            Assert.listOnlyOne(ownerRoomRels, "????????????????????????????????????");
                            OwnerDto owner = new OwnerDto();
                            owner.setOwnerId(ownerRoomRels.get(0).getOwnerId());
                            owner.setOwnerTypeCd("1001"); //????????????
                            List<OwnerDto> owners = ownerInnerServiceSMOImpl.queryOwners(owner);
                            if (owners != null && owners.size() == 0) { //???????????????0???
                                //???????????????????????????
                                OwnerDto newOwner = new OwnerDto();
                                newOwner.setOwnerId(ownerRoomRels.get(0).getOwnerId());
                                newOwner.setOwnerTypeCd("1001"); //????????????
                                newOwner.setStatusCd("1");
                                List<OwnerDto> newOwners = ownerInnerServiceSMOImpl.queryOwners(newOwner);
                                Assert.listOnlyOne(newOwners, "???????????????????????????");
                                reportFeeMonthStatistics.setOwnerName(newOwners.get(0).getName());
                            } else if (owners != null && owners.size() == 1) { //???????????????1???
                                reportFeeMonthStatistics.setOwnerName(owners.get(0).getName());
                            } else {
                                throw new IllegalArgumentException("???????????????????????????");
                            }
                        } else if (ownerRoomRelDtos != null && ownerRoomRelDtos.size() == 1) { //???????????????1???
                            OwnerDto ownerDto = new OwnerDto();
                            ownerDto.setOwnerId(ownerRoomRelDtos.get(0).getOwnerId());
                            ownerDto.setOwnerTypeCd("1001"); //????????????
                            List<OwnerDto> ownerDtos = ownerInnerServiceSMOImpl.queryOwners(ownerDto);
                            if (ownerDtos != null && ownerDtos.size() == 0) { //?????????????????????0???
                                //???????????????????????????
                                OwnerDto newOwner = new OwnerDto();
                                newOwner.setOwnerId(ownerRoomRelDtos.get(0).getOwnerId());
                                newOwner.setOwnerTypeCd("1001"); //????????????
                                newOwner.setStatusCd("1");
                                List<OwnerDto> newOwners = ownerInnerServiceSMOImpl.queryOwners(newOwner);
                                Assert.listOnlyOne(newOwners, "???????????????????????????");
                                reportFeeMonthStatistics.setOwnerName(newOwners.get(0).getName());
                            } else if (ownerDtos != null || ownerDtos.size() == 1) {
                                reportFeeMonthStatistics.setOwnerName(ownerDtos.get(0).getName());
                            } else {
                                throw new IllegalArgumentException("???????????????????????????");
                            }
                        } else {
                            throw new IllegalArgumentException("????????????????????????????????????");
                        }
                    }
                }
                reportList.add(reportFeeMonthStatistics);
            }
            //???????????????(??????)
            reportFeeMonthStatisticsTotalDto.setTotalReceivableAmount(String.format("%.2f", totalReceivableAmount));
            //????????????(??????)
            reportFeeMonthStatisticsTotalDto.setTotalReceivedAmount(String.format("%.2f", totalReceivedAmount));
            //????????????(??????)
            reportFeeMonthStatisticsTotalDto.setTotalPreferentialAmount(String.valueOf(totalPreferentialAmount));
            //????????????(??????)
            reportFeeMonthStatisticsTotalDto.setTotalDeductionAmount(String.valueOf(totalDeductionAmount));
            //?????????(??????)
            reportFeeMonthStatisticsTotalDto.setTotalLateFee(String.valueOf(totalLateFee));
            //???????????????(??????)
            reportFeeMonthStatisticsTotalDto.setTotalVacantHousingDiscount(String.valueOf(totalVacantHousingDiscount));
            //???????????????(??????)
            reportFeeMonthStatisticsTotalDto.setTotalVacantHousingReduction(String.valueOf(totalVacantHousingReduction));
            //????????????(??????)
            reportFeeMonthStatisticsTotalDto.setAllReceivableAmount(String.format("%.2f", allReceivableAmount));
            //????????????(??????)
            reportFeeMonthStatisticsTotalDto.setAllReceivedAmount(String.format("%.2f", allReceivedAmount));
            //????????????(??????)
            reportFeeMonthStatisticsTotalDto.setAllPreferentialAmount(String.valueOf(allPreferentialAmount));
            //????????????(??????)
            reportFeeMonthStatisticsTotalDto.setAllDeductionAmount(String.valueOf(allDeductionAmount));
            //?????????(??????)
            reportFeeMonthStatisticsTotalDto.setAllLateFee(String.valueOf(allLateFee));
            //?????????????????????(??????)
            reportFeeMonthStatisticsTotalDto.setAllVacantHousingDiscount(String.valueOf(allVacantHousingDiscount));
            //?????????????????????(??????)
            reportFeeMonthStatisticsTotalDto.setAllVacantHousingReduction(String.valueOf(allVacantHousingReduction));
        } else {
            reportFeeMonthStatisticsDtos = new ArrayList<>();
            reportList.addAll(reportFeeMonthStatisticsDtos);
            reportFeeMonthStatisticsTotalDto = new ReportFeeMonthStatisticsTotalDto();
        }

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) size / (double) reportFeeMonthStatisticsDto.getRow()), size, reportList, reportFeeMonthStatisticsTotalDto);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        return responseEntity;
    }

    @Override
    public ResponseEntity<String> queryDeadlineFee(ReportFeeMonthStatisticsDto reportFeeMonthStatisticsDto) {
        int count = reportFeeMonthStatisticsInnerServiceSMOImpl.queryDeadlineFeeCount(reportFeeMonthStatisticsDto);

        List<ReportFeeMonthStatisticsDto> reportFeeMonthStatisticsDtos = null;
        if (count > 0) {
            reportFeeMonthStatisticsDtos = reportFeeMonthStatisticsInnerServiceSMOImpl.queryDeadlineFee(reportFeeMonthStatisticsDto);

            freshReportDeadlineDay(reportFeeMonthStatisticsDtos);
        } else {
            reportFeeMonthStatisticsDtos = new ArrayList<>();
        }

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) count / (double) reportFeeMonthStatisticsDto.getRow()), count, reportFeeMonthStatisticsDtos);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        return responseEntity;
    }

    @Override
    public ResponseEntity<String> queryPrePaymentCount(ReportFeeMonthStatisticsDto reportFeeMonthStatisticsDto) {

        List<ReportFeeMonthStatisticsDto> reportFeeMonthStatisticsDtos = null;

        reportFeeMonthStatisticsDtos = reportFeeMonthStatisticsInnerServiceSMOImpl.queryPrePaymentCount(reportFeeMonthStatisticsDto);

        ResultVo resultVo = new ResultVo(reportFeeMonthStatisticsDtos);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        return responseEntity;
    }

    @Override
    public ResponseEntity<String> queryDeadlinePaymentCount(ReportFeeMonthStatisticsDto reportFeeMonthStatisticsDto) {

        List<ReportFeeMonthStatisticsDto> reportFeeMonthStatisticsDtos = null;

        reportFeeMonthStatisticsDtos = reportFeeMonthStatisticsInnerServiceSMOImpl.queryDeadlinePaymentCount(reportFeeMonthStatisticsDto);

        ResultVo resultVo = new ResultVo(reportFeeMonthStatisticsDtos);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        return responseEntity;
    }

    @Override
    public ResponseEntity<String> queryOwePaymentCount(ReportFeeMonthStatisticsDto reportFeeMonthStatisticsDto) {

        List<ReportFeeMonthStatisticsDto> reportFeeMonthStatisticsDtos = null;
        List<ReportFeeMonthStatisticsDto> reportAllFeeMonthStatisticsDtos = null;

        reportFeeMonthStatisticsDtos = reportFeeMonthStatisticsInnerServiceSMOImpl.queryOwePaymentCount(reportFeeMonthStatisticsDto);

        reportAllFeeMonthStatisticsDtos = reportFeeMonthStatisticsInnerServiceSMOImpl.queryAllPaymentCount(reportFeeMonthStatisticsDto);
        int normalFee = 0;
        for (ReportFeeMonthStatisticsDto aReportFeeMonthStatisticsDto : reportAllFeeMonthStatisticsDtos) {
            for (ReportFeeMonthStatisticsDto oweReportFeeMonthStatisticsDto : reportFeeMonthStatisticsDtos) {
                String objCount = aReportFeeMonthStatisticsDto.getObjCount();
                if (aReportFeeMonthStatisticsDto.getFeeName().equals(oweReportFeeMonthStatisticsDto.getFeeName())) {
                    aReportFeeMonthStatisticsDto.setObjCount(oweReportFeeMonthStatisticsDto.getObjCount());
                    normalFee = Integer.parseInt(objCount) - Integer.parseInt(oweReportFeeMonthStatisticsDto.getObjCount());
                    aReportFeeMonthStatisticsDto.setNormalCount(normalFee + "");
                }
            }
        }


        ResultVo resultVo = new ResultVo(reportAllFeeMonthStatisticsDtos);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        return responseEntity;
    }

    /**
     * ?????????????????? ????????????
     *
     * @param reportFeeMonthStatisticsDto
     * @return
     */
    @Override
    public ResponseEntity<String> queryReportProficientCount(ReportFeeMonthStatisticsDto reportFeeMonthStatisticsDto) {
        JSONObject result = reportFeeMonthStatisticsInnerServiceSMOImpl.queryReportProficientCount(reportFeeMonthStatisticsDto);
        ResponseEntity<String> responseEntity = new ResponseEntity<String>(result.toString(), HttpStatus.OK);

        return responseEntity;
    }

    /**
     * ??????????????????
     *
     * @param repairUserDto
     * @return
     */
    @Override
    public ResponseEntity<String> queryRepair(RepairUserDto repairUserDto) {
        //?????????????????????????????????
        List<RepairUserDto> repairUsers = reportFeeMonthStatisticsInnerServiceSMOImpl.queryRepairForStaff(repairUserDto);
        int count = repairUsers.size();
        //????????????id???????????????
        List<RepairUserDto> staffs;
        if (StringUtil.isEmpty(repairUserDto.getStaffId())) {
            repairUserDto.setPage(-1);
            staffs = reportFeeMonthStatisticsInnerServiceSMOImpl.queryRepairForStaff(repairUserDto);
        } else {
            repairUserDto.setPage(-1);
            repairUserDto.setStaffId(null);
            staffs = reportFeeMonthStatisticsInnerServiceSMOImpl.queryRepairForStaff(repairUserDto);
        }
        List<RepairUserDto> repairUserList = new ArrayList<>();
        //??????????????????
        int dealNumber = 0;
        //???????????????
        int statementNumber = 0;
        //???????????????
        int chargebackNumber = 0;
        //???????????????
        int transferOrderNumber = 0;
        //???????????????
        int dispatchNumber = 0;
        //??????????????????
        int returnNumber = 0;
        if (count > 0) {
            for (RepairUserDto repairUser : repairUsers) {
                RepairUserDto repairUserInfo = new RepairUserDto();
                //??????id
                repairUserDto.setStaffId(repairUser.getStaffId());
                List<RepairUserDto> repairUserDtoList = reportFeeMonthStatisticsInnerServiceSMOImpl.queryRepairWithOutPage(repairUserDto);
                if (repairUserDtoList != null && repairUserDtoList.size() > 0) {
                    //???????????????
                    int dealAmount = 0;
                    //????????????
                    int statementAmount = 0;
                    //????????????
                    int chargebackAmount = 0;
                    //????????????
                    int transferOrderAmount = 0;
                    //????????????
                    int dispatchAmount = 0;
                    //????????????
                    int returnAmount = 0;
                    //??????
                    String score = "";
                    for (RepairUserDto repair : repairUserDtoList) {
                        //???????????????
                        if (repair.getState().equals("10001")) {
                            //????????????
                            int amount = Integer.parseInt(repair.getAmount());
                            dealAmount = dealAmount + amount;
                        } else if (repair.getState().equals("10002")) {  //????????????
                            //????????????
                            int amount = Integer.parseInt(repair.getAmount());
                            statementAmount = statementAmount + amount;
                        } else if (repair.getState().equals("10003")) {  //????????????
                            //????????????
                            int amount = Integer.parseInt(repair.getAmount());
                            chargebackAmount = chargebackAmount + amount;
                        } else if (repair.getState().equals("10004")) {  //????????????
                            //????????????
                            int amount = Integer.parseInt(repair.getAmount());
                            transferOrderAmount = transferOrderAmount + amount;
                        } else if (repair.getState().equals("10006")) {  //????????????
                            int amount = Integer.parseInt(repair.getAmount());
                            dispatchAmount = dispatchAmount + amount;
                        } else if (repair.getState().equals("10008")) {  //???????????????
                            int amount = Integer.parseInt(repair.getAmount());
                            returnAmount = returnAmount + amount;
                        }
                        if (!StringUtil.isEmpty(repair.getScore())) {
                            score = repair.getScore();
                        }
                    }
                    //??????id
                    repairUserInfo.setStaffId(repairUser.getStaffId());
                    //????????????
                    repairUserInfo.setStaffName(repairUser.getStaffName());
                    //?????????????????????
                    repairUserInfo.setDealAmount(Integer.toString(dealAmount));
                    //????????????????????????
                    repairUserInfo.setDealNumber(Integer.toString(dealNumber));
                    //??????????????????
                    repairUserInfo.setStatementAmount(Integer.toString(statementAmount));
                    //?????????????????????
                    repairUserInfo.setStatementNumber(Integer.toString(statementNumber));
                    //??????????????????
                    repairUserInfo.setChargebackAmount(Integer.toString(chargebackAmount));
                    //?????????????????????
                    repairUserInfo.setChargebackNumber(Integer.toString(chargebackNumber));
                    //??????????????????
                    repairUserInfo.setTransferOrderAmount(Integer.toString(transferOrderAmount));
                    //?????????????????????
                    repairUserInfo.setTransferOrderNumber(Integer.toString(transferOrderNumber));
                    //??????????????????
                    repairUserInfo.setDispatchAmount(Integer.toString(dispatchAmount));
                    //?????????????????????
                    repairUserInfo.setDispatchNumber(Integer.toString(dispatchNumber));
                    //????????????
                    repairUserInfo.setReturnAmount(Integer.toString(returnAmount));
                    //???????????????
                    repairUserInfo.setReturnNumber(Integer.toString(returnNumber));
                    //??????id?????????????????????
                    repairUserInfo.setRepairList(staffs);
                    //????????????
                    repairUserInfo.setScore(score);
                    repairUserList.add(repairUserInfo);
                } else {
                    continue;
                }
                dealNumber = Integer.parseInt(repairUserInfo.getDealAmount()) + dealNumber;
                statementNumber = Integer.parseInt(repairUserInfo.getStatementAmount()) + statementNumber;
                chargebackNumber = Integer.parseInt(repairUserInfo.getChargebackAmount()) + chargebackNumber;
                transferOrderNumber = Integer.parseInt(repairUserInfo.getTransferOrderAmount()) + transferOrderNumber;
                dispatchNumber = Integer.parseInt(repairUserInfo.getDispatchAmount()) + dispatchNumber;
                returnNumber = Integer.parseInt(repairUserInfo.getReturnAmount()) + returnNumber;
            }
        } else {
            repairUserList = new ArrayList<>();
        }

        RepairUserDto repairUser = new RepairUserDto();
        repairUser.setDealNumber(Integer.toString(dealNumber));
        repairUser.setStatementNumber(Integer.toString(statementNumber));
        repairUser.setChargebackNumber(Integer.toString(chargebackNumber));
        repairUser.setTransferOrderNumber(Integer.toString(transferOrderNumber));
        repairUser.setDispatchNumber(Integer.toString(dispatchNumber));
        repairUser.setReturnNumber(Integer.toString(returnNumber));

        //???????????????
        int size = staffs.size();

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) size / (double) repairUserDto.getRow()), repairUserList.size(), repairUserList, staffs, repairUser);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        return responseEntity;
    }

    @Override
    public ResponseEntity<String> queryNoFeeRooms(RoomDto roomDto) {
        int count = reportFeeMonthStatisticsInnerServiceSMOImpl.queryNoFeeRoomsCount(roomDto);

        List<RoomDto> roomDtos = null;
        if (count > 0) {
            roomDtos = reportFeeMonthStatisticsInnerServiceSMOImpl.queryNoFeeRooms(roomDto);
        } else {
            roomDtos = new ArrayList<>();
        }

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) count / (double) roomDto.getRow()), count, roomDtos);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        return responseEntity;
    }

    @Override
    public ResponseEntity<String> queryPayFeeDeposit(ReportDeposit reportDeposit) {
        //????????????
        List<ReportDeposit> reportDeposits = reportFeeMonthStatisticsInnerServiceSMOImpl.queryPayFeeDeposit(reportDeposit);
        //???????????????????????????
        List<ReportDeposit> reportDepositAmounts = reportFeeMonthStatisticsInnerServiceSMOImpl.queryFeeDepositAmount(reportDeposit);

        //?????????????????????
        FeeConfigDto feeConfigDto = new FeeConfigDto();
        feeConfigDto.setCommunityId(reportDeposit.getCommunityId());
        feeConfigDto.setFeeTypeCd("888800010006");
        List<FeeConfigDto> feeConfigDtos = feeConfigInnerServiceSMOImpl.queryFeeConfigs(feeConfigDto);

        List<ReportDeposit> newReportDeposits = new ArrayList<>();
        BigDecimal unpaidfeeAmount = new BigDecimal(0);//?????????
        BigDecimal unpaidfeeAmounts = new BigDecimal(0);//??????????????????
        BigDecimal paidfeeAmount = new BigDecimal(0);//?????????
        BigDecimal paidfeeAmounts = new BigDecimal(0);//??????????????????
        BigDecimal refundedAmount = new BigDecimal(0);//?????????
        BigDecimal refundedAmounts = new BigDecimal(0);//??????????????????
        BigDecimal refundInProgressAmount = new BigDecimal(0); //?????????
        BigDecimal refundInProgressAmounts = new BigDecimal(0);//??????????????????
        BigDecimal refundFailedAmount = new BigDecimal(0); //????????????
        BigDecimal refundFailedAmounts = new BigDecimal(0);//?????????????????????
        for (ReportDeposit deposit : reportDeposits) {
            deposit.setFeeConfigDtos(feeConfigDtos);
            newReportDeposits.add(deposit);
            if (FeeDto.PAYER_OBJ_TYPE_ROOM.equals(deposit.getPayerObjType())) {
                deposit.setObjName(deposit.getFloorNum()
                        + "???" + deposit.getUnitNum()
                        + "??????" + deposit.getRoomNum() + "???");
            } else if (FeeDto.PAYER_OBJ_TYPE_CAR.equals(deposit.getPayerObjType())) {
                deposit.setObjName(deposit.getCarNum());
            }
            //????????????????????????
            if ("2008001".equals(deposit.getState())) {
                unpaidfeeAmount = unpaidfeeAmount.add(new BigDecimal(deposit.getAdditionalAmount()));
            }
            //???????????????????????????
            if ("2009001".equals(deposit.getState()) && !StringUtil.isEmpty(deposit.getDetailState()) && "1400".equals(deposit.getDetailState())) {
                paidfeeAmount = paidfeeAmount.add(new BigDecimal(deposit.getAdditionalAmount()));
            }
            if (!StringUtil.isEmpty(deposit.getDetailState()) && "1100".equals(deposit.getDetailState())) {//?????????
                refundedAmount = refundedAmount.add(new BigDecimal(deposit.getAdditionalAmount()));
            }
            if (!StringUtil.isEmpty(deposit.getDetailState()) && "1000".equals(deposit.getDetailState())) {//?????????
                refundInProgressAmount = refundInProgressAmount.add(new BigDecimal(deposit.getAdditionalAmount()));
            }
            if (!StringUtil.isEmpty(deposit.getDetailState()) && "1200".equals(deposit.getDetailState())) {//????????????
                refundFailedAmount = refundFailedAmount.add(new BigDecimal(deposit.getAdditionalAmount()));
            }
        }
        for (ReportDeposit reportDeposit1 : reportDepositAmounts) {
            if (StringUtil.isEmpty(reportDeposit1.getAllAmount())) {
                throw new IllegalArgumentException("????????????????????????");
            }
            //???????????????
            BigDecimal bd = new BigDecimal(reportDeposit1.getAllAmount());
            if (StringUtil.isEmpty(reportDeposit1.getDetailState())) { //????????????????????????
                unpaidfeeAmounts = bd;
            } else if (reportDeposit1.getDetailState().equals("1000")) { //????????????????????????
                refundInProgressAmounts = bd;
            } else if (reportDeposit1.getDetailState().equals("1100")) { //????????????????????????
                refundedAmounts = bd;
            } else if (reportDeposit1.getDetailState().equals("1200")) { //???????????????????????????
                refundFailedAmounts = bd;
            } else if (reportDeposit1.getDetailState().equals("1400")) { //????????????????????????
                paidfeeAmounts = bd;
            }
        }
        HashMap<String, String> mp = new HashMap<>();
        mp.put("unpaidfeeAmount", unpaidfeeAmount.toString());
        mp.put("paidfeeAmount", paidfeeAmount.toString());
        mp.put("refundedAmount", refundedAmount.toString());
        mp.put("refundInProgressAmount", refundInProgressAmount.toString());
        mp.put("refundFailedAmount", refundFailedAmount.toString());
        mp.put("unpaidfeeAmounts", unpaidfeeAmounts.toString());
        mp.put("paidfeeAmounts", paidfeeAmounts.toString());
        mp.put("refundedAmounts", refundedAmounts.toString());
        mp.put("refundInProgressAmounts", refundInProgressAmounts.toString());
        mp.put("refundFailedAmounts", refundFailedAmounts.toString());
        int size = 0;
        if (newReportDeposits != null && newReportDeposits.size() > 0) {
            //??????????????????
            reportDeposit.setPage(PageDto.DEFAULT_PAGE);
            List<ReportDeposit> reportDeposits1 = reportFeeMonthStatisticsInnerServiceSMOImpl.queryPayFeeDeposit(reportDeposit);
            size = reportDeposits1.size();
        }

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) size / (double) reportDeposit.getRow()), size, newReportDeposits, mp);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        return responseEntity;
    }

    @Override
    public ResponseEntity<String> queryHuaningOweFee(ReportFeeMonthStatisticsDto reportFeeMonthStatisticsDto) {
        int count = reportFeeMonthStatisticsInnerServiceSMOImpl.queryHuaningOweFeeCount(reportFeeMonthStatisticsDto);

        List<ReportFeeMonthStatisticsDto> reportFeeMonthStatisticsDtos = null;
        if (count > 0) {
            reportFeeMonthStatisticsDtos = reportFeeMonthStatisticsInnerServiceSMOImpl.queryHuaningOweFee(reportFeeMonthStatisticsDto);
        } else {
            reportFeeMonthStatisticsDtos = new ArrayList<>();
        }

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) count / (double) reportFeeMonthStatisticsDto.getRow()), count, reportFeeMonthStatisticsDtos);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        return responseEntity;
    }

    @Override
    public ResponseEntity<String> queryHuaningPayFee(Map paramInfo) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, (int) paramInfo.get("year"));
        calendar.set(Calendar.MONTH, (int) paramInfo.get("month") - 1);
        paramInfo.put("yearMonth", DateUtil.getFormatTimeString(calendar.getTime(), "YYYY-MM"));
        calendar.add(Calendar.MONTH, 1);
        paramInfo.put("nextYear", calendar.get(Calendar.YEAR));
        paramInfo.put("nextMonth", calendar.get(Calendar.MONTH) + 1);
        int count = reportFeeMonthStatisticsInnerServiceSMOImpl.queryHuaningPayFeeCount(paramInfo);

        List<Map> reportFeeMonthStatisticsDtos = null;
        if (count > 0) {
            reportFeeMonthStatisticsDtos = reportFeeMonthStatisticsInnerServiceSMOImpl.queryHuaningPayFee(paramInfo);
        } else {
            reportFeeMonthStatisticsDtos = new ArrayList<>();
        }

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) count / (int) paramInfo.get("row")), count, reportFeeMonthStatisticsDtos);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        return responseEntity;
    }

    @Override
    public ResponseEntity<String> queryHuaningPayFeeTwo(Map paramInfo) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, (int) paramInfo.get("year"));
        calendar.set(Calendar.MONTH, (int) paramInfo.get("month") - 1);
        paramInfo.put("yearMonth", DateUtil.getFormatTimeString(calendar.getTime(), "YYYY-MM"));
        calendar.add(Calendar.MONTH, 1);
        paramInfo.put("nextYear", calendar.get(Calendar.YEAR));
        paramInfo.put("nextMonth", calendar.get(Calendar.MONTH) + 1);
        int count = reportFeeMonthStatisticsInnerServiceSMOImpl.queryHuaningPayFeeTwoCount(paramInfo);

        List<Map> reportFeeMonthStatisticsDtos = null;
        if (count > 0) {
            reportFeeMonthStatisticsDtos = reportFeeMonthStatisticsInnerServiceSMOImpl.queryHuaningPayFeeTwo(paramInfo);
        } else {
            reportFeeMonthStatisticsDtos = new ArrayList<>();
        }

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) count / (int) paramInfo.get("row")), count, reportFeeMonthStatisticsDtos);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        return responseEntity;
    }

    @Override
    public ResponseEntity<String> queryHuaningOweFeeDetail(Map paramInfo) {
        Calendar calendar = Calendar.getInstance();
        int count = reportFeeMonthStatisticsInnerServiceSMOImpl.queryHuaningOweFeeDetailCount(paramInfo);
        List<Map> reportFeeMonthStatisticsDtos = null;
        if (count > 0) {
            reportFeeMonthStatisticsDtos = reportFeeMonthStatisticsInnerServiceSMOImpl.queryHuaningOweFeeDetail(paramInfo);
            refreshOweFee(reportFeeMonthStatisticsDtos, paramInfo);
        } else {
            reportFeeMonthStatisticsDtos = new ArrayList<>();
        }

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) count / (int) paramInfo.get("row")), count, reportFeeMonthStatisticsDtos);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        return responseEntity;
    }

    private void refreshOweFee(List<Map> reportFeeMonthStatisticsDtos, Map paramInfo) {
        Date startTime = null;
        Date endTime = null;
        Calendar calendar = Calendar.getInstance();
        int curMonth = calendar.get(Calendar.MONTH) + 1;
        calendar.set(Calendar.MONTH, 0);
        Date curStart = calendar.getTime();


        for (Map paramIn : reportFeeMonthStatisticsDtos) {

            startTime = (Date) paramIn.get("startTime");
            endTime = (Date) paramIn.get("endTime");
            BigDecimal money = (BigDecimal) paramIn.get("oweAmount");
            double month = Math.ceil(computeFeeSMOImpl.dayCompare(startTime, endTime));
            if (month < 1) {
                paramIn.put("btAmount", 0);
                paramIn.put("bfAmount", 0);
                continue;
            }

            //????????????
            BigDecimal monthAmount = money.divide(new BigDecimal(month), 2, BigDecimal.ROUND_HALF_EVEN);

            if (startTime.getTime() < curStart.getTime()) {
                BigDecimal btAmountDec = monthAmount.multiply(new BigDecimal(curMonth)).setScale(2, BigDecimal.ROUND_HALF_EVEN);
                paramIn.put("btAmount", btAmountDec.doubleValue());
                double preMonth = Math.ceil(computeFeeSMOImpl.dayCompare(startTime, curStart));
                BigDecimal bfAmountDec = monthAmount.multiply(new BigDecimal(preMonth)).setScale(2, BigDecimal.ROUND_HALF_EVEN);
                paramIn.put("bfAmount", bfAmountDec.doubleValue());
                continue;
            }

            if (startTime.getTime() >= curStart.getTime()) {
                paramIn.put("btAmount", money.doubleValue());
                paramIn.put("bfAmount", 0);
            }
        }

    }

    @Override
    public ResponseEntity<String> queryPrePayment(ReportFeeMonthStatisticsDto reportFeeMonthStatisticsDto) {

        int count = reportFeeMonthStatisticsInnerServiceSMOImpl.queryPrePaymentNewCount(reportFeeMonthStatisticsDto);

        List<ReportFeeMonthStatisticsDto> reportFeeMonthStatisticsDtos = null;
        if (count > 0) {
            reportFeeMonthStatisticsDtos = reportFeeMonthStatisticsInnerServiceSMOImpl.queryPrePayment(reportFeeMonthStatisticsDto);
            for (ReportFeeMonthStatisticsDto tmpReportFeeMonthStatisticsDto : reportFeeMonthStatisticsDtos) {
                if (FeeDto.PAYER_OBJ_TYPE_ROOM.equals(tmpReportFeeMonthStatisticsDto.getPayerObjType())) {
                    tmpReportFeeMonthStatisticsDto.setObjName(tmpReportFeeMonthStatisticsDto.getFloorNum()
                            + "???" + tmpReportFeeMonthStatisticsDto.getUnitNum()
                            + "??????" + tmpReportFeeMonthStatisticsDto.getRoomNum() + "???");
                } else {
                    tmpReportFeeMonthStatisticsDto.setObjName(tmpReportFeeMonthStatisticsDto.getCarNum());
                }

                try {
                    tmpReportFeeMonthStatisticsDto.setOweDay(DateUtil.daysBetween(DateUtil.getDateFromString(tmpReportFeeMonthStatisticsDto.getEndTime(), DateUtil.DATE_FORMATE_STRING_A), DateUtil.getCurrentDate()));
                } catch (ParseException e) {
                    logger.error("????????????????????????" + tmpReportFeeMonthStatisticsDto.getEndTime(), e);
                }
            }
        } else {
            reportFeeMonthStatisticsDtos = new ArrayList<>();
        }

        ResultVo resultVo = new ResultVo((int) Math.ceil((double) count / (double) reportFeeMonthStatisticsDto.getRow()), count, reportFeeMonthStatisticsDtos);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(resultVo.toString(), HttpStatus.OK);

        return responseEntity;
    }

    private void freshReportOweDay(List<ReportFeeMonthStatisticsDto> reportFeeMonthStatisticsDtos) {
        for (ReportFeeMonthStatisticsDto reportFeeMonthStatisticsDto : reportFeeMonthStatisticsDtos) {
            try {
                int day = DateUtil.daysBetween(DateUtil.getDateFromString(reportFeeMonthStatisticsDto.getDeadlineTime(), DateUtil.DATE_FORMATE_STRING_A), DateUtil.getDateFromString(reportFeeMonthStatisticsDto.getFeeCreateTime(),
                        DateUtil.DATE_FORMATE_STRING_A));
                reportFeeMonthStatisticsDto.setOweDay(day);
            } catch (Exception e) {
                logger.error("????????????????????????", e);
            }

        }
    }

    private void freshReportDeadlineDay(List<ReportFeeMonthStatisticsDto> reportFeeMonthStatisticsDtos) {

        Date nowDate = DateUtil.getCurrentDate();

        for (ReportFeeMonthStatisticsDto reportFeeMonthStatisticsDto : reportFeeMonthStatisticsDtos) {
            try {
                int day = DateUtil.daysBetween(DateUtil.getDateFromString(reportFeeMonthStatisticsDto.getDeadlineTime(),
                        DateUtil.DATE_FORMATE_STRING_A), nowDate);
                reportFeeMonthStatisticsDto.setOweDay(day);
            } catch (Exception e) {
                logger.error("????????????????????????", e);
            }

        }
    }


}
