package org.example.domains.auth.controller

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.example.domains.auth.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.net.URL


@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {

    @GetMapping("/callback")
    fun callback(
        @RequestParam("code", required = true) code: String,
        @RequestParam("state", required = true) state: String,
        response: HttpServletResponse,
    ): ResponseEntity<Void> {
        val token = authService.handleAuth(state, code)

        response.addCookie(
            Cookie("authToken", token).apply {
                isHttpOnly = true
                path = "/callback"
                maxAge = 60 * 60 * 24 * 7
            }
        )

        // TODO -> create url 을 config 로 관리
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create("http://localhost:3000"))
            .build()
    }

    @GetMapping("/verify-token")
    fun verifyToken(
        @RequestHeader("Authorization", required = true) token: String,
    ) {
        authService.verifyToken(token)
    }
}