package com.uic.cs553.distributed.algorithms

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import com.uic.cs553.distributed.framework._

/**
 * Simple Echo Algorithm
 * 
 * This is a basic distributed algorithm where:
 * 1. One node is designated as the initiator
 * 2. The initiator sends a message to all neighbors
 * 3. Each node that receives a message for the first time forwards it to all neighbors
 * 4. Each node sends back an echo once it has received echoes from all neighbors
 * 5. The initiator terminates when it receives echoes from all neighbors
 */
object EchoAlgorithm {
  
  sealed trait EchoMessage extends DistributedMessage
  case class Wave(initiator: String, waveId: Int, sender: ActorRef[DistributedMessage]) extends EchoMessage
  case class Echo(from: String, waveId: Int, sender: ActorRef[DistributedMessage]) extends EchoMessage
  
  class EchoNode(
    nodeId: String,
    isInitiator: Boolean = false
  ) extends BaseDistributedNode(nodeId) {
    
    private var parent: Option[ActorRef[DistributedMessage]] = None
    private var receivedEchoes: Set[ActorRef[DistributedMessage]] = Set.empty
    private var waveReceived: Boolean = false
    private var currentWaveId: Int = 0
    
    override protected def onMessage(
      ctx: ActorContext[DistributedMessage],
      msg: DistributedMessage
    ): Behavior[DistributedMessage] = {
      msg match {
        case CommonMessages.Start() =>
          if (isInitiator) {
            ctx.log.info(s"[$nodeId] Initiating echo wave")
            currentWaveId += 1
            broadcast(Wave(nodeId, currentWaveId, ctx.self))
            if (peers.isEmpty) {
              ctx.log.info(s"[$nodeId] No peers - algorithm complete")
            }
          }
          Behaviors.same
        
        case Wave(initiator, waveId, sender) =>
          if (!waveReceived) {
            waveReceived = true
            currentWaveId = waveId
            parent = Some(sender)
            ctx.log.info(s"[$nodeId] Received wave from $initiator, forwarding to neighbors")
            
            // Forward to all neighbors except parent
            peers.filterNot(_ == sender).foreach(_ ! Wave(initiator, waveId, ctx.self))
            
            // If no children, send echo immediately
            if (peers.size <= 1) {
              sendEcho(ctx)
            }
          }
          Behaviors.same
        
        case Echo(from, waveId, sender) if waveId == currentWaveId =>
          receivedEchoes = receivedEchoes + sender
          ctx.log.info(s"[$nodeId] Received echo from $from (${receivedEchoes.size}/${peers.size - 1})")
          
          // If received echoes from all children, send echo to parent
          if (receivedEchoes.size == peers.size - 1 && parent.isDefined) {
            sendEcho(ctx)
          } else if (receivedEchoes.size == peers.size && isInitiator) {
            ctx.log.info(s"[$nodeId] ALGORITHM COMPLETE - Received all echoes")
          }
          Behaviors.same
        
        case CommonMessages.GetState(replyTo) =>
          val state = Map(
            "nodeId" -> nodeId,
            "isInitiator" -> isInitiator,
            "waveReceived" -> waveReceived,
            "echoesReceived" -> receivedEchoes.size,
            "totalPeers" -> peers.size
          )
          replyTo ! CommonMessages.StateResponse(nodeId, state)
          Behaviors.same
        
        case _ =>
          Behaviors.same
      }
    }
    
    private def sendEcho(ctx: ActorContext[DistributedMessage]): Unit = {
      parent.foreach { p =>
        ctx.log.info(s"[$nodeId] Sending echo to parent")
        p ! Echo(nodeId, currentWaveId, ctx.self)
      }
    }
  }
}
