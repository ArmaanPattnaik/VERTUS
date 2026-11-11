package com.example.model;

public class Process {

    private final String pid;
    private final int arrivalTime;
    private final int burstTime;
    private final int priority;

    private int completionTime;
    private int turnaroundTime;
    private int waitingTime;

    public Process(String pid, int arrivalTime, int burstTime, int priority) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;

        this.completionTime = 0;
        this.turnaroundTime = 0;
        this.waitingTime = 0;
    }

    public String getPid() {
        return pid;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getPriority() {
        return priority;
    }

    public int getCompletionTime() {
        return completionTime;
    }

    public int getTurnaroundTime() {
        return turnaroundTime;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void setCompletionTime(int completionTime) {
        this.completionTime = completionTime;
        this.turnaroundTime = completionTime - arrivalTime;
        this.waitingTime = turnaroundTime - burstTime;
    }

    public Process copy() {
        Process copy = new Process(
                pid,
                arrivalTime,
                burstTime,
                priority
        );

        copy.completionTime = completionTime;
        copy.turnaroundTime = turnaroundTime;
        copy.waitingTime = waitingTime;

        return copy;
    }
}