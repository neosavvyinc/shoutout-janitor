package com.shoutout.db

import java.util.UUID

import org.joda.time.LocalDate

case class ShoutoutUser(id : Option[Long],
                        profilePictureUrl : Option[String])

