package remoting

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import remoting.MasterApp.config
import remoting.WordCountDomain.Initialize

import scala.io.Source


object WordCountDomain {

  case class Initialize(nWorkers: Int)

  case class WordCountTask(text: String)

  case class WordCountResult(count: Int)

  case object EndWordCount

}

class WordCountWorker extends Actor with ActorLogging {

  import WordCountDomain._

  override def receive: Receive = {
    case WordCountTask(text) => {
      log.info("I'm processing: {}", text)
      sender() ! WordCountResult(text.split(" ").length)
    }
  }
}

class WordCountMaster extends Actor with ActorLogging {

  import WordCountDomain._

  override def receive: Receive = {
    case Initialize(nWorkers) =>

  }

  def online(workers: List[ActorRef], remainingTasks: Int, totalCount: Int): Receive = {
    case text: String =>
      val sentences = text.split("\\. ")
      Iterator.continually(workers).flatten.zip(sentences.iterator).foreach { pair =>
        val (worker, sentence) = pair
        worker ! WordCountTask(sentence)
      }

      context.become(online(workers, remainingTasks + sentences.length, totalCount))

    case WordCountResult(count) =>
      if (remainingTasks == 1) {
        log.info(s"Total result: ${totalCount + count}")
        context.stop(self)
      } else {
        context.become(online(workers, remainingTasks - 1, totalCount + count))
      }
  }
}


object MasterApp extends App {
  val config = ConfigFactory.parseString(
    """
      |akka.remote.artery.canonical.port = 2551
    """.stripMargin)
    .withFallback(ConfigFactory.load("remoting/remoteActorsExercise.conf"))

  val system = ActorSystem("MasterSystem", config)
  val master: ActorRef = system.actorOf(Props[WordCountMaster], "wordCountMaster")
  master ! Initialize(5)
  Thread.sleep(1000)

  Source.fromFile("src/main/resources/txt/lipsum.txt").getLines().foreach { line =>
    master ! line
  }
}

object WorkersApp extends App {
  val config = ConfigFactory.parseString(
    """
      |akka.remote.artery.canonical.port = 2552
    """.stripMargin)
    .withFallback(ConfigFactory.load("remoting/remoteActorsExercise.conf"))

  val system = ActorSystem("WorkersSystem", config)

  (1 to 5).map(i => system.actorOf(Props[WordCountWorker], s"wordCountWorker$i"))

}

  class RemoteActorsExercise {

}
