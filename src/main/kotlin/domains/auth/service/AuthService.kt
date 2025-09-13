package org.example.domains.auth.service

import com.github.f4b6a3.ulid.UlidCreator
import org.example.common.exception.CustomException
import org.example.common.exception.ErrorCode
import org.example.common.jwt.JwtProvider
import org.example.common.logging.Logging
import org.example.common.transaction.Transactional
import org.example.domains.auth.repository.AuthUserRepository
import org.example.interfaces.OAuthServiceInterface
import org.example.types.entity.User
import org.slf4j.Logger
import org.springframework.stereotype.Service


@Service
class AuthService(
    private val oAuth2Services: Map<String, OAuthServiceInterface>,
    private val jwtProvider: JwtProvider,
    private val logger: Logger = Logging.getLogger(AuthService::class.java),
    private val transactional: Transactional,
    private val authUserRepository: AuthUserRepository
) {
    fun handleAuth(state: String, code: String): String = Logging.logFor(logger) { log ->
        val provider = state.lowercase()
        log["provider"] = provider

        val callService = oAuth2Services[provider] ?: throw CustomException(ErrorCode.PROVIDER_NOT_FOUND, "provider")

        val accessToken = callService.getToken(code)
        val userInfo = callService.getUserInfo(accessToken.accessToken)

        val token = jwtProvider.createToken(provider,userInfo.email, userInfo.name, userInfo.id)

        val username = (userInfo.name ?: userInfo.email).toString()

        transactional.run {
            val exist = authUserRepository.existsUserByUsernameAndPlatform(username, provider)

            if(exist) {
                authUserRepository.updateAccessTokenByUsername(username, token)
            } else {
                val ulid = UlidCreator.getUlid().toString()

                val user = User(
                    ulid = ulid,
                    username = username,
                    accessToken = token
                )
                authUserRepository.save(user)
            }
        }
        return@logFor token
    }

    fun verifyToken(authorization: String) {
        jwtProvider.verifyToken(authorization.removePrefix("Bearer "))
    }
}