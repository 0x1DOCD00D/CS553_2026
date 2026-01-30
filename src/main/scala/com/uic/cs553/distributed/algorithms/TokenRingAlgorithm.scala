package com.uic.cs553.distributed.algorithms

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import com.uic.cs553.distributed.framework._
import scala.util.Random

/**
 * Token Ring Algorithm
 * 
 * This algorithm demonstrates mutual exclusion using a token ring:
 * 1. Nodes are arranged in a logical ring
 * 2. A token is passed around the ring
 * 3. Only the node with the token can access the critical section
 * 4. After finishing, the node passes the token to the next node
 */
object TokenRingAlgorithm {
  
  sealed trait TokenMessage extends DistributedMessage
  case class Token(value: Int) extends TokenMessage
  case class SetNext(next: ActorRef[DistributedMessage]) extends TokenMessage
  
  class TokenRingNode(
    nodeId: String,
    hasInitialToken: Boolean = false
  ) extends BaseDistributedNode(nodeId) {
    
    private var nextNode: Option[ActorRef[DistributedMessage]] = None
    private var hasToken: Boolean = hasInitialToken
    private var criticalSectionCount: Int = 0
    private val random = new Random()
    
    override protected def onInitialize(
      ctx: ActorContext[DistributedMessage],
      peerRefs: Set[ActorRef[DistributedMessage]]
    ): Behavior[DistributedMessage] = {
      super.onInitialize(ctx, peerRefs)
      
      // In a ring, each node has exactly one next node
      // For simplicity, we'll just pick one peer as next
      if (peerRefs.nonEmpty) {
        nextNode = Some(peerRefs.head)
        ctx.log.info(s"[$nodeId] Set next node in ring")
      }
      
      Behaviors.same
    }
    
    override protected def onMessage(
      ctx: ActorContext[DistributedMessage],
      msg: DistributedMessage
    ): Behavior[DistributedMessage] = {
      msg match {
        case CommonMessages.Start() =>
          if (hasToken) {
            ctx.log.info(s"[$nodeId] Starting with token")
            scheduleTokenPass(ctx)
          }
          Behaviors.same
        
        case SetNext(next) =>
          nextNode = Some(next)
          ctx.log.info(s"[$nodeId] Next node set")
          Behaviors.same
        
        case Token(value) =>
          hasToken = true
          ctx.log.info(s"[$nodeId] Received token (value: $value)")
          
          // Simulate critical section
          enterCriticalSection(ctx)
          
          // Pass token to next node
          scheduleTokenPass(ctx)
          Behaviors.same
        
        case CommonMessages.GetState(replyTo) =>
          val state = Map(
            "nodeId" -> nodeId,
            "hasToken" -> hasToken,
            "criticalSectionCount" -> criticalSectionCount
          )
          replyTo ! CommonMessages.StateResponse(nodeId, state)
          Behaviors.same
        
        case _ =>
          Behaviors.same
      }
    }
    
    private def enterCriticalSection(ctx: ActorContext[DistributedMessage]): Unit = {
      criticalSectionCount += 1
      ctx.log.info(s"[$nodeId] *** ENTERING CRITICAL SECTION (count: $criticalSectionCount) ***")
      
      // Simulate some work
      Thread.sleep(100 + random.nextInt(200))
      
      ctx.log.info(s"[$nodeId] Exiting critical section")
    }
    
    private def scheduleTokenPass(ctx: ActorContext[DistributedMessage]): Unit = {
      // Wait a bit before passing the token
      ctx.system.scheduler.scheduleOnce(
        scala.concurrent.duration.FiniteDuration(500 + random.nextInt(500), scala.concurrent.duration.MILLISECONDS),
        () => passToken(ctx)
      )(ctx.executionContext)
    }
    
    private def passToken(ctx: ActorContext[DistributedMessage]): Unit = {
      if (hasToken && nextNode.isDefined) {
        val tokenValue = criticalSectionCount
        ctx.log.info(s"[$nodeId] Passing token to next node (value: $tokenValue)")
        nextNode.get ! Token(tokenValue)
        hasToken = false
      }
    }
  }
}
