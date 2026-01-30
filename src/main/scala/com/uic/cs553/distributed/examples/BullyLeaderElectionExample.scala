package com.uic.cs553.distributed.examples

import com.uic.cs553.distributed.algorithms.BullyLeaderElection
import com.uic.cs553.distributed.framework.ExperimentRunner

/**
 * Example application demonstrating the Bully Leader Election Algorithm
 */
object BullyLeaderElectionExample extends App {
  
  println("""
    |========================================
    |  Bully Leader Election Demonstration
    |========================================
    |
    | The Bully algorithm is a leader election algorithm where:
    | 1. Each node has a unique numeric ID
    | 2. Nodes with higher IDs have higher priority
    | 3. Any node can initiate an election
    | 4. Nodes send election messages to higher-ID nodes
    | 5. If no response, the node declares itself leader
    | 6. The highest-ID node becomes the leader
    |
    | This is useful for:
    | - Coordinator selection in distributed systems
    | - Failure recovery
    | - Resource allocation
    |
    """.stripMargin)
  
  // Configuration
  val nodeCount = 7
  val durationSeconds = 15
  
  // Run the experiment
  ExperimentRunner.runExperiment(
    algorithmName = "Bully Leader Election",
    nodeCount = nodeCount,
    nodeFactory = (id: String) => {
      // Extract node number from id (e.g., "node-3" -> 3)
      val nodeNum = id.split("-").last.toInt
      new BullyLeaderElection.BullyNode(id, nodeNum)
    },
    durationSeconds = durationSeconds
  )
  
  println("\nExperiment completed successfully!")
  println("The node with the highest ID should have been elected as leader.")
}
