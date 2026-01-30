# CS553_2026
An open-source repository for a grad-level course at UIC on distributed systems

## Ping-Pong Actors Application

A baseline Scala 3.7 application demonstrating typed actors with message exchange in a ping-pong style. The application currently uses **Apache Pekko** (an Apache Software Foundation fork of Akka that maintains API compatibility). Instructions for using Akka with Cinnamon instrumentation are provided below.

### Prerequisites

- Java 11 or higher (Java 17 recommended)
- SBT 1.10.6 or higher
- Lightbend Cinnamon credentials (optional, only if using Akka + Cinnamon)

### Project Structure

```
.
├── build.sbt                  # SBT build configuration
├── project/
│   ├── build.properties       # SBT version
│   └── plugins.sbt           # SBT plugins (Cinnamon plugin commented out)
└── src/
    └── main/
        ├── scala/
        │   └── com/uic/cs553/
        │       └── PingPongApp.scala  # Main application with Ping/Pong actors
        └── resources/
            ├── application.conf       # Pekko configuration (Akka+Cinnamon config commented)
            └── logback.xml           # Logging configuration
```

### Features

- **Scala 3.7**: Uses the latest Scala 3 version
- **Typed Actors**: Implements type-safe actor model using Apache Pekko Typed
- **Ping-Pong Pattern**: Two actors (PingActor and PongActor) exchanging messages
- **Configurable Rounds**: Configurable number of message exchanges (default: 10)
- **Logging**: Comprehensive logging of actor lifecycle and message flow
- **Ready for Cinnamon**: Easy migration path to Akka with Lightbend Cinnamon instrumentation

### Building and Running

#### Current Implementation (Apache Pekko)

```bash
# Compile the project
sbt compile

# Run the application
sbt run
```

The application will:
1. Create a PingActor and PongActor
2. Exchange 10 ping-pong messages (configurable)
3. Log each message exchange with timestamps
4. Shut down gracefully after completing all rounds

#### Migrating to Akka with Cinnamon Instrumentation

To use Akka with Lightbend Cinnamon for advanced monitoring:

1. **Set up Lightbend credentials:**
   - Create `~/.lightbend/commercial.credentials`
   - Add your Lightbend commercial repository access token

2. **Update `build.sbt`:**
   - Comment out the Pekko dependencies
   - Uncomment the Akka + Cinnamon section
   - Replace `XXXXX` in the resolver URL with your actual token

3. **Update `project/plugins.sbt`:**
   - Uncomment the Cinnamon plugin line

4. **Update `src/main/scala/com/uic/cs553/PingPongApp.scala`:**
   - Change imports from `org.apache.pekko` to `akka`
   - Update the startup log message if desired

5. **Update `src/main/resources/application.conf`:**
   - Replace the `pekko` configuration block with the `akka` and `cinnamon` blocks (see comments in file)

6. **Run with Cinnamon:**
   ```bash
   sbt run
   ```
   - Metrics will be exposed on http://localhost:9001/metrics (Prometheus format)

### How It Works

**Actor Model Components:**

1. **PingActor**: 
   - Initiates the ping-pong game when receiving `Start` message
   - Responds to `Pong` messages by sending `Ping` messages
   - Stops both actors after reaching maximum rounds

2. **PongActor**: 
   - Responds to `Ping` messages with `Pong` messages
   - Stops when receiving `Stop` signal

3. **Message Protocol**:
   - `Ping(replyTo: ActorRef[Message])`: Request message containing sender reference
   - `Pong(replyTo: ActorRef[Message])`: Response message containing sender reference
   - `Start`: Signal to begin the ping-pong exchange
   - `Stop`: Signal to terminate actors

4. **Message Flow**:
   ```
   Main → PingPongApp: Start
   PingPongApp → PingActor: Start
   PingActor → PongActor: Ping(replyTo)
   PongActor → PingActor: Pong(replyTo)
   PingActor → PongActor: Ping(replyTo)
   ... (continues for configured rounds)
   PingActor → PongActor: Stop
   ```

### Customization

- **Number of Rounds**: Modify the `maxRounds` parameter in `PingPongApp.scala` (line 97)
- **Message Delay**: Adjust the delay duration in `context.scheduleOnce(100.millis, ...)` calls (currently 100 milliseconds)
- **Actor Configuration**: Modify dispatcher settings in `application.conf`
- **Logging Level**: Change `loglevel` in `application.conf` or `logback.xml`

### Example Output

```
01:23:20.232 [sbt-bg-threads-1] INFO  Main - === Starting Ping-Pong Actor System (Apache Pekko) ===
01:23:20.786 [ping-pong-system-pekko.actor.default-dispatcher-5] INFO  PingPongApp - Setting up PingPong application
01:23:20.790 [ping-pong-system-pekko.actor.default-dispatcher-6] INFO  PingActor - PingActor: Starting ping-pong game with max 10 rounds
01:23:20.791 [ping-pong-system-pekko.actor.default-dispatcher-6] INFO  PingActor - PingActor: Sending Ping #1
01:23:20.791 [ping-pong-system-pekko.actor.default-dispatcher-6] INFO  PongActor - PongActor: Received Ping, sending Pong #1
01:23:20.892 [ping-pong-system-pekko.actor.default-dispatcher-5] INFO  PingActor - PingActor: Received Pong, sending Ping #2
...
01:23:22.499 [ping-pong-system-pekko.actor.default-dispatcher-5] INFO  PingActor - PingActor: Reached max rounds (10), stopping
01:23:25.778 [sbt-bg-threads-1] INFO  Main - === Shutting down Ping-Pong Actor System ===
```

### License

See LICENSE file for details.


