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
  case object EnterCriticalSection extends TokenMessage
  case object ExitCriticalSection extends TokenMessage
  case object PassTokenMessage extends TokenMessage
  
  class TokenRingNode(
    nodeId: String,
    hasInitialToken: Boolean = false
  ) extends BaseDistributedNode(nodeId) {
    
    private var nextNode: Option[ActorRef[DistributedMessage]] = None
    private var hasToken: Boolean = hasInitialToken
    private var criticalSectionCount: Int = 0
    private var inCriticalSection: Boolean = false
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
            ctx.self ! EnterCriticalSection
          }
          Behaviors.same
        
        case SetNext(next) =>
          nextNode = Some(next)
          ctx.log.info(s"[$nodeId] Next node set")
          Behaviors.same
        
        case Token(value) =>
          hasToken = true
          ctx.log.info(s"[$nodeId] Received token (value: $value)")
          
          // Schedule entry to critical section
          ctx.self ! EnterCriticalSection
          Behaviors.same
        
        case EnterCriticalSection =>
          if (hasToken && !inCriticalSection) {
            inCriticalSection = true
            criticalSectionCount += 1
            ctx.log.info(s"[$nodeId] *** ENTERING CRITICAL SECTION (count: $criticalSectionCount) ***")
            
            // Simulate work in critical section by scheduling exit
            import scala.concurrent.duration._
            val workDuration = (100 + random.nextInt(200)).milliseconds
            ctx.scheduleOnce(workDuration, ctx.self, ExitCriticalSection)
          }
          Behaviors.same
        
        case ExitCriticalSection =>
          if (inCriticalSection) {
            ctx.log.info(s"[$nodeId] Exiting critical section")
            inCriticalSection = false
            
            // Schedule token pass
            import scala.concurrent.duration._
            val passDuration = (500 + random.nextInt(500)).milliseconds
            ctx.scheduleOnce(passDuration, ctx.self, PassTokenMessage)
          }
          Behaviors.same
        
        case PassTokenMessage =>
          if (hasToken && nextNode.isDefined) {
            val tokenValue = criticalSectionCount
            ctx.log.info(s"[$nodeId] Passing token to next node (value: $tokenValue)")
            nextNode.get ! Token(tokenValue)
            hasToken = false
          }
          Behaviors.same
        
        case CommonMessages.GetState(replyTo) =>
          val state = Map(
            "nodeId" -> nodeId,
            "hasToken" -> hasToken,
            "criticalSectionCount" -> criticalSectionCount,
            "inCriticalSection" -> inCriticalSection
          )
          replyTo ! CommonMessages.StateResponse(nodeId, state)
          Behaviors.same
        
        case _ =>
          Behaviors.same
      }
    }
  }
}
