package com.zhj.blog.Utils;

/**
 * @author:zouhuajun
 * @date:2022/1/12 0:38
 **/
public class Convert {
    public static String EmoConvert(String str){
        String RexString = "(\\[)em_([0-9]*)(\\])";
        return str.replaceAll(RexString,"<img src='/lib/qqFace/arclist/$2.gif' border='0' />");
    }

    public static String ContentConvert(String str){
        String Rex = "\\n|\\r\\n";
        return str.replaceAll(Rex, "<br>");
    }

}

