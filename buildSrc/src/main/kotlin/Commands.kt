import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.TimeZone
import java.util.Date

fun Project.getCommitCount(): String {
  return runCommand("git rev-list --count HEAD")
}

fun Project.getGitSha(): String {
  return runCommand("git rev-parse --short HEAD")
}

fun Project.getBuildTime(): String {
  val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
  df.timeZone = TimeZone.getTimeZone("UTC")
  return df.format(Date())
}

fun Project.runCommand(command: String): String {
  val byteOut = ByteArrayOutputStream()
  project.exec {
    commandLine = command.split(" ")
    standardOutput = byteOut
  }
  return String(byteOut.toByteArray()).trim()
}
