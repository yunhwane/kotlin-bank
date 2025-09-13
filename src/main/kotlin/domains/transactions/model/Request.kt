package org.example.domains.transactions.model

import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

data class DepositRequest(
    @field:NotBlank(message = "Account ULID is required")
    val toAccountId: String,

    @field:NotBlank(message = "ulid is required")
    val toUlid: String,

    @field:NotBlank(message = "Value is required")
    val value: BigDecimal
)


data class TransferRequest(

    @field:NotBlank(message = "from Account ULID is required")
    val fromAccountId: String,

    @field:NotBlank(message = "to Account ULID is required")
    val toAccountId: String,

    @field:NotBlank(message = "from ulid is required")
    val fromUlid: String,

    @field:NotBlank(message = "Value is required")
    val value: BigDecimal
)