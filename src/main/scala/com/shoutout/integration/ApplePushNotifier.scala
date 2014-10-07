package com.shoutout.integration

import com.relayrides.pushy.apns._
import com.relayrides.pushy.apns.util.{ApnsPayloadBuilder, TokenUtil, SimpleApnsPushNotification}
import com.shoutout.janitorial.JanitorConfig
import org.slf4j.LoggerFactory

import scala.slick.util.Logging


/**
 * Created by aparrish on 10/6/14.
 */
class AppleAPNSRejectListener extends RejectedNotificationListener[SimpleApnsPushNotification] {

  val log = LoggerFactory.getLogger(this.getClass)

  override def handleRejectedNotification( pushManager : PushManager[_ <: SimpleApnsPushNotification],
                                           notification : SimpleApnsPushNotification,
                                           reason : RejectedNotificationReason) : Unit = {
    log.trace(s"received rejection notification")
    log.trace(s"notification: $notification")
    log.trace(s"reason: $reason")
  }

}

class ApplePushNotifier extends JanitorConfig {


  def sendMessageToRecipients( recipients : List[String], message : String ) = {
    def readPem(location : String) = {
      this.getClass.getClassLoader.getResourceAsStream(location)
    }

    val keystoreInputStream = readPem(APNSSettings.productionCertPath)
    val keyStore = java.security.KeyStore.getInstance("PKCS12")

    keyStore.load(keystoreInputStream, APNSSettings.keyStorePassword.toCharArray)

    val pushManagerFactory = new PushManagerFactory[SimpleApnsPushNotification](
      ApnsEnvironment.getProductionEnvironment,
      PushManagerFactory.createDefaultSSLContext(
        keyStore,
        APNSSettings.keyStorePassword.toCharArray)
    )

    val pushManager = pushManagerFactory.buildPushManager()
    pushManager.registerRejectedNotificationListener(new AppleAPNSRejectListener)
    pushManager.start()
    keystoreInputStream.close()

    val payloadBuilder = new ApnsPayloadBuilder()
    payloadBuilder.setBadgeNumber(1)
    payloadBuilder.setAlertBody(message)
    payloadBuilder.setSoundFileName("default")

    val payload = payloadBuilder.buildWithDefaultMaximumLength()


    recipients foreach {
      recipient => {
        println(s"Sending message $message to $recipient")
        pushManager.getQueue.put(new SimpleApnsPushNotification(TokenUtil.tokenStringToByteArray(recipient), payload))
      }
    }
  }


}
