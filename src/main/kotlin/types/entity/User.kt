package org.example.types.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime


@Entity
@Table(name ="user")
data class User(
    @Id
    @Column(name = "ulid", length = 12, nullable = false)
    val ulid: String,

    @Column(name = "platform", nullable = false, length = 25)
    val platform: String? = null,

    @Column(name = "username", nullable = false, length = 50)
    val username: String,

    @Column(name = "access_token", length = 255)
    val accessToken: String? = null,

    @Column(name = "is_deleted", nullable = false)
    val isDeleted: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "user")
    val accounts: MutableList<Account> = mutableListOf()
) {
}