package com.shoutout.integration

import java.util.Locale

import com.netaporter.i18n.ResourceBundle
import com.relayrides.pushy.apns._
import com.relayrides.pushy.apns.util.{ApnsPayloadBuilder, TokenUtil, SimpleApnsPushNotification}
import com.shoutout.janitorial.JanitorConfig
import com.sun.org.apache.xml.internal.utils.LocaleUtility
import org.apache.commons.lang.LocaleUtils
import org.slf4j.LoggerFactory

import scala.slick.util.Logging
import scala.util.{Success, Try}


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


    val randomNumber = scala.util.Random
    val codes = List(
      "\ue415",
      "\ue057",
      "\ue404",
      "\ue011",
      "\ue052",
      "\ue048",
      "\ue531",
      "\ue050",
      "\ue051",
      "\ue528",
      "\ue109",
      "\ue11b",
      "\ue312",
      "\ue112",
      "\ue310",
      "\ue12b",
      "\ue120",
      "\ue33b",
      "\ue33a",
      "\ue34b",
      "\ue10d"
    )

  private def getLocaleFromString( string : String ): Locale = {
    try {
      LocaleUtils.toLocale( string )
    } catch {
      case e : Exception => Locale.ENGLISH
    }
  }

  def sendMessageToRecipients( recipients : List[(String, String)]) = {
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



      recipients foreach {
        recipient => {

          val randomUnicode = codes(randomNumber.nextInt(codes.size))
          val locale = getLocaleFromString(recipient._2)

          val resourceBundle = ResourceBundle("messages/messages")
          val message = resourceBundle.getWithParams("shoutout.pushNotification.unviewedPhotos", locale, randomUnicode)

          val payloadBuilder = new ApnsPayloadBuilder()
          payloadBuilder.setBadgeNumber(1)
          payloadBuilder.setAlertBody(message)
          payloadBuilder.setSoundFileName("default")

          val payload = payloadBuilder.buildWithDefaultMaximumLength()
          pushManager.getQueue.put(new SimpleApnsPushNotification(TokenUtil.tokenStringToByteArray(recipient._1), payload))
        }
      }
    }



}
