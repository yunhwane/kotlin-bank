package org.example.common.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.AlgorithmMismatchException
import com.auth0.jwt.exceptions.InvalidClaimException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import org.example.common.exception.CustomException
import org.example.common.exception.ErrorCode
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.SignatureException
import java.util.Date


@Component
class JwtProvider(
    @Value("\${jwt.secret-key}") private val secretKey: String,
    @Value("\${jwt.expire-ms}")  private val expirationMs: Long
) {

    private val ONE_MINUTE_TO_MILLIS = 60 * 1000L

    fun createToken(platform: String, email: String?, name: String?, id: String): String {
        return JWT.create()
            .withSubject("$platform-$id-$email-$name")
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + expirationMs * ONE_MINUTE_TO_MILLIS))
            .sign(Algorithm.HMAC256(secretKey))
    }

    fun verifyToken(token: String): DecodedJWT {
        try {
            return JWT.require(Algorithm.HMAC256(secretKey)).build().verify(token)
        } catch (e: AlgorithmMismatchException) {
            throw CustomException(ErrorCode.TOKEN_IS_INVALID)
        } catch (e: SignatureException) {
            throw CustomException(ErrorCode.TOKEN_IS_INVALID)
        } catch (e: InvalidClaimException) {
            throw CustomException(ErrorCode.TOKEN_IS_INVALID)
        } catch (e: TokenExpiredException) {
            throw CustomException(ErrorCode.TOKEN_EXPIRED)
        }
    }
}