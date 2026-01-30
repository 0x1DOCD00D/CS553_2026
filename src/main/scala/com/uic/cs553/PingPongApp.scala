package com.uic.cs553

import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.slf4j.LoggerFactory
import scala.concurrent.duration._

/**
 * Ping-Pong Actor System demonstrating message exchange with Apache Pekko (Akka fork) Typed
 * Note: This implementation uses Apache Pekko, which is API-compatible with Akka.
 * For Akka with Cinnamon instrumentation, see the commented build.sbt configuration.
 */

// Message protocol
sealed trait Message
case class Ping(replyTo: ActorRef[Message]) extends Message
case class Pong(replyTo: ActorRef[Message]) extends Message
case object Start extends Message
case object Stop extends Message

object PingActor {
  def apply(pongActor: ActorRef[Message], maxRounds: Int): Behavior[Message] = 
    active(pongActor, maxRounds, 0)
  
  private def active(pongActor: ActorRef[Message], maxRounds: Int, count: Int): Behavior[Message] = {
    val logger = LoggerFactory.getLogger("PingActor")
    
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case Start =>
          logger.info(s"PingActor: Starting ping-pong game with max $maxRounds rounds")
          val newCount = count + 1
          logger.info(s"PingActor: Sending Ping #$newCount")
          pongActor ! Ping(context.self)
          active(pongActor, maxRounds, newCount)
          
        case Pong(replyTo) =>
          val newCount = count + 1
          logger.info(s"PingActor: Received Pong, sending Ping #$newCount")
          
          if (newCount >= maxRounds) {
            logger.info(s"PingActor: Reached max rounds ($maxRounds), stopping")
            replyTo ! Stop
            Behaviors.stopped
          } else {
            // Use scheduler instead of Thread.sleep to avoid blocking dispatcher
            context.scheduleOnce(100.millis, replyTo, Ping(context.self))
            active(pongActor, maxRounds, newCount)
          }
          
        case Stop =>
          logger.info("PingActor: Received Stop signal")
          Behaviors.stopped
          
        case _ =>
          logger.warn("PingActor: Received unexpected message")
          active(pongActor, maxRounds, count)
      }
    }
  }
}

object PongActor {
  def apply(): Behavior[Message] = active(0)
  
  private def active(count: Int): Behavior[Message] = {
    val logger = LoggerFactory.getLogger("PongActor")
    
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case Ping(replyTo) =>
          val newCount = count + 1
          logger.info(s"PongActor: Received Ping, sending Pong #$newCount")
          // Use scheduler instead of Thread.sleep to avoid blocking dispatcher
          context.scheduleOnce(100.millis, replyTo, Pong(context.self))
          active(newCount)
          
        case Stop =>
          logger.info("PongActor: Received Stop signal")
          Behaviors.stopped
          
        case _ =>
          logger.warn("PongActor: Received unexpected message")
          active(count)
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
    logger.info("=== Starting Ping-Pong Actor System (Apache Pekko) ===")
    
    val system: ActorSystem[Message] = ActorSystem(PingPongApp(), "ping-pong-system")
    
    // Start the game
    system ! Start
    
    // Wait for system to complete gracefully
    // Note: Increase this timeout if you increase the number of rounds or message delay
    Thread.sleep(5000)
    
    logger.info("=== Shutting down Ping-Pong Actor System ===")
    system.terminate()
  }
}
