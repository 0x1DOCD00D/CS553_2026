package com.uic.cs553.distributed.examples

import com.uic.cs553.distributed.algorithms.TokenRingAlgorithm
import com.uic.cs553.distributed.framework.ExperimentRunner

/**
 * Example application demonstrating the Token Ring Algorithm
 */
object TokenRingExample extends App {
  
  println("""
    |========================================
    |  Token Ring Algorithm Demonstration
    |========================================
    |
    | The Token Ring algorithm demonstrates mutual exclusion:
    | 1. Nodes are arranged in a logical ring
    | 2. A single token circulates around the ring
    | 3. Only the node holding the token can enter critical section
    | 4. After finishing, node passes token to next node
    |
    | This is useful for:
    | - Mutual exclusion in distributed systems
    | - Fair resource access
    | - Preventing race conditions
    |
    """.stripMargin)
  
  // Configuration
  val nodeCount = 4
  val durationSeconds = 20
  
  // Run the experiment
  ExperimentRunner.runExperiment(
    algorithmName = "Token Ring",
    nodeCount = nodeCount,
    nodeFactory = (id: String) => {
      // Give the token to the first node
      val hasToken = id == "node-1"
      new TokenRingAlgorithm.TokenRingNode(id, hasToken)
    },
    durationSeconds = durationSeconds
  )
  
  println("\nExperiment completed successfully!")
  println("Each node should have accessed the critical section multiple times.")
}
