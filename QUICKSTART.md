# Quick Start Guide

This guide will help you get started with the Distributed Algorithms Framework quickly.

## Prerequisites

1. **Install Java 11+**
   ```bash
   java -version
   ```

2. **Install SBT (Scala Build Tool)**
   
   On Ubuntu/Debian:
   ```bash
   echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | sudo tee /etc/apt/sources.list.d/sbt.list
   curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | sudo apt-key add
   sudo apt-get update
   sudo apt-get install sbt
   ```
   
   On macOS:
   ```bash
   brew install sbt
   ```
   
   On Windows:
   Download from https://www.scala-sbt.org/download.html

## Building the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/0x1DOCD00D/CS553_2026.git
   cd CS553_2026
   ```

2. Compile the project:
   ```bash
   sbt compile
   ```

## Running Examples

### Echo Algorithm
Demonstrates broadcast and convergcast operations:
```bash
sbt "runMain com.uic.cs553.distributed.examples.EchoAlgorithmExample"
```

**What you'll see:**
- Node-1 initiates a wave to all nodes
- Each node forwards the wave to neighbors
- Nodes send echo messages back
- Algorithm completes when initiator receives all echoes

### Bully Leader Election
Demonstrates leader election based on node priorities:
```bash
sbt "runMain com.uic.cs553.distributed.examples.BullyLeaderElectionExample"
```

**What you'll see:**
- All nodes start an election
- Nodes with higher IDs suppress lower-ID nodes
- The highest-ID node (node-7) becomes the leader
- Victory message is broadcast

### Token Ring
Demonstrates mutual exclusion using token passing:
```bash
sbt "runMain com.uic.cs553.distributed.examples.TokenRingExample"
```

**What you'll see:**
- Token circulates among nodes in the ring
- Each node enters critical section when it has the token
- Fair access - each node gets equal opportunities

## Running Tests

```bash
sbt test
```

## Development Workflow

### 1. Understanding the Framework

Start by reading these files in order:
1. `framework/DistributedNode.scala` - Base classes
2. `algorithms/EchoAlgorithm.scala` - Simple example
3. `examples/EchoAlgorithmExample.scala` - How to run it

### 2. Creating Your Own Algorithm

Create a new file in `src/main/scala/com/uic/cs553/distributed/algorithms/`:

```scala
package com.uic.cs553.distributed.algorithms

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import com.uic.cs553.distributed.framework._

object MyAlgorithm {
  
  // Define your messages
  sealed trait MyMessage extends DistributedMessage
  case class CustomMessage(data: String, sender: ActorRef[DistributedMessage]) extends MyMessage
  
  // Create your node class
  class MyNode(nodeId: String) extends BaseDistributedNode(nodeId) {
    
    override protected def onMessage(
      ctx: ActorContext[DistributedMessage],
      msg: DistributedMessage
    ): Behavior[DistributedMessage] = {
      msg match {
        case CommonMessages.Start() =>
          // Your initialization logic
          ctx.log.info(s"[$nodeId] Starting my algorithm")
          Behaviors.same
        
        case CustomMessage(data, sender) =>
          // Handle your custom messages
          ctx.log.info(s"[$nodeId] Received: $data")
          Behaviors.same
        
        case _ =>
          Behaviors.same
      }
    }
  }
}
```

### 3. Creating an Example Application

Create a file in `src/main/scala/com/uic/cs553/distributed/examples/`:

```scala
package com.uic.cs553.distributed.examples

import com.uic.cs553.distributed.algorithms.MyAlgorithm
import com.uic.cs553.distributed.framework.ExperimentRunner

object MyAlgorithmExample extends App {
  ExperimentRunner.runExperiment(
    algorithmName = "My Algorithm",
    nodeCount = 5,
    nodeFactory = (id: String) => new MyAlgorithm.MyNode(id),
    durationSeconds = 30
  )
}
```

### 4. Run Your Algorithm

```bash
sbt "runMain com.uic.cs553.distributed.examples.MyAlgorithmExample"
```

## Debugging Tips

1. **Increase logging verbosity**: Edit `src/main/resources/logback.xml`
   ```xml
   <logger name="com.uic.cs553" level="DEBUG" />
   ```

2. **Add custom logging**:
   ```scala
   ctx.log.info(s"[$nodeId] Your debug message here")
   ```

3. **Interactive mode**: Use `sbt` shell for faster iteration
   ```bash
   sbt
   > compile
   > test
   > runMain com.uic.cs553.distributed.examples.EchoAlgorithmExample
   ```

## Common Issues

### "Out of memory" errors
Increase JVM heap size:
```bash
export SBT_OPTS="-Xmx2G -Xss2M"
sbt compile
```

### Compilation errors with sender references
In Akka Typed, there's no `ctx.sender()`. Always pass sender as part of the message:
```scala
case class MyMessage(data: String, sender: ActorRef[DistributedMessage])
```

### Actor not receiving messages
Make sure to:
1. Initialize nodes with `CommonMessages.Initialize(peers)`
2. Start algorithm with `CommonMessages.Start()`
3. Check that messages extend `DistributedMessage`

## Next Steps

1. **Study existing algorithms** in the `algorithms/` directory
2. **Implement classic algorithms** like:
   - Ricart-Agrawala mutual exclusion
   - Chandy-Lamport snapshot
   - Ring election
   - Flood fill
3. **Add features** like:
   - Message delays
   - Node failures
   - Network partitions
   - Visualization
4. **Write tests** for your algorithms

## Resources

- [Akka Documentation](https://doc.akka.io/docs/akka/current/)
- [Distributed Algorithms by Nancy Lynch](https://mitpress.mit.edu/9780262011549/)
- Course materials and lectures

## Getting Help

- Check the README.md for detailed documentation
- Review example implementations
- Look at test cases for usage patterns
- Ask questions in course forums or office hours

Happy coding! 🚀
