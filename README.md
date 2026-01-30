# CS553_2026
An open-source repository for a grad-level course at UIC on distributed systems

## Ping-Pong Actors Application

A baseline Scala 3.7 application demonstrating Akka Typed actors with Cinnamon instrumentation. The application creates two actors (PingActor and PongActor) that exchange messages in a ping-pong style.

### Prerequisites

- Java 11 or higher
- SBT 1.10.6 or higher
- Lightbend Cinnamon credentials (optional, for full instrumentation)

### Project Structure

```
.
├── build.sbt                  # SBT build configuration
├── project/
│   ├── build.properties       # SBT version
│   └── plugins.sbt           # SBT plugins including Cinnamon
└── src/
    └── main/
        ├── scala/
        │   └── com/uic/cs553/
        │       └── PingPongApp.scala  # Main application with Ping/Pong actors
        └── resources/
            ├── application.conf       # Akka and Cinnamon configuration
            └── logback.xml           # Logging configuration
```

### Features

- **Scala 3.7**: Uses the latest Scala 3 version
- **Akka Typed Actors**: Implements type-safe actor model
- **Ping-Pong Pattern**: Two actors exchanging messages
- **Cinnamon Instrumentation**: Lightbend Cinnamon for monitoring and metrics
- **Prometheus Metrics**: Metrics exposed on port 9001

### Building and Running

#### Without Cinnamon (if credentials not configured)

If you don't have Lightbend commercial credentials, you can comment out the Cinnamon-related dependencies in `build.sbt`:

```bash
# Compile the project
sbt compile

# Run the application
sbt run
```

#### With Cinnamon (full instrumentation)

1. Set up Lightbend commercial credentials in `~/.lightbend/commercial.credentials`
2. Update the resolver URL in `build.sbt` with your actual credentials token
3. Run with Cinnamon enabled:

```bash
sbt run
```

The application will:
1. Create a PingActor and PongActor
2. Exchange 10 ping-pong messages
3. Log each message exchange
4. Expose metrics on http://localhost:9001/metrics (with Cinnamon)
5. Shut down gracefully

### How It Works

1. **PingActor**: Initiates the game and responds to Pong messages with Ping messages
2. **PongActor**: Responds to Ping messages with Pong messages
3. **Message Exchange**: Actors exchange messages until reaching the maximum round count (10 by default)
4. **Cinnamon Monitoring**: Tracks actor metrics, message throughput, and system performance

### Customization

- Change the number of rounds by modifying the `maxRounds` parameter in `PingPongApp.scala`
- Adjust Akka configuration in `src/main/resources/application.conf`
- Modify Cinnamon settings in the same configuration file

### License

See LICENSE file for details.

