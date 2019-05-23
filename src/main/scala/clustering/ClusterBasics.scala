package clustering

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import com.typesafe.config.ConfigFactory

class ClusterSubscriber extends Actor with ActorLogging {
  val cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(
      self,
      initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent],
      classOf[UnreachableMember]
    )
  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = {
    case MemberJoined(member) =>
      log.info(s"New member is joined to cluster${member.address}")
    case MemberUp(member) =>
      log.info(s"New member is UP ${member.address}")
    case MemberRemoved(member, previousStatus) =>
      log.info(s"Poor ${member.address}, it was removed from previous status $previousStatus")
    case UnreachableMember(member) =>
      log.info(s"Member ${member.address} is unreachable")
    case m: MemberEvent =>
      log.info(s"Another member event $m")
  }
}

object ClusterBasics extends App {

  def startCluster(ports: List[Int]): Unit = ports.foreach { port =>
    val config = ConfigFactory.parseString(
      s"""
         |akka.remote.artery.canonical.port = $port
      """.stripMargin)
      .withFallback(ConfigFactory.load("clustering/clusteringBasics.conf"))

    val system = ActorSystem("BaseCluster", config)
    system.actorOf(Props[ClusterSubscriber], "clusterSubscriber")
  }

  startCluster(List(2551, 2552, 0))

}
