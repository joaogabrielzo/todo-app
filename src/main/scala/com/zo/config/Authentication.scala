package com.zo.config

import java.util.concurrent.TimeUnit

import authentikat.jwt._
import com.typesafe.config.ConfigFactory

abstract class Authentication {
    
    val expirationInDays = 1L
    val secretKey = ConfigFactory.load().getString("secret.secretKey")
    val header = JwtHeader("HS256")
    
    def setClaims(username: String, expiryPeriodInDays: Long): JwtClaimsSetMap = JwtClaimsSet(
        Map("user" -> username,
            "expiredAt" -> (System.currentTimeMillis() + TimeUnit.DAYS
                                                                 .toMillis(expiryPeriodInDays)))
    )
    
    def getClaims(jwt: String): Option[Map[String, String]] = jwt match {
        case JsonWebToken(_, claims, _) => claims.asSimpleMap.toOption
        case _                          => None
    }
    
    def isTokenExpired(jwt: String): Boolean = getClaims(jwt) match {
        case Some(claims) =>
            claims.get("expiredAt") match {
                case Some(value) => value.toLong < System.currentTimeMillis()
                case None        => false
            }
        case None         => false
    }
    
}
