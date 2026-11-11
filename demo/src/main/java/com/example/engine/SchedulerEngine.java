package com.example.engine;

import com.example.model.GanttRecord;
import com.example.model.Process;

import java.util.*;

public class SchedulerEngine {

    private SchedulerEngine() {
    }

    // =========================================================
    // FCFS
    // =========================================================

    public static List<GanttRecord> simulateFCFS(List<Process> input) {

        List<Process> processes = copyProcesses(input);

        processes.sort(
                Comparator
                        .comparingInt(Process::getArrivalTime)
                        .thenComparing(Process::getPid)
        );

        List<GanttRecord> timeline = new ArrayList<>();

        int currentTime = 0;

        for (Process process : processes) {

            if (currentTime < process.getArrivalTime()) {
                timeline.add(
                        new GanttRecord(
                                "Idle",
                                currentTime,
                                process.getArrivalTime()
                        )
                );

                currentTime = process.getArrivalTime();
            }

            int start = currentTime;

            currentTime += process.getBurstTime();

            process.setCompletionTime(currentTime);

            timeline.add(
                    new GanttRecord(
                            process.getPid(),
                            start,
                            currentTime
                    )
            );
        }

        copyResults(processes, input);

        return timeline;
    }

    // =========================================================
    // SJF - Non Preemptive
    // =========================================================

    public static List<GanttRecord> simulateSJF(List<Process> input) {

        List<Process> processes = copyProcesses(input);

        List<GanttRecord> timeline = new ArrayList<>();

        Set<String> completed = new HashSet<>();

        int currentTime = 0;
        int completedCount = 0;

        while (completedCount < processes.size()) {

            Process selected = null;

            for (Process process : processes) {

                if (completed.contains(process.getPid())) {
                    continue;
                }

                if (process.getArrivalTime() <= currentTime) {

                    if (selected == null
                            || process.getBurstTime() < selected.getBurstTime()
                            || (
                            process.getBurstTime() == selected.getBurstTime()
                                    && process.getArrivalTime()
                                    < selected.getArrivalTime()
                    )) {
                        selected = process;
                    }
                }
            }

            // No process currently available
            if (selected == null) {

                int nextArrival = Integer.MAX_VALUE;

                for (Process process : processes) {
                    if (!completed.contains(process.getPid())) {
                        nextArrival = Math.min(
                                nextArrival,
                                process.getArrivalTime()
                        );
                    }
                }

                timeline.add(
                        new GanttRecord(
                                "Idle",
                                currentTime,
                                nextArrival
                        )
                );

                currentTime = nextArrival;

                continue;
            }

            int start = currentTime;

            currentTime += selected.getBurstTime();

            selected.setCompletionTime(currentTime);

            completed.add(selected.getPid());
            completedCount++;

            timeline.add(
                    new GanttRecord(
                            selected.getPid(),
                            start,
                            currentTime
                    )
            );
        }

        copyResults(processes, input);

        return timeline;
    }

    // =========================================================
    // SRTF - Preemptive SJF
    // =========================================================

    public static List<GanttRecord> simulateSRTF(List<Process> input) {

        List<Process> processes = copyProcesses(input);

        Map<String, Integer> remaining = new HashMap<>();

        for (Process process : processes) {
            remaining.put(
                    process.getPid(),
                    process.getBurstTime()
            );
        }

        List<GanttRecord> timeline = new ArrayList<>();

        int currentTime = 0;
        int completed = 0;

        while (completed < processes.size()) {

            Process selected = null;

            for (Process process : processes) {

                int remainingTime = remaining.get(process.getPid());

                if (process.getArrivalTime() <= currentTime
                        && remainingTime > 0) {

                    if (selected == null
                            || remainingTime
                            < remaining.get(selected.getPid())
                            || (
                            remainingTime
                                    == remaining.get(selected.getPid())
                                    && process.getArrivalTime()
                                    < selected.getArrivalTime()
                    )) {
                        selected = process;
                    }
                }
            }

            // CPU idle
            if (selected == null) {

                int nextArrival = Integer.MAX_VALUE;

                for (Process process : processes) {

                    int remainingTime =
                            remaining.get(process.getPid());

                    if (remainingTime > 0) {
                        nextArrival = Math.min(
                                nextArrival,
                                process.getArrivalTime()
                        );
                    }
                }

                addOrMerge(
                        timeline,
                        "Idle",
                        currentTime,
                        nextArrival
                );

                currentTime = nextArrival;

                continue;
            }

            int start = currentTime;

            // Run exactly one time unit
            currentTime++;

            int newRemaining =
                    remaining.get(selected.getPid()) - 1;

            remaining.put(
                    selected.getPid(),
                    newRemaining
            );

            if (newRemaining == 0) {

                selected.setCompletionTime(currentTime);
                completed++;
            }

            addOrMerge(
                    timeline,
                    selected.getPid(),
                    start,
                    currentTime
            );
        }

        copyResults(processes, input);

        return timeline;
    }

    // =========================================================
    // Round Robin
    // =========================================================

