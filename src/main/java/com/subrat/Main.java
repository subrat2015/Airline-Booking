package com.subrat;

import com.subrat.checkin.dao.SeatsRepository;
import com.subrat.checkin.db.PoolManager;
import com.subrat.checkin.processor.ConcurrentCheckInProcessor;
import com.subrat.checkin.processor.ManualCheckInProcessor;
import com.subrat.checkin.processor.Processor;

public class Main {
    private static ThreadLocal<PoolManager> poolManagerThreadLocal = new ThreadLocal<>();

    public static void main(String[] args) {

        init();
        /*if (args.length > 0 && args[0].equals("manual")) {
            manualCheckIn();
        } else if (args.length > 0 && args[0].equals("concurrent")){
            concurrentCheckIn();
        } else {
            System.out.println("Please provide a valid argument: manual or concurrent");
        }*/
        concurrentCheckIn();
    }

    private static void concurrentCheckIn ()
    {
        System.out.println("Starting concurrent check-in process");
        SeatsRepository.clearAllSeats();
        Processor concurrentProcessor = new ConcurrentCheckInProcessor();
        concurrentProcessor.process();
        System.out.println("Concurrent check-in process completed");
    }

    private static void manualCheckIn ()
    {
        System.out.println("Starting manual check-in process");
        SeatsRepository.clearAllSeats();
        System.out.println("All seats cleared");
        Processor manualProcessor = new ManualCheckInProcessor();
        manualProcessor.process();
        System.out.println("Manual check-in process completed");
    }

    private static void init() {
        System.out.println("Initializing application");
        poolManagerThreadLocal.set(PoolManager.getInstance());
        System.out.println("Application initialized");
    }

    public static PoolManager getPoolManager() {
        return poolManagerThreadLocal.get();
    }
}
