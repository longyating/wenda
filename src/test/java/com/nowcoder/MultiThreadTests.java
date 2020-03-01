package com.nowcoder;

import ch.qos.logback.core.encoder.EchoEncoder;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


import static java.lang.Thread.sleep;

class MyThread extends Thread{
    private int tid;

    public MyThread(int tid){
        this.tid = tid;
    }

    @Override
    public void run(){
        try{
            for(int i=0 ;i < 10 ;i++){
                //sleep(1000);
                System.out.println(String.format("%d:%d",tid,i));
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}

class Producer implements Runnable{
    private BlockingQueue<String> q;
    public Producer(BlockingQueue<String> q){
        this.q = q;
    }
    @Override
    public void run() {
        try{
            for(int i =0; i< 100; i++){
                q.put(String.valueOf(i));
                Thread.sleep(10);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

class Consumer implements Runnable{
    private BlockingQueue<String> q;
    public Consumer(BlockingQueue<String> q){
        this.q = q;
    }
    @Override
    public void run() {
        try{
            while(true){
                System.out.println(Thread.currentThread().getName() + ":" + q.take());
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}


public class MultiThreadTests {
    public static void testThread(){
//        //创建线程方式一
//        for (int j = 0; j<5; j++){
//            MyThread mt = new MyThread(j);
//            mt.start();
//        }

        //创建线程方式二
        for(int j =0 ;j <5; j++){
            int finalJ = j;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        for(int i=0 ;i < 10 ;i++){
                            Thread.sleep(1000);
                            System.out.println(String.format("T2 %d :%d", finalJ, i));
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

            }).start();
        }
    }

    private static Object obj = new Object();

    public static void testSynchronized1(){
        synchronized (obj){
            try{
                for(int k=0 ;k < 10 ;k++){
                    Thread.sleep(1000);
                    System.out.println(String.format("T3 %d",k));}
            }catch(Exception e){
                e.printStackTrace();
                }
        }
    }
    public static void testSynchronized2(){
        synchronized (obj){
            try{
                for(int k=0; k < 10; k++){
                    Thread.sleep(1000);
                    System.out.println(String.format("T4 %d",k));}
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void testSynchronized(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                testSynchronized1();
                testSynchronized2();  //1 2 是同一个 obj,只有等1用完了才能给2 用
            }
        }).start();
    }

    public static void testBlockingQueue(){
        BlockingQueue<String> q = new ArrayBlockingQueue<String>(10);
        new Thread(new Producer(q)).start();
        new Thread(new Consumer(q), "consumer 1").start();
        new Thread(new Consumer(q), "consumer 2").start();
    }

    private static ThreadLocal<Integer> threadLocalUserIds = new ThreadLocal<>();
    private static int userId; //谁都可以访问，谁最后写的，就是谁的值

    public static void testThreadLocal(){
        for(int i = 0; i<10; i++){
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        threadLocalUserIds.set(finalI);
                        Thread.sleep(1000);
                        System.out.println(threadLocalUserIds.get());

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        for (int i = 0; i < 10; ++i) {
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        userId = finalI;
                        Thread.sleep(1000);
                        System.out.println("UserId:" + userId);  //打印出来全是9，因为最后一个线程将该设置为9.
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public static void testExecutor(){
        ExecutorService service = Executors.newFixedThreadPool(2);
        service.submit(new Runnable() {
            @Override
            public void run() {
                for(int i=0; i< 10; i++){
                    try{
                        System.out.println("Executor 1:" + i);
                        Thread.sleep(1000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        service.submit(new Runnable() {
            @Override
            public void run() {
                for(int i=0; i< 10; i++){
                    try{
                        System.out.println("Executor 2:" + i);
                        Thread.sleep(1000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        service.shutdown();
        while(!service.isTerminated()){
            try{
                System.out.println("waiting");
                Thread.sleep(1000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private static int count =0;
    private static AtomicInteger atomicInteger = new AtomicInteger();

    public static void testWithoutAtomic(){
        for (int i = 0; i<10; i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Thread.sleep(1000);
                        for(int j =0; j<10; j++){
                            count ++;
                            System.out.println(count);
                        }

                    }catch(Exception e){
                        e.printStackTrace();
                    }

                }
            }).start();
        }
    }

    public static void testWithAtomic(){
        for (int i = 0; i<10; i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Thread.sleep(1000);
                        for(int j =0; j<10; j++){
                            System.out.println(atomicInteger.incrementAndGet());

                        }

                    }catch(Exception e){
                        e.printStackTrace();
                    }

                }
            }).start();
        }
    }

    public static void testFuture(){
        ExecutorService service = Executors.newSingleThreadExecutor();
        Future<Integer> future = service.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Thread.sleep(1000);
                return 1;
                //throw  new IllegalArgumentException("hah");
            }
        });
        service.shutdown();
        try{
            System.out.println(future.get());
        }catch(Exception e){
            e.printStackTrace();
        }



        }


    public static void main(String[] argv){
        //testThread();
        //testSynchronized();
        //testBlockingQueue();
        //testThreadLocal();
        //testExecutor();
        //testWithoutAtomic();
        //testWithAtomic();   //原子性
        testFuture();




    }
}
