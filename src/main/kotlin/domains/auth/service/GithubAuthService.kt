package org.example.domains.auth.service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.FormBody
import org.example.common.exception.CustomException
import org.example.common.exception.ErrorCode
import org.example.common.httpclient.CallClient
import org.example.common.json.JsonUtil
import org.example.config.OAuth2Config
import org.example.interfaces.OAuth2TokenResponse
import org.example.interfaces.OAuth2UserResponse
import org.example.interfaces.OAuthServiceInterface
import org.springframework.stereotype.Service

private const val key = "github"

@Service(key)
class GithubAuthService(
    private val config: OAuth2Config,
    private val httpClient: CallClient
): OAuthServiceInterface {
    private val oAuthInfo = config.providers[key] ?: throw CustomException(ErrorCode.AUTH_CONFIG_NOT_FOUND)
    private val tokenURL = "https://github.com/login/oauth/access_token"
    private val userInfoURL = "https://api.github.com/user"

    override val providerName: String = key

    override fun getToken(code: String): OAuth2TokenResponse {
        val body = FormBody.Builder()
            .add("code", code)
            .add("client_id", oAuthInfo.clientId)
            .add("client_secret", oAuthInfo.clientSecret)
            .add("redirect_uri", oAuthInfo.redirectUri)
            .add("grant_type", "authorization_code")
            .build()

        val headers = mapOf("Accept" to "application/json")

        val jsonString = httpClient.POST(tokenURL, headers, body)

        val response = JsonUtil.decodeFromJson(jsonString, GithubTokenResponse.serializer())

        return response
    }

    override fun getUserInfo(accessToken: String): OAuth2UserResponse {
        val headers = mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer $accessToken"
        )

        val jsonString = httpClient.GET(userInfoURL, headers)
        val tempResponse = JsonUtil.decodeFromJson(jsonString, GithubUserResponseTemp.serializer())
        return tempResponse.toOAuth2UserResponse()
    }
}

@Serializable
data class GithubTokenResponse(
    @SerialName("access_token") override val accessToken: String,
): OAuth2TokenResponse

@Serializable
data class GithubUserResponseTemp(
    val id: Int,
    @SerialName("repos_url")
    val email: String? = null,
    val name: String? = null,
) {
    fun toOAuth2UserResponse(): OAuth2UserResponse {
        return GithubUserResponse(
            id = id.toString(),
            name = name,
            email = email,
        )
    }
}

@Serializable
data class GithubUserResponse(
    override val id: String,
    override val email: String? = null,
    override val name: String? = null,
): OAuth2UserResponse