    public static List<GanttRecord> simulateRoundRobin(
            List<Process> input,
            int quantum
    ) {

        if (quantum <= 0) {
            throw new IllegalArgumentException(
                    "Time quantum must be greater than zero."
            );
        }

        List<Process> processes = copyProcesses(input);

        processes.sort(
                Comparator
                        .comparingInt(Process::getArrivalTime)
                        .thenComparing(Process::getPid)
        );

        Map<String, Integer> remaining = new HashMap<>();

        for (Process process : processes) {
            remaining.put(
                    process.getPid(),
                    process.getBurstTime()
            );
        }

        Queue<Process> readyQueue = new LinkedList<>();

        List<GanttRecord> timeline = new ArrayList<>();

        int currentTime = 0;
        int nextProcess = 0;
        int completed = 0;

        while (completed < processes.size()) {

            // Add processes that have arrived
            while (nextProcess < processes.size()
                    && processes.get(nextProcess).getArrivalTime()
                    <= currentTime) {

                readyQueue.offer(processes.get(nextProcess));
                nextProcess++;
            }

            // Nothing ready
            if (readyQueue.isEmpty()) {

                if (nextProcess < processes.size()) {

                    int nextArrival =
                            processes.get(nextProcess).getArrivalTime();

                    addOrMerge(
                            timeline,
                            "Idle",
                            currentTime,
                            nextArrival
                    );

                    currentTime = nextArrival;

                    continue;
                }
            }

            Process current = readyQueue.poll();

            int remainingTime =
                    remaining.get(current.getPid());

            int executionTime =
                    Math.min(quantum, remainingTime);

            int start = currentTime;

            currentTime += executionTime;

            remainingTime -= executionTime;

            remaining.put(
                    current.getPid(),
                    remainingTime
            );

            addOrMerge(
                    timeline,
                    current.getPid(),
                    start,
                    currentTime
            );

            // Add newly arrived processes
            while (nextProcess < processes.size()
                    && processes.get(nextProcess).getArrivalTime()
                    <= currentTime) {

                readyQueue.offer(processes.get(nextProcess));
                nextProcess++;
            }

            if (remainingTime > 0) {

                readyQueue.offer(current);

            } else {

                current.setCompletionTime(currentTime);
                completed++;
            }
        }

        copyResults(processes, input);

        return timeline;
    }

    // =========================================================
    // Priority Scheduling - Non Preemptive
    // Lower number = Higher priority
    // =========================================================

    public static List<GanttRecord> simulatePriority(
            List<Process> input
    ) {

        List<Process> processes = copyProcesses(input);

        List<GanttRecord> timeline = new ArrayList<>();

        Set<String> completed = new HashSet<>();

        int currentTime = 0;
        int completedCount = 0;

        while (completedCount < processes.size()) {

            Process selected = null;

            for (Process process : processes) {

                if (completed.contains(process.getPid())) {
                    continue;
                }

                if (process.getArrivalTime() <= currentTime) {

                    if (selected == null
                            || process.getPriority()
                            < selected.getPriority()
                            || (
                            process.getPriority()
                                    == selected.getPriority()
                                    && process.getArrivalTime()
                                    < selected.getArrivalTime()
                    )) {

                        selected = process;
                    }
                }
            }

            if (selected == null) {

                int nextArrival = Integer.MAX_VALUE;

                for (Process process : processes) {

                    if (!completed.contains(process.getPid())) {

                        nextArrival = Math.min(
                                nextArrival,
                                process.getArrivalTime()
                        );
                    }
                }

                timeline.add(
                        new GanttRecord(
                                "Idle",
                                currentTime,
                                nextArrival
                        )
                );

                currentTime = nextArrival;

                continue;
            }

            int start = currentTime;

            currentTime += selected.getBurstTime();

            selected.setCompletionTime(currentTime);

            completed.add(selected.getPid());
            completedCount++;

            timeline.add(
                    new GanttRecord(
                            selected.getPid(),
                            start,
                            currentTime
                    )
            );
        }

        copyResults(processes, input);

        return timeline;
    }

    // =========================================================
    // Helper Methods
    // =========================================================

    private static List<Process> copyProcesses(
            List<Process> input
    ) {

        List<Process> copies = new ArrayList<>();

        for (Process process : input) {
            copies.add(process.copy());
        }

        return copies;
    }

    private static void copyResults(
            List<Process> calculated,
            List<Process> original
    ) {

        Map<String, Process> calculatedMap = new HashMap<>();

        for (Process process : calculated) {
            calculatedMap.put(process.getPid(), process);
        }

        for (int i = 0; i < original.size(); i++) {

            Process originalProcess = original.get(i);

            Process calculatedProcess =
                    calculatedMap.get(originalProcess.getPid());

            if (calculatedProcess != null) {

                originalProcess.setCompletionTime(
                        calculatedProcess.getCompletionTime()
                );
            }
        }
    }

    private static void addOrMerge(
            List<GanttRecord> timeline,
            String pid,
            int start,
            int end
    ) {

        if (start == end) {
            return;
        }

        if (!timeline.isEmpty()) {

            GanttRecord last =
                    timeline.get(timeline.size() - 1);

            if (last.getPid().equals(pid)
                    && last.getEndTime() == start) {

                timeline.set(
                        timeline.size() - 1,
                        new GanttRecord(
                                pid,
                                last.getStartTime(),
                                end
                        )
                );

                return;
            }
        }

        timeline.add(
                new GanttRecord(
                        pid,
                        start,
                        end
                )
        );
    }
}