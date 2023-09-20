package com.example.e_finity

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uniqueID: String,
    val full_name: String,
    val phone_num: String,
    val role: String,
    var avatar: Boolean,
    val group: String,
    val score: Int
)

@Serializable
data class UserRead(
    val id: Int,
    val uniqueID: String,
    val full_name: String,
    val phone_num: String,
    val role: String,
    var avatar: Boolean,
    val group: String,
    val score: Int
)

@Serializable
data class GroupRead(
    val id: Int,
    val name: String,
    val color: String
)

@Serializable
data class Stats(
    val uniqueID: String,
    val Attack: Int,
    val HP: Int,
    val Defence: Int,
    val Accuracy: Int
)

@Serializable
data class StatsRead(
    val id: Int,
    val uniqueID: String,
    val Attack: Int,
    val HP: Int,
    val Defence: Int,
    val Accuracy: Int
)

@Serializable
data class GroupName(
    val name: String
)

@Serializable
data class GroupAdd(
    val name: String,
    val color: String
)