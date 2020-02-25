package com.knoldus

import akka.actor.Actor
import akka.pattern.pipe
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

/**
 * The class LogActor provides Actor functionality.
 */
class LogActor extends Actor {
  var numOfErrors = 0
  var numOfWarnings = 0
  var numOfInfo = 0

  override def receive: Receive = {
    case fileName(file) =>
      val fSource = Source.fromFile(s"$file")
      val listOfLines = fSource.getLines().toList
      Future(finder(listOfLines)).pipeTo(sender())
  }

  /**
   * finder function finds Errors,Warnings and Information from each line of each file
   * @param listOfLines - a list of Lines from input file
   * @return - an object of newDatatructure case class containing counts of Errors,Warnings and Information
   */
  def finder(listOfLines: List[String]): CountItems = {
    listOfLines match {
      case Nil => CountItems(numOfErrors, numOfWarnings, numOfInfo)
      case head :: rest if head.contains("[ERROR]") => numOfErrors += 1; finder(rest)
      case head :: rest if head.contains("[WARN]") => numOfWarnings += 1; finder(rest)
      case head :: rest if head.contains("[INFO]") => numOfInfo += 1; finder(rest)
      case _ :: rest => finder(rest)
    }
  }

}
