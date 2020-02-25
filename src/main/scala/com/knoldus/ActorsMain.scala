package com.knoldus

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

object Constants {
  val child = 5
}

object ActorsMain extends App {

  val path = "/home/knoldus/Music"
  val rd = new ReadDirectory
  val listOfFiles = rd.getListOfFile(path).map(_.toString)

  val actorSystem = ActorSystem("First-Actor-System")
  val myActor = actorSystem.actorOf(RoundRobinPool(Constants.child).props(Props[LogActor]).withDispatcher("fixed-thread-pool"))
  val x = getFutureOfCountItems(listOfFiles, myActor, List())
  val almostFinal = Future.sequence(x).map(an => an.foldLeft(CountItems(0, 0, 0)) { (acc, y) => caseClassMembersAddition(acc, y) })
  val finalResult = Await.result(almostFinal, 10 second)
  println(finalResult)

  val avgResult = calcAverage(finalResult,listOfFiles.length)
  println(avgResult)

  /**
   * getFutureOfCountItems function returns a list that contains all case class objects with future wrapper.
   *
   * @param files          - a list of files from a directory
   * @param actorReference - a list of actor references.
   * @param futureLst      - a list containing futures of case class objects (initially empty).
   * @return - list of future of case class objects
   */
  @scala.annotation.tailrec
  def getFutureOfCountItems(files: List[String], actorReference: ActorRef, futureLst: List[Future[CountItems]]): List[Future[CountItems]] = {
    implicit val timeout: Timeout = Timeout(5 second)
    files match {
      case Nil => futureLst
      case head :: rest =>
        val temp = (actorReference ? fileName(head)).mapTo[CountItems]
        getFutureOfCountItems(rest, actorReference, temp :: futureLst)
    }
  }

  /**
   * caseClassMembersAddition function performs addition of member's values on two case class objects
   *
   * @param acc - first case class object
   * @param y   - second case class object
   * @return - case class object after addition
   */
  def caseClassMembersAddition(acc: CountItems, y: CountItems): CountItems = {
    CountItems(acc.countError + y.countError, acc.countWarnings + y.countWarnings, acc.countInfo + y.countInfo)
  }

  def calcAverage(items: CountItems, length: Int) : List[Double] = {
    val err = items.countError / length
    val warn = items.countWarnings / length
    val info = items.countInfo / length
    List(err,warn,info)
  }
}
