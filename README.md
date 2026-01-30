# CS553_2026 - Distributed Algorithms Framework

An open-source repository for a grad-level course at UIC on distributed systems. This project provides an Akka-based framework for students to develop and experiment with distributed algorithms.

## Overview

This framework allows students to:
- Implement and experiment with classical distributed algorithms
- Study distributed system concepts through hands-on experimentation
- Develop custom distributed algorithms using Akka actors
- Observe algorithm behavior in a controlled environment

## Features

- **Akka Actor-Based Framework**: Built on Akka Typed actors for robust distributed computation
- **Multiple Algorithm Implementations**:
  - **Echo Algorithm**: Broadcast and convergcast operations
  - **Bully Leader Election**: Leader election with priority-based selection
  - **Token Ring**: Mutual exclusion using token passing
- **Experiment Runner**: Utilities for running and observing distributed algorithms
- **Extensible Architecture**: Easy to add new algorithms

## Prerequisites

- Java 11 or higher
- Scala 2.13.x
- SBT 1.9.x

## Getting Started

### Building the Project

```bash
sbt compile
```

### Running Examples

The project includes several example applications demonstrating different distributed algorithms:

#### Echo Algorithm

```bash
sbt "runMain com.uic.cs553.distributed.examples.EchoAlgorithmExample"
```

The Echo algorithm demonstrates:
- Wave propagation from an initiator node
- Echo collection from all nodes
- Termination detection

#### Bully Leader Election

```bash
sbt "runMain com.uic.cs553.distributed.examples.BullyLeaderElectionExample"
```

The Bully algorithm demonstrates:
- Leader election based on node IDs
- Election message propagation
- Leader announcement

#### Token Ring

```bash
sbt "runMain com.uic.cs553.distributed.examples.TokenRingExample"
```

The Token Ring algorithm demonstrates:
- Mutual exclusion through token passing
- Fair access to critical sections
- Ring topology

## Project Structure

```
src/main/scala/com/uic/cs553/distributed/
├── framework/           # Core framework classes
│   ├── DistributedNode.scala          # Base classes for nodes
│   └── ExperimentRunner.scala         # Experiment execution utilities
├── algorithms/          # Distributed algorithm implementations
│   ├── EchoAlgorithm.scala
│   ├── BullyLeaderElection.scala
│   └── TokenRingAlgorithm.scala
└── examples/           # Example applications
    ├── EchoAlgorithmExample.scala
    ├── BullyLeaderElectionExample.scala
    └── TokenRingExample.scala
```

## Implementing Your Own Algorithm

To create a custom distributed algorithm:

1. **Extend the base classes**:

```scala
import com.uic.cs553.distributed.framework._

class MyAlgorithmNode(nodeId: String) extends BaseDistributedNode(nodeId) {
  override protected def onMessage(
    ctx: ActorContext[DistributedMessage],
    msg: DistributedMessage
  ): Behavior[DistributedMessage] = {
    // Implement your algorithm logic here
    msg match {
      case CommonMessages.Start() =>
        // Initialize your algorithm
        Behaviors.same
      case _ =>
        Behaviors.same
    }
  }
}
```

2. **Create an example application**:

```scala
import com.uic.cs553.distributed.framework.ExperimentRunner

object MyAlgorithmExample extends App {
  ExperimentRunner.runExperiment(
    algorithmName = "My Algorithm",
    nodeCount = 5,
    nodeFactory = (id: String) => new MyAlgorithmNode(id),
    durationSeconds = 30
  )
}
```

3. **Run your algorithm**:

```bash
sbt "runMain com.uic.cs553.distributed.examples.MyAlgorithmExample"
```

## Key Concepts

### DistributedNode

The base class for all nodes in a distributed algorithm. Provides:
- Automatic peer initialization
- Message handling infrastructure
- Broadcasting capabilities

### ExperimentRunner

A utility for running distributed algorithm experiments with:
- Automatic node creation and initialization
- Coordinated start/stop of algorithms
- Configurable execution duration

### Message Types

All messages extend `DistributedMessage`. Common messages include:
- `Initialize`: Set up node connections
- `Start`: Begin algorithm execution
- `Stop`: Terminate the algorithm

## Advanced Usage

### Custom Network Topologies

While the framework defaults to a fully-connected topology, you can create custom topologies by controlling peer initialization:

```scala
// Example: Ring topology
val nodes = createNodes(nodeCount)
nodes.zipWithIndex.foreach { case (node, i) =>
  val nextNode = nodes((i + 1) % nodes.length)
  node ! SetNext(nextNode)
}
```

### State Monitoring

Query node states during execution:

```scala
node ! CommonMessages.GetState(replyTo)
```

## Testing

Run tests with:

```bash
sbt test
```

## Contributing

Students are encouraged to:
1. Implement additional distributed algorithms
2. Enhance existing algorithms with new features
3. Add visualization or monitoring capabilities
4. Improve documentation

## License

See the LICENSE file for details.

## Course Information

This repository is part of CS553 - Distributed Systems at the University of Illinois Chicago (UIC).

## Further Reading

- [Akka Documentation](https://akka.io/docs/)
- "Distributed Algorithms" by Nancy Lynch
- "Introduction to Reliable and Secure Distributed Programming" by Cachin, Guerraoui, and Rodrigues

