package com.gaaocorp.taskflow.domain.model

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val priority: Priority,
    val createdAt: Long,
    val updatedAt: Long
)

enum class Priority { LOW, MEDIUM, HIGH }
