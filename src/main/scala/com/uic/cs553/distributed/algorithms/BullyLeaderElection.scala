package com.uic.cs553.distributed.algorithms

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import com.uic.cs553.distributed.framework._

/**
 * Bully Algorithm for Leader Election
 * 
 * In the Bully algorithm:
 * 1. Each node has a unique ID (higher ID has higher priority)
 * 2. Any node can start an election
 * 3. A node sends election messages to all nodes with higher IDs
 * 4. If a node receives an election message, it responds with "alive" and starts its own election
 * 5. If a node receives no response, it declares itself the leader
 * 6. The node with the highest ID becomes the leader
 */
object BullyLeaderElection {
  
  sealed trait LeaderMessage extends DistributedMessage
  case class Election(from: String, fromId: Int) extends LeaderMessage
  case class Alive(from: String) extends LeaderMessage
  case class Victory(leaderId: String, leaderIdNum: Int) extends LeaderMessage
  
  class BullyNode(
    nodeId: String,
    val nodeIdNum: Int
  ) extends BaseDistributedNode(nodeId) {
    
    private var currentLeader: Option[String] = None
    private var electionInProgress: Boolean = false
    private var responsesReceived: Set[String] = Set.empty
    
    override protected def onMessage(
      ctx: ActorContext[DistributedMessage],
      msg: DistributedMessage
    ): Behavior[DistributedMessage] = {
      msg match {
        case CommonMessages.Start() =>
          ctx.log.info(s"[$nodeId] Starting leader election")
          startElection(ctx)
          Behaviors.same
        
        case Election(from, fromId) =>
          ctx.log.info(s"[$nodeId] Received election request from $from (ID: $fromId)")
          
          if (fromId < nodeIdNum) {
            // Send alive message
            ctx.sender() ! Alive(nodeId)
            
            // Start own election if not already in progress
            if (!electionInProgress) {
              startElection(ctx)
            }
          }
          Behaviors.same
        
        case Alive(from) =>
          ctx.log.info(s"[$nodeId] Received alive message from $from")
          responsesReceived = responsesReceived + from
          Behaviors.same
        
        case Victory(leaderId, leaderIdNum) =>
          currentLeader = Some(leaderId)
          electionInProgress = false
          ctx.log.info(s"[$nodeId] Acknowledged new leader: $leaderId (ID: $leaderIdNum)")
          Behaviors.same
        
        case CommonMessages.GetState(replyTo) =>
          val state = Map(
            "nodeId" -> nodeId,
            "nodeIdNum" -> nodeIdNum,
            "currentLeader" -> currentLeader.getOrElse("None"),
            "electionInProgress" -> electionInProgress
          )
          replyTo ! CommonMessages.StateResponse(nodeId, state)
          Behaviors.same
        
        case _ =>
          Behaviors.same
      }
    }
    
    private def startElection(ctx: ActorContext[DistributedMessage]): Unit = {
      electionInProgress = true
      responsesReceived = Set.empty
      
      ctx.log.info(s"[$nodeId] Starting election with ID $nodeIdNum")
      
      // Send election to all nodes (in a real implementation, would only send to higher IDs)
      broadcast(Election(nodeId, nodeIdNum))
      
      // Schedule a check to see if we won (simplified - in production use timers)
      ctx.scheduleOnce(
        scala.concurrent.duration.FiniteDuration(2, scala.concurrent.duration.SECONDS),
        ctx.self,
        new DistributedMessage {
          override def toString: String = "CheckElectionResult"
        }
      )
      
      // Check if we should declare victory
      ctx.system.scheduler.scheduleOnce(
        scala.concurrent.duration.FiniteDuration(2, scala.concurrent.duration.SECONDS),
        () => checkElectionResult(ctx)
      )(ctx.executionContext)
    }
    
    private def checkElectionResult(ctx: ActorContext[DistributedMessage]): Unit = {
      if (electionInProgress && responsesReceived.isEmpty) {
        // No responses, we are the leader
        currentLeader = Some(nodeId)
        electionInProgress = false
        ctx.log.info(s"[$nodeId] *** ELECTED AS LEADER (ID: $nodeIdNum) ***")
        broadcast(Victory(nodeId, nodeIdNum))
      }
    }
  }
}
