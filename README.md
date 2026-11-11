# VERTUS
### CPU Scheduling Simulator

A JavaFX-based desktop application that demonstrates and visualizes fundamental CPU scheduling algorithms used in Operating Systems. The simulator allows users to explore process scheduling behavior through algorithm execution and interactive Gantt chart visualization.

## Overview

CPU scheduling is a core function of an operating system that determines the order in which processes are executed by the CPU.

This project implements five major CPU scheduling algorithms and provides a visual representation of their execution using Gantt charts.

## Features

- Implementation of five CPU scheduling algorithms.
- Graphical desktop interface built with JavaFX.
- Gantt chart visualization for process execution.
- Process scheduling based on arrival time, burst time, and priority.
- Visual comparison of different scheduling strategies.

## Scheduling Algorithms

| Algorithm | Description |
|---|---|
| FCFS | First Come, First Served |
| SJF | Shortest Job First |
| SRJF | Shortest Remaining Job First |
| Round Robin | Time Quantum-based scheduling |
| Priority | Execution based on process priority |

### Algorithm Details

**1. First Come, First Served (FCFS)**

Executes processes in the order of their arrival. It follows a non-preemptive scheduling approach.

**2. Shortest Job First (SJF)**

Selects the available process with the shortest CPU burst time. It is non-preemptive.

**3. Shortest Remaining Job First (SRJF)**

A preemptive scheduling algorithm that executes the process with the shortest remaining burst time.

**4. Round Robin (RR)**

Allocates CPU time to each process using a fixed time quantum, ensuring fair CPU allocation.

**5. Priority Scheduling**

Selects processes based on their assigned priority levels.

## Gantt Chart Visualization

The simulator uses Gantt charts to represent the execution sequence of processes over time.

The visualization helps users understand:

- Process execution order.
- CPU allocation intervals.
- Scheduling and preemption behavior.
- Time-sharing between processes.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java | Core application logic |
| JavaFX | Graphical User Interface |
| Maven | Project and dependency management |

## Project Structure

```text
cpu-scheduling-simulator/
│
├── pom.xml
│
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── example/
        │           └── ...
        │
        └── resources/
            └── ...
```

*The structure above is illustrative; adapt it to the actual source layout.*

## Getting Started

### Prerequisites

- Java Development Kit (JDK)
- Maven
- JavaFX dependencies configured in the project

### Installation

1. Clone the repository:

```bash
git clone https://github.com/ArmaanPattnaik/ VERTUS.git
```

2. Navigate to the project directory:

```bash
cd VERTUS
```

3. Open the project in your preferred Java IDE.

4. Resolve Maven dependencies and run the JavaFX application using the configured Maven plugin or IDE run configuration.

## Learning Objectives

- Understand CPU scheduling concepts.
- Explore preemptive and non-preemptive scheduling.
- Study process execution and CPU allocation.
- Understand how scheduling algorithms affect execution order.
- Visualize scheduling behavior through Gantt charts.

## Project Information

**Category:** Operating Systems / Desktop Application

**Language:** Java

**GUI Framework:** JavaFX

**Project Type:** CPU Scheduling Algorithm Simulator

## Author

**Armaan Pattnaik**

Computer Science and Engineering Student

GitHub: [@ArmaanPattnaik](https://github.com/ArmaanPattnaik)

---

*Built to explore Operating System scheduling concepts through Java programming and visual simulation.*
