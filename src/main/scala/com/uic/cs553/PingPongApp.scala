package com.uic.cs553

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import org.slf4j.LoggerFactory

/**
 * Ping-Pong Actor System demonstrating message exchange with Akka Typed and Cinnamon instrumentation
 */

// Message protocol
sealed trait Message
case class Ping(replyTo: ActorRef[Message]) extends Message
case class Pong(replyTo: ActorRef[Message]) extends Message
case object Start extends Message
case object Stop extends Message

object PingActor {
  def apply(pongActor: ActorRef[Message], maxRounds: Int): Behavior[Message] = {
    val logger = LoggerFactory.getLogger("PingActor")
    
    Behaviors.setup { context =>
      var count = 0
      
      Behaviors.receiveMessage {
        case Start =>
          logger.info(s"PingActor: Starting ping-pong game with max $maxRounds rounds")
          count += 1
          logger.info(s"PingActor: Sending Ping #$count")
          pongActor ! Ping(context.self)
          Behaviors.same
          
        case Pong(replyTo) =>
          count += 1
          logger.info(s"PingActor: Received Pong, sending Ping #$count")
          
          if (count >= maxRounds) {
            logger.info(s"PingActor: Reached max rounds ($maxRounds), stopping")
            replyTo ! Stop
            Behaviors.stopped
          } else {
            Thread.sleep(100) // Small delay to simulate processing
            replyTo ! Ping(context.self)
            Behaviors.same
          }
          
        case Stop =>
          logger.info("PingActor: Received Stop signal")
          Behaviors.stopped
          
        case _ =>
          logger.warn("PingActor: Received unexpected message")
          Behaviors.same
      }
    }
  }
}

object PongActor {
  def apply(): Behavior[Message] = {
    val logger = LoggerFactory.getLogger("PongActor")
    
    Behaviors.setup { context =>
      var count = 0
      
      Behaviors.receiveMessage {
        case Ping(replyTo) =>
          count += 1
          logger.info(s"PongActor: Received Ping, sending Pong #$count")
          Thread.sleep(100) // Small delay to simulate processing
          replyTo ! Pong(context.self)
          Behaviors.same
          
        case Stop =>
          logger.info("PongActor: Received Stop signal")
          Behaviors.stopped
          
        case _ =>
          logger.warn("PongActor: Received unexpected message")
          Behaviors.same
      }
    }
  }
}

object PingPongApp {
  def apply(): Behavior[Message] = {
    val logger = LoggerFactory.getLogger("PingPongApp")
    
    Behaviors.setup { context =>
      logger.info("Setting up PingPong application")
      
      // Create actors
      val pongActor = context.spawn(PongActor(), "pong-actor")
      val pingActor = context.spawn(PingActor(pongActor, 10), "ping-actor")
      
      Behaviors.receiveMessage {
        case Start =>
          logger.info("Starting ping-pong game")
          pingActor ! Start
          Behaviors.same
          
        case Stop =>
          logger.info("Application stopping")
          Behaviors.stopped
          
        case _ =>
          Behaviors.same
      }
    }
  }
  
  def main(args: Array[String]): Unit = {
    val logger = LoggerFactory.getLogger("Main")
    logger.info("=== Starting Ping-Pong Actor System with Cinnamon Instrumentation ===")
    
    val system: ActorSystem[Message] = ActorSystem(PingPongApp(), "ping-pong-system")
    
    // Start the game
    system ! Start
    
    // Wait for completion
    Thread.sleep(5000)
    
    logger.info("=== Shutting down Ping-Pong Actor System ===")
    system.terminate()
  }
}
