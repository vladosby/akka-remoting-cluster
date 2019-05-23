package clustering

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object ClusterBasics extends App {

  def startCluster(ports: List[Int]): Unit = ports.foreach { port =>
    val config = ConfigFactory.parseString(
      s"""
        |akka.remote.artery.canonical.port = $port
      """.stripMargin)
      .withFallback(ConfigFactory.load("clustering/clusteringBasics.conf"))

    ActorSystem("BaseCluster", config)
  }

  startCluster(List(2551, 2552, 0))

}
