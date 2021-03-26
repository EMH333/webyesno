package com.ethohampton.yesno

import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random

object Counter {
    var yes: AtomicLong = AtomicLong()
    var no: AtomicLong = AtomicLong()
    var total: AtomicLong = AtomicLong()

    private val bloom: BloomFilter = BloomFilter(50000)

    fun getResult():String{
        var t = total.get()
        var y = yes.get()
        var n = no.get()
        if(t <= 10){//deal with small values
            t = 10
            y = Random.Default.nextLong(t)
            n = t - y
            //println("${t},${y},${n}")
        }
        val ans = Random.Default.nextLong(t)
        return if( ans <= y){
            "YES;${y};${t}"
        }else{
            "NO;${n};${t}"
        }
    }
    fun addYes(rawID: ByteArray){
        val id = rawID.contentToString()
        if(bloom.contains(id)){
            return
        }
        yes.incrementAndGet()
        total.incrementAndGet()
        bloom.add(id)
    }
    fun addNo(rawID: ByteArray){
        val id = rawID.contentToString()
        if(bloom.contains(id)){
            return
        }
        no.incrementAndGet()
        total.incrementAndGet()
        bloom.add(id)
    }
}