package org.example.domains.bank.controller

import org.example.domains.bank.service.BankService
import org.example.types.dto.Response
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal


@RestController
@RequestMapping("/api/v1/bank")
class BankController(
    private val bankService: BankService,
){

    @PostMapping("/create/{ulid}")
    fun createAccount(
        @PathVariable("ulid", required = true) ulid: String,
    ) : ResponseEntity<Response<String>> {
        return ResponseEntity.ok(bankService.createAccount(ulid))
    }

    @GetMapping("/balance/{user_ulid}/{account_ulid}")
    fun balance(
        @PathVariable("user_ulid", required = true) userUlid: String,
        @PathVariable("account_ulid", required = true) accountUlid: String,
    ) : ResponseEntity<Response<BigDecimal>> {
        return ResponseEntity.ok(bankService.balance(userUlid, accountUlid))
    }

    @PostMapping("/remove/{user_ulid}/{account_ulid}")
    fun removeAccount(
        @PathVariable("user_ulid", required = true) userUlid: String,
        @PathVariable("account_ulid", required = true) accountUlid: String,
    ) : ResponseEntity<Response<String>> {
        return ResponseEntity.ok(bankService.removeAccount(userUlid, accountUlid))
    }

}