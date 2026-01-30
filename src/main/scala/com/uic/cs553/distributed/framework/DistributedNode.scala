package com.uic.cs553.distributed.framework

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}

/**
 * Base trait for all messages in the distributed system
 */
trait DistributedMessage

/**
 * Base trait for node behaviors in distributed algorithms
 */
trait DistributedNode {
  /**
   * The unique identifier for this node
   */
  def nodeId: String
  
  /**
   * Create the behavior for this node
   */
  def behavior(): Behavior[DistributedMessage]
}

/**
 * Common messages used across distributed algorithms
 */
object CommonMessages {
  case class Initialize(peers: Set[ActorRef[DistributedMessage]]) extends DistributedMessage
  case class Start() extends DistributedMessage
  case class Stop() extends DistributedMessage
  case class GetState(replyTo: ActorRef[StateResponse]) extends DistributedMessage
  case class StateResponse(nodeId: String, state: Map[String, Any]) extends DistributedMessage
}

/**
 * Network message wrapper for messages between nodes
 */
case class NetworkMessage(
  from: String,
  to: String,
  payload: DistributedMessage
) extends DistributedMessage

/**
 * Base class for implementing distributed algorithm nodes
 */
abstract class BaseDistributedNode(val nodeId: String) extends DistributedNode {
  
  private var peers: Set[ActorRef[DistributedMessage]] = Set.empty
  
  /**
   * Get the current set of peer references
   */
  protected def getPeers: Set[ActorRef[DistributedMessage]] = peers
  
  /**
   * Handle initialization of the node
   */
  protected def onInitialize(
    ctx: ActorContext[DistributedMessage],
    peerRefs: Set[ActorRef[DistributedMessage]]
  ): Behavior[DistributedMessage] = {
    peers = peerRefs
    ctx.log.info(s"Node $nodeId initialized with ${peers.size} peers")
    Behaviors.same
  }
  
  /**
   * Handle incoming messages
   */
  protected def onMessage(
    ctx: ActorContext[DistributedMessage],
    msg: DistributedMessage
  ): Behavior[DistributedMessage]
  
  /**
   * Default behavior implementation
   */
  def behavior(): Behavior[DistributedMessage] = {
    Behaviors.setup { ctx =>
      Behaviors.receiveMessage {
        case CommonMessages.Initialize(peerRefs) =>
          onInitialize(ctx, peerRefs)
        
        case CommonMessages.Stop() =>
          ctx.log.info(s"Node $nodeId stopping")
          Behaviors.stopped
        
        case msg =>
          onMessage(ctx, msg)
      }
    }
  }
  
  /**
   * Send a message to all peers
   */
  protected def broadcast(msg: DistributedMessage): Unit = {
    getPeers.foreach(_ ! msg)
  }
}
