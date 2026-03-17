package com.tyt.behaviorexample

import org.junit.Test

/**
 * @author hezd
 * @date 2023/9/26 14:18
 * @description
 */
class Test {

    @Test
    fun testGroupBy() {
        val strList = listOf(
            "a111",
            "a222",
            "a333",
            "b111",
            "b222",
            "b333",
        )
        val strGroup = strList.groupBy {
            it.startsWith("a")
        }
        val prefixA = strGroup[true] ?: emptyList()
        val prefixNonA = strGroup[false] ?: emptyList()
        println("prefixA list:")
        prefixA.forEach {
            println(it)
        }
        println("prefixNonA list:")
        prefixNonA.forEach {
            println(it)
        }
    }
}