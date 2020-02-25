package com.knoldus

import java.io.File

class ReadDirectory {
  def getListOfFile(pathName: String): List[File] = {

    val file = new File(pathName)
    if (file.isDirectory) {
      file.listFiles.toList
    } else {
      List[File]()
    }
  }


}
