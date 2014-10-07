package com.shoutout.janitorial

import com.typesafe.config.ConfigFactory

/**
 * Created by aparrish on 8/29/14.
 */
trait JanitorConfig {

  lazy val cfg = ConfigFactory.load().getConfig("com.shoutout.janitorial")


  object DBConfiguration {

    val driver = dbCfg.getString("driver")
    val url = dbCfg.getString("url")
    val user = dbCfg.getString("user")
    val pass = dbCfg.getString("password")

    private def dbCfg = cfg.getConfig("db")

  }

  object S3Configuration {

    val accessKey = awsCfg.getString("accessKeyId")
    val secretKey = awsCfg.getString("secretKey")

    val profileBucket  = awsCfg.getString("profileBucket")
    val shoutoutBucket = awsCfg.getString("shoutoutBucket")

    private def awsCfg = cfg.getConfig("aws")

  }

  object JanitorSettings {

    val age = settingsCfg.getInt("age")

    private def settingsCfg = cfg.getConfig("settings")

  }

  object MandrillSettings {

    val apiKey = mandrill.getString("apiKey")
    val smtpHost = mandrill.getString("smtpHost")
    val smtpPort = mandrill.getString("smtpPort")
    val username = mandrill.getString("username")
    val recipients = mandrill.getStringList("recipients")

    private def mandrill = cfg.getConfig("mandrill")
  }

  object APNSSettings {

    val keyStorePassword = apnsSettings.getString("keyStorePassword")
    val connectionCount = apnsSettings.getString("connectionCount")
    val productionCertPath = apnsSettings.getString("productionCertPath")

    private def apnsSettings = cfg.getConfig("apple")
  }

}
