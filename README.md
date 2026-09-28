# Smart Laundry Facility Simulation

Concurrent Programming - CT074-3-2

A Java Swing implementation of the Smart Laundry Facility Simulation. The project follows the assignment values: 50 customers, 6 washers, 4 dryers, 2 payment kiosks, random 0–3 second arrivals, 4–6 second washing, 3–5 second drying, 1–2 second payment, 5% washer/payment failures, payment retry after 2 seconds, thread-safe statistics, and a congestion-mode bonus scenario.

## IntelliJ IDEA

1. Extract the project.
2. Open the `SmartLaundrySimulation` folder in IntelliJ IDEA.
3. Select JDK 17 or newer (JDK 21 recommended).
4. Right-click `src/main/java` → **Mark Directory as → Sources Root** if IntelliJ does not detect it automatically.
5. Open `src/main/java/com/smartlaundry/Main.java`.
6. Run `Main.main()`.

No Maven or Gradle dependency is required. It is a standard Java project using Swing and `java.util.concurrent`.

## Console modes

Normal:

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java).FullName
java -cp out com.smartlaundry.Main --console
```

Bonus congestion:

```powershell
java -cp out com.smartlaundry.Main --console --congestion
```

## Concurrency mapping

- Customer: `Runnable` + `Thread`
- Washers: `Semaphore(6)` + `ReentrantLock`
- Dryers: `Semaphore(4)` + `ReentrantLock`
- Payment kiosks: `Semaphore(2)` + `ReentrantLock`
- Statistics: `AtomicInteger` / `AtomicLong`
- Random values: `ThreadLocalRandom`
- GUI refresh: Swing `Timer` on the Event Dispatch Thread

## Important implementation assumptions

1. The assignment does not fully define how a failed washer is repaired. This implementation temporarily removes a failed washer from service, repairs it after 1 second, and allows the customer to retry.
2. A normal payment kiosk failure is repaired after 1 second; the customer still waits the required 2 seconds before retrying payment.
3. In the bonus congestion mode, the owner is called when the real payment queue reaches 30. After a configurable 5-second owner response delay, the kiosks are restored so the demonstration can finish. This recovery step is an implementation assumption because the brief specifies the owner call but does not specify what happens afterward.
4. The brief contains both approximately 60 seconds and approximately 1–2 minutes for overall simulation timing. Individual required activity durations are preserved; overall runtime depends on the random arrival schedule and queueing.

## GUI improvements

The light-theme Swing dashboard provides:

- clearly visible RUNNING / STOPPED / COMPLETED / CONGESTION MODE states
- all 6 washers, 4 dryers, and 2 kiosks as separate cards
- actual resource status, customer, current operation, and remaining time
- live customer activity table
- real washer/dryer/payment queue customer IDs and waiting reasons
- concurrency/resource utilization counts
- required and useful thread-safe statistics
- failure and retry visibility
- thread-aware event log
- real completion progress
- normal and congestion simulation controls
- Swing Event Dispatch Thread-safe periodic refresh

The GUI is only a presentation layer; customer threads and resource coordination live in the simulation/service classes.

## Verification performed

The recreated build was compiled successfully with JDK 21. The Swing GUI was smoke-tested under a headless X server and the light UI layout was checked at 1280×1024. A full normal simulation completed with 50/50 customers served and maximum concurrency of 6 washers and 4 dryers. The congestion scenario reached the real payment-queue threshold of 30 customers, triggered the owner-call event, restored both kiosks, and completed 50/50 customers.
