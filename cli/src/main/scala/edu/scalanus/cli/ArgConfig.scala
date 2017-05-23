package edu.scalanus.cli

import java.io.File

case class ArgConfig(
  file: Option[File] = None,
  showVersion: Boolean = false
)
