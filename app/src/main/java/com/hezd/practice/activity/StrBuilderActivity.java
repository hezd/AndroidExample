package com.hezd.practice.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hezd.practice.R;

import java.util.Random;

/**
 * @author hezd
 * @date 2023/7/25 09:44
 * @description
 */
public class StrBuilderActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_str_builder);
        try {
            strBuilderTest();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void strBuilderTest() throws InterruptedException {
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random(1000);
        Runnable task = () -> {
            for(int i = 0;;i++){
                if(i%10==0){
                    stringBuilder.setLength(0);
                }
                stringBuilder.append(String.valueOf(random.nextInt()%100).concat("\n"));
                System.out.println(Thread.currentThread().getName()+":"+stringBuilder);
            }
        };
        Runnable task2 = () -> {
            for(int i = 0;;i++){
                if(i%10==0){
                    stringBuilder.setLength(0);
                }
                stringBuilder.append(String.valueOf(random.nextInt()%1000).concat("\n"));
                System.out.println(Thread.currentThread().getName()+":"+stringBuilder);
            }
        };
        Runnable task3 = () -> {

            for(int i = 0;;i++){
                if(i%10==0){
                    stringBuilder.setLength(0);
                }
                stringBuilder.append(String.valueOf(random.nextInt()%10000).concat("\n"));
                System.out.println(Thread.currentThread().getName()+":"+stringBuilder);
            }
        };
        Runnable task4 = () -> {

            for(int i = 0;;i++){
                if(i%10==0){
                    stringBuilder.setLength(0);
                }
                stringBuilder.append(String.valueOf(random.nextInt()).concat("\n"));
                System.out.println(Thread.currentThread().getName()+":"+stringBuilder);
            }
        };
        Runnable task5 = () -> {

            for(int i = 0;;i++){
                if(i%10==0){
                    stringBuilder.setLength(0);
                }
                stringBuilder.append(String.valueOf(i+(i*10000)).concat("\n"));
                System.out.println(Thread.currentThread().getName()+":"+stringBuilder);
            }
        };
        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task2);
        Thread thread3 = new Thread(task3);
        Thread thread4 = new Thread(task4);
        Thread thread5 = new Thread(task5);
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        thread5.start();
//        Thread.sleep(1000L);
    }
}
