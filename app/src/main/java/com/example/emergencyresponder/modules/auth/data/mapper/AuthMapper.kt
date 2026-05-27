package com.example.emergencyresponder.modules.auth.data.mapper

import com.example.emergencyresponder.core.domain.model.EmergencyContact
import com.example.emergencyresponder.modules.auth.data.model.AuthenticatedUser as DataAuthenticatedUser
import com.example.emergencyresponder.modules.auth.data.model.EmergencyContact as DataEmergencyContact
import com.example.emergencyresponder.modules.auth.data.model.User as DataUser
import com.example.emergencyresponder.modules.auth.domain.model.AuthenticatedUser
import com.example.emergencyresponder.modules.auth.domain.model.User

/**
 * Maps between auth data models and domain entities.
 * Keeps domain independent of data layer.
 */
object AuthMapper {

    fun DataAuthenticatedUser.toDomain(): AuthenticatedUser =
        AuthenticatedUser(uid = uid, name = name, email = email)

    fun AuthenticatedUser.toData(): DataAuthenticatedUser =
        DataAuthenticatedUser(uid = uid, name = name, email = email)

    fun DataUser.toDomain(): User =
        User(
            uid = uid,
            name = name,
            email = email,
            emergencyContacts = emergencyContacts.map { it.toDomain() }
        )

    fun User.toData(): DataUser =
        DataUser(
            uid = uid,
            name = name,
            email = email,
            emergencyContacts = emergencyContacts.map { it.toData() }
        )

    fun DataEmergencyContact.toDomain(): EmergencyContact =
        EmergencyContact(name = name, phone = phone)

    fun EmergencyContact.toData(): DataEmergencyContact =
        DataEmergencyContact(name = name, phone = phone)
}
