package com.hzdq.nppvdoctorclient.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * Time:2023/3/16
 * Author:Sinory
 * Description:
 */
public class PinYinUtils {
    /**
     * 得到指定汉字的拼音
     * 注意:不应该被频繁调用，它消耗一定内存
     * @param hanzi
     * @return
     */
    public  String getPinYin(String hanzi){
        String pinyin = "";

        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();//控制转换是否大小写，是否带音标
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);//大写是UPPERCASE;小写是LOWERCASE
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        //由于不能直接对多个汉字转换，只能对单个汉字转换
        char[] arr = hanzi.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            if(Character.isWhitespace(arr[i]))continue;//如果是空格，则不处理，进行下次遍历

            //汉字是2个字节存储，肯定大于127，所以大于127就可以当为汉字转换
            if(arr[i]>127){
                try {
                    //由于多音字的存在，单 dan shan
                    String[] pinyinArr = PinyinHelper.toHanyuPinyinStringArray(arr[i], format);

                    if(pinyinArr!=null){
                        pinyin += pinyinArr[0];
                    }else {
                        pinyin += arr[i];
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                    //不是正确的汉字
                    pinyin += arr[i];
                }
            }else {
                //不是汉字，
                pinyin += arr[i];
            }
        }
        if (EnglishUtil.INSTANCE.isEnglishAlphabet(pinyin.substring(0, 1))){
            return pinyin;
        }else {
            return "#"+pinyin;
        }

    }
}
