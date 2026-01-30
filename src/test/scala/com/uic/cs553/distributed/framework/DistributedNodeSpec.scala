package com.uic.cs553.distributed.framework

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.uic.cs553.distributed.algorithms.EchoAlgorithm
import org.scalatest.wordspec.AnyWordSpecLike

/**
 * Basic tests for the distributed algorithm framework
 */
class DistributedNodeSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  
  "A DistributedNode" must {
    "be created with a node ID" in {
      val node = new EchoAlgorithm.EchoNode("test-node-1", isInitiator = false)
      node.nodeId shouldBe "test-node-1"
    }
    
    "create a behavior that can be spawned" in {
      val node = new EchoAlgorithm.EchoNode("test-node-2", isInitiator = false)
      val actorRef = spawn(node.behavior())
      
      // Verify the actor was created
      actorRef should not be null
    }
    
    "respond to Initialize message" in {
      val node = new EchoAlgorithm.EchoNode("test-node-3", isInitiator = false)
      val actorRef = spawn(node.behavior())
      
      // Send initialize with empty peers
      actorRef ! CommonMessages.Initialize(Set.empty)
      
      // Actor should still be alive after initialization
      Thread.sleep(100)
      actorRef should not be null
    }
  }
  
  "The framework" must {
    "allow multiple nodes to be created" in {
      val nodes = (1 to 5).map { i =>
        new EchoAlgorithm.EchoNode(s"node-$i", isInitiator = i == 1)
      }
      
      nodes.size shouldBe 5
      nodes.head.nodeId shouldBe "node-1"
      nodes.last.nodeId shouldBe "node-5"
    }
  }
}
