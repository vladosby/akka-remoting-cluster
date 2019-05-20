package remoting

import akka.actor.{Actor, ActorLogging}

class SimpleActor extends Actor with ActorLogging{
  override def receive ={
    case m =>
      log.info(s"Received0 $m from ${sender()}")
  }
}
