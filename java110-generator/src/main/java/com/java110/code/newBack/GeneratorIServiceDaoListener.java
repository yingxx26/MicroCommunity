package com.java110.code.newBack;

import com.java110.code.util.FileUtilBase;
import com.java110.utils.util.DateUtil;

public class GeneratorIServiceDaoListener extends BaseGenerator {





    /**
     * 生成代码
     * @param data
     */
    public void generator(Data data) throws Exception {
        StringBuffer sb = readFile(this.getClass().getResource("/newTemplate/IServiceDao.txt").getFile());
        String fileContext = sb.toString();
        fileContext = fileContext.replace("store",toLowerCaseFirstOne(data.getName()))
                .replace("@@shareName@@",data.getShareName())
                .replace("Store",toUpperCaseFirstOne(data.getName()))
                .replace("商户",data.getDesc())
                .replace("@@date@@", DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));

        String writePath = this.getClass().getResource("/").getPath()
                + "out/back/dao/I"+toUpperCaseFirstOne(data.getName())+"V1ServiceDao.java";
        writeFile(writePath,
                fileContext);
        //复制生成的文件到对应分区目录下
        if (data.isAutoMove()) {
            FileUtilBase.copyfile(writePath, "service-" + data.getShareName()  + "\\src\\main\\java\\com\\java110\\"+data.getShareName()+"\\dao\\" + "I" + toUpperCaseFirstOne(data.getName()) + "V1ServiceDao.java");
        }
    }
}
