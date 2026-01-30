# NetGameSim to Akka Distributed Algorithms Simulator

This repository is a course project that turns randomly generated graphs into a running message passing system. It starts from NetGameSim, an experimental platform that generates large random graphs, can perturb them, and exports artifacts for analysis and visualization.

The main outcome is a configurable Akka simulation where each graph node becomes an actor and each graph edge becomes a communication channel. The simulation produces background traffic using a probability distribution assigned to each actor, and it provides a framework for implementing distributed algorithms on top of the same network.

Upstream links and the code walk through video are shown below.

[NetGameSim repository](https://github.com/0x1DOCD00D/NetGameSim) and NetGameSim overview and [code walk through video](https://www.youtube.com/watch?v=6fdazJBkdjA&t=2658s)

Akka documentation [home](https://doc.akka.io/index.html)

## What you will build

You will build an end to end pipeline that goes from graph generation to a running distributed computation.

1. Generate random graphs with the desired size and structural properties.
2. Extend the graph model so edges are labeled with the types of messages they carry.
3. Assign a probability distribution function to each node to determine what messages it produces.
4. Convert the graph into an Akka actor system where nodes are actors and edges are channels.
5. Run the system, generate traffic from the node distributions, and record metrics.
6. Implement two distributed algorithms assigned by the instructor on top of the same runtime.

## Initiating computations

A distributed system does not magically start doing useful work. This project supports two explicit initiation mechanisms so that computations begin in a controllable, repeatable way.

Timer nodes start the computation by running a periodic timer. On every tick, a timer node generates one or more messages and sends them to eligible neighbors. The message type can be fixed or sampled from the node PDF, and the destination must respect the edge label constraints.

Input nodes start the computation by accepting external stimuli. The simulation driver can inject messages into specific nodes at runtime, which models clients, sensors, workload traces, or test harnesses. An input node treats injected messages exactly like normal messages, so algorithms do not need special cases.

A simple configuration pattern is shown below.

```hocon
sim {
  initiators {
    timers = [
      { node = 0, tickEveryMs = 50, mode = "pdf" },
      { node = 7, tickEveryMs = 100, mode = "fixed", fixedMsg = "WORK" }
    ]

    inputs = [
      { node = 1 },
      { node = 2 }
    ]
  }
}
```

The CLI should support at least a file driven injection mode and an interactive injection mode. In file driven mode, each line describes when and where to inject a message. In interactive mode, the driver reads commands from standard input and forwards them to the selected node actor.

## Architecture

The implementation is organized around four concepts.

The graph generator produces a topology and optional perturbations.

The graph enrichment layer adds message oriented metadata. Each edge gains a label describing which message types are allowed on that channel. Each node gains a PDF describing how likely it is to produce each application level message type.

The runtime turns the enriched graph into actors and channels. A node actor owns local state, schedules outbound message generation, and runs one or more distributed algorithm modules. A channel is modeled as the destination actor reference stored by the source actor. If you want to add delay, loss, duplication, or non FIFO behavior, you can insert an explicit channel actor between the endpoints later, but the baseline mapping is direct.

The algorithm layer implements distributed algorithms as plug in modules that share the same messaging substrate, logging, and instrumentation.

## Repository layout

A typical layout for the project is shown below.

```text
.
├─ netgamesim/                 # fork or submodule of NetGameSim
├─ sim-core/                   # graph model, edge labels, node PDFs, serialization
├─ sim-runtime-akka/           # actor runtime, metrics, run loop
├─ sim-algorithms/             # student implementations of assigned algorithms
├─ sim-cli/                    # command line entry points to run experiments
└─ docs/                       # report and experiment notes
```

## Getting started

Follow the upstream NetGameSim build instructions to ensure you can generate a graph artifact and a visualization before integrating Akka.

Add the following section to your README, ideally right after Getting started and before the actor template section.

```markdown
## Akka learning examples by Prof. Grechanik

Before you wire NetGameSim graphs into actors, spend 30 to 60 minutes with the small Akka experiments in the PLANE repository. These files are short, runnable, and they demonstrate the exact patterns this project relies on.

The examples live here.
https://github.com/0x1DOCD00D/PLANE/tree/master/src/main/scala/Akka

Most files include an @main entry point or extend App. Run them from sbt by invoking the generated main, or open them in an IDE and run the main method.

Suggested reading order is shown below.

Basic actor messaging and forwarding
https://github.com/0x1DOCD00D/PLANE/blob/master/src/main/scala/Akka/BasicActorComm.scala
This is the smallest example of creating an ActorSystem, instantiating actors with Props, sending messages with !, replying to sender(), and forwarding a message to preserve the original sender.

Scheduling and timers
https://github.com/0x1DOCD00D/PLANE/blob/master/src/main/scala/Akka/ScheduleEventsRandomIntervals.scala
This shows how to drive a computation with the Akka scheduler instead of Thread.sleep. This is the pattern you will reuse for timer nodes in the simulator.

Actor state machines with context.become
https://github.com/0x1DOCD00D/PLANE/blob/master/src/main/scala/Akka/StateMachineWithActors.scala
This demonstrates a state machine implemented as an actor that switches receive handlers. This maps directly to algorithm phases such as snapshot colored vs uncolored or termination passive vs active.

Diffusing computation on a tree
https://github.com/0x1DOCD00D/PLANE/blob/master/src/main/scala/Akka/DiffusingComputation.scala
This is a simple diffusion style computation that creates a tree of actors, sends workload down, and aggregates acknowledgements up. This is the mental model behind many termination detection algorithms.

ActorSystem lifecycle and coordinated shutdown
https://github.com/0x1DOCD00D/PLANE/blob/master/src/main/scala/Akka/ActorSystemExperiments.scala
This shows system termination, registerOnTermination hooks, and adding tasks to CoordinatedShutdown. You will need these patterns to stop long simulations cleanly and to flush metrics.

Generic messages with Scala Dynamic
https://github.com/0x1DOCD00D/PLANE/blob/master/src/main/scala/Akka/AkkaSelectDynamic.scala
This demonstrates sending a record-like object and mutating its fields through selectDynamic and updateDynamic. You do not have to use Dynamic, but it is a useful contrast for why explicit message ADTs are safer.

Remote actors, actorSelection, and deployment
https://github.com/0x1DOCD00D/PLANE/blob/master/src/main/scala/Akka/RemoteActorExperiment1.scala
This contains a local and a remote setup, shows actor paths, actorSelection with Identify, and RemoteScope deployment. This is the shortest path from local actors to a distributed topology.

Chit chat message protocol and remote handshake
https://github.com/0x1DOCD00D/PLANE/blob/master/src/main/scala/Akka/ChitChatMessages.scala
https://github.com/0x1DOCD00D/PLANE/blob/master/src/main/scala/Akka/ChatActor.scala
https://github.com/0x1DOCD00D/PLANE/blob/master/src/main/scala/Akka/ChitActor.scala
These files define a tiny message protocol and two actors that start and stop a chat, including a remote identity handshake. This mirrors how you will define your own message types and control messages for distributed algorithms.

Typed Akka HTTP microservice
https://github.com/0x1DOCD00D/PLANE/blob/master/src/main/scala/Akka/HttpBasicService.scala
This is a minimal typed ActorSystem hosting an Akka HTTP route. It is optional for this project, but it is a good reference if you expose simulation controls through an HTTP endpoint.

Akka Streams building blocks and graph wiring
https://github.com/0x1DOCD00D/PLANE/blob/master/src/main/scala/Akka/StreamItUpAndDown.scala
https://github.com/0x1DOCD00D/PLANE/blob/master/src/main/scala/Akka/StreamGraphDocExample.scala
These show Source, Flow, Sink, async boundaries, and GraphDSL wiring. Streams are optional, but they are handy if you want to build trace pipelines or online aggregation of metrics.

How these examples map to this project

Timer nodes should follow the scheduling pattern from ScheduleEventsRandomIntervals.scala. Use the scheduler to send tick messages without blocking actor threads.

Input nodes should follow the message sending and forwarding style from BasicActorComm.scala. Keep message ADTs small and explicit.

Algorithm modules should follow the state switching pattern from StateMachineWithActors.scala. Use context.become to model phases and to keep state changes obvious.

Termination style workloads can be modeled like DiffusingComputation.scala. Make sure you can observe when all work has diffused and acknowledgements have returned.

Simulation shutdown and metrics flush should follow ActorSystemExperiments.scala. Terminate cleanly and deterministically.
```

The file list and links come from the PLANE Akka examples directory contents. ([GitHub API][1])
The brief explanations are based on the corresponding example source files. ([GitHub][2])

[1]: https://api.github.com/repos/0x1DOCD00D/PLANE/contents/src/main/scala/Akka?ref=master "api.github.com"
[2]: https://raw.githubusercontent.com/0x1DOCD00D/PLANE/master/src/main/scala/Akka/BasicActorComm.scala "raw.githubusercontent.com"


A minimal command line workflow is shown below.

```bash
sbt clean test

sbt "sim-cli/runMain edu.uic.cs553.SimMain --config conf/sim.conf --out outputs/run1"

sbt "sim-cli/runMain edu.uic.cs553.SimMain --graph outputs/run1/graph.json --run 60s"
```

## Actor template in Scala

This project uses Akka classic actors to represent graph nodes. A node actor stores a map of destination actor references that represent its outgoing channels. Sending a message over an edge means sending a message to the destination ActorRef.

Consider the following example.

```scala
import akka.actor.{Actor, ActorRef, Props, Timers}
import scala.concurrent.duration.*

object NodeActor:
  def props(id: Int): Props = Props(new NodeActor(id))

  sealed trait Msg
  final case class Init(
    neighbors: Map[Int, ActorRef],
    allowedOnEdge: Map[Int, Set[String]],
    pdf: Map[String, Double],
    timerEnabled: Boolean,
    tickEvery: FiniteDuration
  ) extends Msg

  final case class ExternalInput(kind: String, payload: String) extends Msg
  final case class Envelope(from: Int, kind: String, payload: String) extends Msg
  private case object Tick extends Msg

final class NodeActor(id: Int) extends Actor with Timers:
  import NodeActor.*

  private var neighbors: Map[Int, ActorRef] = Map.empty
  private var allowedOnEdge: Map[Int, Set[String]] = Map.empty
  private var pdf: Map[String, Double] = Map.empty

  override def receive: Receive =
    case Init(nbrs, allow, pdf0, timerEnabled, tickEvery) =>
      neighbors = nbrs
      allowedOnEdge = allow
      pdf = pdf0

      if timerEnabled then
        timers.startTimerAtFixedRate("tick", Tick, tickEvery)

    case Tick =>
      val kind = sampleFromPdfOrDefault(pdf, default = "PING")
      sendToOneNeighbor(kind, payload = s"tick-from-$id")

    case ExternalInput(kind, payload) =>
      sendToOneNeighbor(kind, payload)

    case Envelope(from, kind, payload) =>
      // Handle normal incoming traffic here.
      // Algorithms plug in at this point and can inspect or react to the message.
      ()

  private def sendToOneNeighbor(kind: String, payload: String): Unit =
    val eligible = neighbors.keys.filter { to =>
      allowedOnEdge.getOrElse(to, Set.empty).contains(kind)
    }.toList

    eligible.headOption.foreach { to =>
      neighbors(to) ! Envelope(from = id, kind = kind, payload = payload)
    }

  private def sampleFromPdfOrDefault(pdf: Map[String, Double], default: String): String =
    // Keep this deterministic under a fixed seed in the real implementation.
    pdf.keys.headOption.getOrElse(default)
```

The code that instantiates actors from a graph should follow the same pattern. First create one actor per node. Then compute each node’s outgoing neighbor ActorRefs from the graph edges. Finally send Init to each actor, including the neighbor map, edge label constraints, the node PDF, and whether the node is a timer node.

Consider the following example.

```scala
import akka.actor.{ActorRef, ActorSystem}
import scala.concurrent.duration.*

val system = ActorSystem("sim")

val nodeIds: Seq[Int] = graph.nodes
val nodeRefs: Map[Int, ActorRef] =
  nodeIds.map(id => id -> system.actorOf(NodeActor.props(id), s"node-$id")).toMap

nodeIds.foreach { id =>
  val outgoing: Seq[Int] = graph.outNeighbors(id)

  val neighbors: Map[Int, ActorRef] =
    outgoing.map(to => to -> nodeRefs(to)).toMap

  val allowedOnEdge: Map[Int, Set[String]] =
    outgoing.map { to =>
      val allowed: Set[String] = graph.edgeLabel(id, to)
      to -> allowed
    }.toMap

  val pdf: Map[String, Double] = config.pdfForNode(id)

  val timerEnabled = config.isTimerNode(id)
  val tickEvery = config.tickEvery(id).millis

  nodeRefs(id) ! NodeActor.Init(neighbors, allowedOnEdge, pdf, timerEnabled, tickEvery)
}
```

## Graph generation and desired properties

Graph generation is controlled by configuration. In this project, you will define what properties you need for your assigned algorithms and experiments. Typical examples include connectivity from an initial node, bounded degree, controllable graph density, and the presence or absence of cycles.

You are expected to include at least three experiment configurations that vary graph properties in meaningful ways and to explain why those properties matter for the algorithms you implemented.

## Edge labels for message types

Each edge must be labeled with the message types that are allowed to traverse that channel. This requirement should be reflected in the in memory graph representation, the serialized output, and the visualization output.

A configuration driven labeling scheme is shown below.

```hocon
sim {
  messages {
    types = [ "CONTROL", "PING", "GOSSIP", "WORK", "ACK" ]
  }

  edgeLabeling {
    default = [ "CONTROL", "PING" ]

    overrides = [
      { from = 0, to = 7, allow = [ "CONTROL", "WORK", "ACK" ] },
      { from = 3, to = 5, allow = [ "CONTROL", "GOSSIP" ] }
    ]
  }
}
```

One practical approach is to treat algorithm messages as CONTROL and ensure CONTROL is permitted on every edge, while application traffic is constrained by the remaining labels.

## Node PDFs for message production

Each node actor must have a PDF that describes what application messages it produces. In practice, message types are discrete, so this is a probability mass function over the configured message types.

The PDF should be configurable and reproducible under a fixed seed. You should support at least two distribution families such as uniform and Zipf, and at least one per node override.

An example configuration is shown below.

```hocon
sim {
  traffic {
    tickIntervalMs = 50

    defaultPdf = [
      { msg = "PING",   p = 0.50 },
      { msg = "GOSSIP", p = 0.30 },
      { msg = "WORK",   p = 0.20 }
    ]

    perNodePdf = [
      { node = 12, pdf = [ { msg = "WORK", p = 0.80 }, { msg = "PING", p = 0.20 } ] }
    ]
  }
}
```

The runtime should validate that probabilities sum to 1.0 within a small tolerance and should fail fast if a configuration is inconsistent.

## Running computation on top of the network

The simulator should run a simple computation that is meaningful for distributed algorithms.

A recommended baseline is a work propagation model. A node maintains a queue of work items, processing a work item may enqueue additional work on neighbors by sending WORK messages, and nodes may become idle when their queue is empty. This supports termination detection algorithms because the computation has a well defined notion of completion.

A second baseline is continuous background chatter such as PING and GOSSIP traffic. This supports snapshot algorithms because it produces in flight messages and concurrent activity.

Your implementation should record message counts by type, approximate in flight messages per channel, time to completion for terminating workloads, and algorithm specific outcomes such as snapshot consistency checks or termination detection time.

## Distributed algorithm assignments

Each student implements two assigned distributed algorithms using the same runtime and message layer. Examples include snapshot algorithms such as Chandy Lamport and Lai Yang, and termination detection algorithms such as Dijkstra Scholten.

A minimal plug in interface is shown below.

```scala
trait DistributedAlgorithm:
  def name: String
  def onStart(ctx: NodeContext): Unit
  def onMessage(ctx: NodeContext, msg: Any): Unit
  def onTick(ctx: NodeContext): Unit = ()
```

The runtime should provide a NodeContext that includes the local node id, neighbor references, and helpers for sending messages, logging, and emitting metrics.

Your algorithms should include a deterministic test mode where randomness is disabled or seeded and where assertions can be checked automatically.

## Cinnamon instrumentation

This project supports Lightbend Telemetry, also known as Cinnamon, to collect metrics about actors, dispatchers, and message processing.

In sbt, add the sbt plugin and enable it for the project, then enable the agent for run and test.

Consider the following example.

```scala
// project/plugins.sbt
addSbtPlugin("com.lightbend.cinnamon" % "sbt-cinnamon" % "2.21.4")
```

```scala
// build.sbt
lazy val app = project in file(".") enablePlugins(Cinnamon)

run / cinnamon := true
test / cinnamon := true

cinnamonLogLevel := "INFO"

// Minimal instrumentation and JVM metrics producer
libraryDependencies += Cinnamon.library.cinnamonAkka
libraryDependencies += Cinnamon.library.cinnamonCHMetrics
libraryDependencies += Cinnamon.library.cinnamonJvmMetricsProducer
```

Cinnamon does not instrument actors unless you select them in application.conf. A minimal configuration that instruments all user actors is shown below.

```hocon
cinnamon {
  akka.actors = {
    default-by-class {
      includes = "/user/*"
      report-by = class
    }
  }
}
```

If you run from an IDE, make sure the run configuration includes a javaagent VM option that points to the Cinnamon agent jar.

## Deliverables

You are expected to deliver a runnable system that generates graphs, enriches them with edge labels and node PDFs, and runs an Akka simulation on the resulting topology. You will also deliver implementations of the two assigned distributed algorithms integrated into the same runtime.

Include a short report in docs that explains design decisions, configuration choices, and experiment results. Include a reproducible experiment script or command set that a grader can run on a clean machine. Include a brief demo, typically three to six minutes, that shows graph generation, a live run, and at least one algorithm result.

## Evaluation

Grading focuses on correctness first, then on clarity and reproducibility.

Correctness covers message handling, edge label enforcement, PDF driven traffic generation, initiation behavior for timer and input nodes, and algorithm properties.

Reproducibility covers fixed seeds, configuration driven experiments, and clear build instructions.

Clarity covers code organization, documentation, and tests that justify important invariants.

