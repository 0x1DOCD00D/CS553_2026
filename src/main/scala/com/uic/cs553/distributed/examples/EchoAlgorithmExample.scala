package com.uic.cs553.distributed.examples

import com.uic.cs553.distributed.algorithms.EchoAlgorithm
import com.uic.cs553.distributed.framework.ExperimentRunner

/**
 * Example application demonstrating the Echo Algorithm
 */
object EchoAlgorithmExample extends App {
  
  println("""
    |========================================
    |  Echo Algorithm Demonstration
    |========================================
    |
    | The Echo algorithm is a fundamental distributed algorithm where:
    | 1. An initiator node sends a wave message to all neighbors
    | 2. Each node forwards the wave to all its neighbors (except parent)
    | 3. Nodes send echo messages back when they receive echoes from all children
    | 4. The initiator terminates when it receives all echoes
    |
    | This is useful for:
    | - Broadcast and convergcast operations
    | - Termination detection
    | - Spanning tree construction
    |
    """.stripMargin)
  
  // Configuration
  val nodeCount = 5
  val durationSeconds = 10
  
  // Run the experiment
  ExperimentRunner.runExperiment(
    algorithmName = "Echo Algorithm",
    nodeCount = nodeCount,
    nodeFactory = (id: String) => {
      // Make the first node the initiator
      val isInitiator = id == "node-1"
      new EchoAlgorithm.EchoNode(id, isInitiator)
    },
    durationSeconds = durationSeconds
  )
  
  println("\nExperiment completed successfully!")
}
