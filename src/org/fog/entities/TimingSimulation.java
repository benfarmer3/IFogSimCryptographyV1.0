package org.fog.entities;

//Class to measure the simulation timings
public class TimingSimulation {

    private  static long startTime;
    private  static  long stopTimer;

    //Method to start the timer

    public static void startTimer(){
        startTime = System.currentTimeMillis();
    }

    //Method to stop the timer
    public static void stopTimer(){
        stopTimer = System.currentTimeMillis();
    }

    //Method to print the simulation timing
    public static void printTimes(){
        System.out.println("Total time taken : " + (stopTimer - startTime));

    }

}
