package com.java110.code.newBack;

import com.alibaba.fastjson.JSONObject;
import com.java110.utils.util.DateUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class BaseGenerator {

//    protected FileUtilBase fileUtilBase(){
//        return new FileUtilBase();
//    }
    //首字母转小写
    public static String toLowerCaseFirstOne(String s){
        if(Character.isLowerCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
    }


    //首字母转大写
    public static String toUpperCaseFirstOne(String s){
        if(Character.isUpperCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
    }


    /**
     * 读入TXT文件
     */
    public static StringBuffer readFile(String pathname) {
        StringBuffer sb = new StringBuffer();
        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line ="";
            //网友推荐更加简洁的写法
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                sb.append(line+"\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb;
    }

    /**
     * 写入TXT文件
     */
    public static void writeFile(String filePath,String fileName) {
        try {
            File writeName = new File(filePath); // 相对路径，如果没有则要建立一个新的output.txt文件
            File fileParent = writeName.getParentFile();
            if(!fileParent.exists()){
                fileParent.mkdirs();
            }
            writeName.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
            try (FileWriter writer = new FileWriter(writeName);
                 BufferedWriter out = new BufferedWriter(writer)
            ) {
                out.write(fileName);
                out.flush(); // 把缓存区内容压入文件
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String replaceBindingTemplateContext(String srcStr, JSONObject data){
        return srcStr.replace("TableCode@@",toUpperCaseFirstOne(data.getString("tableName")))
                .replace("@@tableCode@@",data.getString("tableName"))
                .replace("@@templateName@@", data.getString("templateName"))
                .replace("@@templateCode@@", data.getString("templateCode"))
                .replace("@@TemplateCode@@", toUpperCaseFirstOne(data.getString("templateCode")))
                .replace("@@templateKey@@", data.getString("templateKey"))
                .replace("@@TemplateKey@@", toUpperCaseFirstOne(data.getString("templateKey")))
                .replace("@@templateKeyName@@", data.getString("templateKeyName"))
                .replace("@@TEMPLATECODE@@", data.getString("templateCode").toUpperCase());
    }

    protected String replaceTemplateContext(String srcStr, JSONObject data){
        return srcStr.replace("@@templateName@@", data.getString("templateName"))
                .replace("@@templateCode@@", data.getString("templateCode"))
                .replace("@@TemplateCode@@", toUpperCaseFirstOne(data.getString("templateCode")))
                .replace("@@templateKey@@", data.getString("templateKey"))
                .replace("@@TemplateKey@@", toUpperCaseFirstOne(data.getString("templateKey")))
                .replace("@@templateKeyName@@", data.getString("templateKeyName"))
                .replace("@@TEMPLATECODE@@", data.getString("templateCode").toUpperCase())
                .replace("@@searchCode@@", data.getString("searchCode"))
                .replace("@@directories@@", data.getString("directories"))
                .replace("@@searchName@@", data.getString("searchName"));
    }

    protected String replaceTemplateContext(String srcStr, Data data){
        return srcStr.replace("@@templateName@@", data.getDesc())
                .replace("@@templateCode@@", data.getName())
                .replace("@@TemplateCode@@", toUpperCaseFirstOne(data.getName()))
                .replace("@@templateKey@@", data.getId())
                .replace("@@TemplateKey@@", toUpperCaseFirstOne(data.getId()))
                .replace("@@templateKeyName@@", data.getId())
                .replace("@@TEMPLATECODE@@", data.getName().toUpperCase())
                .replace("@@searchCode@@", data.getId())
                .replace("@@shareName@@", data.getShareName())
                .replace("@@searchName@@", data.getId())
                .replace("@@date@@", DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
    }
}
