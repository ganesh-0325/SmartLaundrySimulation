Smart Laundry Facility Simulation

Concurrent Programming – CT074-3-2

A Java Swing implementation of the Smart Laundry Facility Simulation using Java concurrency features and java.util.concurrent facilities.

The system simulates 50 customers, 6 washers, 4 dryers, and 2 payment kiosks. It includes random customer arrivals, configurable washing/drying/payment durations, probabilistic washer and payment failures, retry handling, thread-safe statistics, and a congestion-mode scenario with owner response.

IntelliJ IDEA

Extract the project.

Open the SmartLaundrySimulation folder in IntelliJ IDEA.

Select JDK 17 or newer. JDK 21 is recommended.

If required, right-click src/main/java and select Mark Directory as → Sources Root.

Open src/main/java/com/smartlaundry/Main.java.

Run Main.main().

No Maven or Gradle dependency is required. The project is a standard Java application using Swing and java.util.concurrent.

Console Modes

Normal Mode

javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java).FullName
java -cp out com.smartlaundry.Main --console

Congestion Mode

java -cp out com.smartlaundry.Main --console --congestion

Concurrency Mapping

Component

Concurrency Technique

Purpose

Customers

Runnable + Thread

Represents each customer as an independent concurrent task.

Washers

Semaphore(6) + ReentrantLock

Controls washer capacity and protects resource assignment.

Dryers

Semaphore(4) + ReentrantLock

Controls dryer capacity and protects resource assignment.

Payment kiosks

Semaphore(2) + ReentrantLock

Controls kiosk capacity and protects resource assignment.

Statistics

AtomicInteger / AtomicLong

Provides thread-safe counters and timing totals.

Shared runtime state

AtomicBoolean / AtomicReference

Manages simulation flags and lifecycle state safely.

Customer activities

ConcurrentHashMap

Stores live customer activity information for concurrent access.

Queues and events

ConcurrentLinkedDeque

Stores waiting customers and event history with concurrent access.

Random values

ThreadLocalRandom

Generates timing values and probabilistic failure events.

Resource recovery

ScheduledExecutorService

Schedules delayed repair and recovery tasks.

GUI refresh

Swing Timer + Event Dispatch Thread

Periodically updates the Swing interface.

Important Implementation Assumptions

Washer failure recovery: The brief does not fully define how a failed washer is repaired. This implementation temporarily removes the failed washer from service, repairs it after 1 second, and allows the customer to retry.

Payment kiosk failure recovery: A normal payment kiosk failure is repaired after 1 second, while the affected customer still waits the required 2 seconds before retrying payment.

Congestion-mode recovery: In the bonus congestion scenario, the owner is called when the real payment queue reaches 30 customers. After a configurable 5-second owner response delay, both kiosks are restored so the simulation can continue. The recovery action is an implementation assumption because the brief specifies the owner call but does not define the subsequent recovery behaviour.

Overall simulation duration: The brief contains both approximately 60 seconds and approximately 1–2 minutes for the overall simulation. The specified individual activity durations are preserved; total runtime varies according to random arrivals, resource contention, retries, and queueing.

GUI Features

The light-theme Swing dashboard provides:

RUNNING, STOPPED, COMPLETED, and CONGESTION MODE states

Separate cards for all 6 washers, 4 dryers, and 2 payment kiosks

Current resource status, assigned customer, operation, and remaining time

Live customer activity table

Actual washer, dryer, and payment queue customer IDs and waiting reasons

Current and peak resource utilisation information

Thread-safe customer and resource statistics

Failure and retry visibility

Thread-aware event log

Customer completion progress

Normal and congestion-mode controls

Periodic Swing GUI refresh on the Event Dispatch Thread

The GUI acts as the presentation layer. Customer execution, resource coordination, failure recovery, queues, and statistics are handled by the simulation and service classes.

Verification Results

The current implementation was verified as follows:

The project was compiled successfully using JDK 21.

The Swing GUI was smoke-tested under a headless X server.

The light-theme UI layout was checked at 1280×1024.

A full normal simulation completed with 50/50 customers served.

The normal run reached a maximum concurrent usage of 6 washers and 4 dryers.

The congestion scenario reached the real payment-queue threshold of 30 customers.

The congestion scenario triggered the owner-call event, restored both payment kiosks, and completed with 50/50 customers served.

Project Structure

SmartLaundrySimulation/
└── src/main/java/com/smartlaundry/
    ├── config/
    │   └── SimulationConfig.java
    ├── gui/
    │   ├── ActivityPanel.java
    │   ├── EventLogPanel.java
    │   ├── MainFrame.java
    │   ├── QueuePanel.java
    │   ├── ResourceCard.java
    │   ├── StatisticsPanel.java
    │   └── UiTheme.java
    ├── model/
    │   ├── Customer.java
    │   ├── CustomerActivity.java
    │   ├── Dryer.java
    │   ├── EventRecord.java
    │   ├── PaymentKiosk.java
    │   ├── ResourceSnapshot.java
    │   ├── ResourceStatus.java
    │   ├── SimulationStatus.java
    │   └── WashingMachine.java
    ├── service/
    │   ├── LaundryFacility.java
    │   ├── ResourceManager.java
    │   ├── SimulationState.java
    │   └── StatisticsManager.java
    ├── util/
    │   ├── Logger.java
    │   └── RandomUtils.java
    └── Main.java

Notes

For demonstration and assessment, run the normal mode to show concurrent customer activity, resource limits, queues, statistics, failures, retries, and the event log. Run congestion mode separately to demonstrate payment-queue congestion, the owner-call threshold, and kiosk restoration.