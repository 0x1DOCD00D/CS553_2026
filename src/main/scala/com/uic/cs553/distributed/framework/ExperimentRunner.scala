package com.uic.cs553.distributed.framework

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Coordinator for managing a distributed algorithm experiment
 */
object DistributedSystemCoordinator {
  
  sealed trait CoordinatorMessage
  case class CreateNodes(
    count: Int,
    nodeFactory: String => DistributedNode,
    replyTo: ActorRef[NodesCreated]
  ) extends CoordinatorMessage
  case class NodesCreated(nodes: List[ActorRef[DistributedMessage]]) extends CoordinatorMessage
  case class StartAlgorithm() extends CoordinatorMessage
  case class StopAlgorithm() extends CoordinatorMessage
  case class QueryStates(replyTo: ActorRef[SystemState]) extends CoordinatorMessage
  case class SystemState(states: Map[String, Map[String, Any]]) extends CoordinatorMessage
  
  def apply(): Behavior[CoordinatorMessage] = {
    Behaviors.setup { ctx =>
      var nodes: List[ActorRef[DistributedMessage]] = List.empty
      
      Behaviors.receiveMessage {
        case CreateNodes(count, nodeFactory, replyTo) =>
          ctx.log.info(s"Creating $count nodes")
          
          // Create nodes
          nodes = (1 to count).map { i =>
            val nodeId = s"node-$i"
            val node = nodeFactory(nodeId)
            ctx.spawn(node.behavior(), nodeId)
          }.toList
          
          // Initialize each node with references to all other nodes
          nodes.foreach { node =>
            val peers = nodes.filterNot(_ == node).toSet
            node ! CommonMessages.Initialize(peers)
          }
          
          replyTo ! NodesCreated(nodes)
          Behaviors.same
        
        case StartAlgorithm() =>
          ctx.log.info("Starting algorithm on all nodes")
          nodes.foreach(_ ! CommonMessages.Start())
          Behaviors.same
        
        case StopAlgorithm() =>
          ctx.log.info("Stopping algorithm on all nodes")
          nodes.foreach(_ ! CommonMessages.Stop())
          Behaviors.same
        
        case QueryStates(replyTo) =>
          ctx.log.info("Querying states from all nodes")
          // This is a simplified version - in production, you'd use ask pattern
          replyTo ! SystemState(Map.empty)
          Behaviors.same
      }
    }
  }
}

/**
 * Utility for running distributed algorithm experiments
 */
object ExperimentRunner {
  
  /**
   * Run a distributed algorithm with the specified number of nodes
   */
  def runExperiment(
    algorithmName: String,
    nodeCount: Int,
    nodeFactory: String => DistributedNode,
    durationSeconds: Int = 30
  ): Unit = {
    implicit val system: ActorSystem[DistributedSystemCoordinator.CoordinatorMessage] =
      ActorSystem(DistributedSystemCoordinator(), "DistributedSystem")
    
    println(s"=== Starting $algorithmName Experiment ===")
    println(s"Nodes: $nodeCount")
    println(s"Duration: ${durationSeconds}s")
    println("=" * 50)
    
    try {
      // Create nodes
      import akka.actor.typed.scaladsl.AskPattern._
      import akka.util.Timeout
      implicit val timeout: Timeout = 3.seconds
      
      val nodesFuture: Future[DistributedSystemCoordinator.NodesCreated] = 
        system.ask(replyTo => DistributedSystemCoordinator.CreateNodes(nodeCount, nodeFactory, replyTo))
      
      Await.result(nodesFuture, 5.seconds)
      
      // Start the algorithm
      system ! DistributedSystemCoordinator.StartAlgorithm()
      
      // Let it run
      println(s"Algorithm running for ${durationSeconds}s...")
      Thread.sleep(durationSeconds * 1000)
      
      // Stop the algorithm
      system ! DistributedSystemCoordinator.StopAlgorithm()
      
      println("\n=== Experiment Complete ===")
      
    } finally {
      Thread.sleep(1000) // Give time for cleanup
      system.terminate()
      Await.result(system.whenTerminated, 10.seconds)
    }
  }
}
