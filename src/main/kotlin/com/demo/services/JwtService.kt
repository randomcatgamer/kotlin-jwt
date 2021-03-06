package com.demo.services

import com.demo.domain.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.annotation.PostConstruct
import javax.crypto.SecretKey

@Service
class JwtService {

    @Value("\${jwt.expiration}")
    val expiration: Int = 600 // 10 minutes

    @Value("\${jwt.secret}")
    private val secret: String = ""

    lateinit var secretKey: SecretKey

    @PostConstruct
    fun init() {
        secretKey = Keys.hmacShaKeyFor(secret.toByteArray())
    }

    // consider using different jwt per different endpoint, each with his own secret key and exp time
    fun create(user: User): String {

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, expiration)

        return Jwts.builder()
                .setSubject(user.id.toString())
                .setIssuedAt(Date()) // consider using this instead of (or with) exp, and validate manually (iat + 10 min < now )
                .setExpiration(calendar.time)
                .setNotBefore(Date())
                .signWith(secretKey)
                .compact()
    }

    fun getJwtClaims(authorizationHeader: String): Jws<Claims> {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(getJwtFromHeader(authorizationHeader))
    }

    private fun getJwtFromHeader(authorizationHeader: String): String {
        val jwt = authorizationHeader.split("Bearer ")
        return if (jwt.size == 2)
            jwt[1]
        else
            throw MalformedJwtException("Header is not a valid jwt!")
    }
}