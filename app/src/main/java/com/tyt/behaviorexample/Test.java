package com.tyt.behaviorexample;

import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Set;

/**
 * @author hezd
 * @date 2023/6/9 09:54
 * @description
 */
public class Test {
    public static int test(int i){
        synchronized (Test.class){

        }
        int j = 0;
        int k = 0;
        return i+j+k;
    }

    public static Set<SoftReference<Test>> cache = new HashSet<>();
    public static void main(String[] args) {
    }
}
