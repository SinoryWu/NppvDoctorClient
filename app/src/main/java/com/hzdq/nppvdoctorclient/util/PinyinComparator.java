package com.hzdq.nppvdoctorclient.util;

import com.hzdq.nppvdoctorclient.dataclass.DoctorList;

import java.util.Comparator;

/**
 * Time:2023/3/27
 * Author:Sinory
 * Description:
 */
public class PinyinComparator implements Comparator<DoctorList> {
    //先根据首字母判断，首字母为“#”都放在最后，都为“#”或者都是字母时才根据拼音来比较排序
    public int compare(DoctorList o1, DoctorList o2) {
        if (o1.getPinyin().startsWith("#")
                && !o2.getPinyin().startsWith("#")) {
            return 1;
        } else if (!o1.getPinyin().startsWith("#")
                && o2.getPinyin().startsWith("#")) {
            return -1;
        } else {
            return o1.getPinyin().compareToIgnoreCase(o2.getPinyin());
        }
    }

}

