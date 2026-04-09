package com.example.myapplication.data.model

import com.google.firebase.firestore.DocumentSnapshot

data class StoreApplication(
    val applicationId: String,
    val applicantUserId: String,
    val applicantName: String,
    val applicantEmail: String,
    val storeName: String,
    val storeDescription: String,
    val storeLogoUrl: String,
    val status: String,
    val createdAtMs: Long,
    val reviewedAtMs: Long = 0L,
    val reviewedByUserId: String = "",
    val rejectionReason: String = "",
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_APPROVED = "approved"
        const val STATUS_REJECTED = "rejected"
    }
}

fun StoreApplication.toFirestoreMap(): Map<String, Any> = mapOf(
    FIELD_APPLICANT_USER_ID to applicantUserId,
    FIELD_APPLICANT_NAME to applicantName,
    FIELD_APPLICANT_EMAIL to applicantEmail,
    FIELD_STORE_NAME to storeName,
    FIELD_STORE_DESCRIPTION to storeDescription,
    FIELD_STORE_LOGO_URL to storeLogoUrl,
    FIELD_STATUS to status,
    FIELD_CREATED_AT to createdAtMs,
    FIELD_REVIEWED_AT to reviewedAtMs,
    FIELD_REVIEWED_BY_USER_ID to reviewedByUserId,
    FIELD_REJECTION_REASON to rejectionReason,
)

fun DocumentSnapshot.toStoreApplication(): StoreApplication? {
    if (!exists()) return null
    return StoreApplication(
        applicationId = id,
        applicantUserId = getString(FIELD_APPLICANT_USER_ID).orEmpty(),
        applicantName = getString(FIELD_APPLICANT_NAME).orEmpty(),
        applicantEmail = getString(FIELD_APPLICANT_EMAIL).orEmpty(),
        storeName = getString(FIELD_STORE_NAME).orEmpty(),
        storeDescription = getString(FIELD_STORE_DESCRIPTION).orEmpty(),
        storeLogoUrl = getString(FIELD_STORE_LOGO_URL).orEmpty(),
        status = getString(FIELD_STATUS) ?: StoreApplication.STATUS_PENDING,
        createdAtMs = readMillis(FIELD_CREATED_AT),
        reviewedAtMs = readMillis(FIELD_REVIEWED_AT),
        reviewedByUserId = getString(FIELD_REVIEWED_BY_USER_ID).orEmpty(),
        rejectionReason = getString(FIELD_REJECTION_REASON).orEmpty(),
    )
}

internal const val COLLECTION_STORE_APPLICATIONS = "storeApplications"

internal const val FIELD_APPLICANT_USER_ID = "applicantUserId"
internal const val FIELD_APPLICANT_NAME = "applicantName"
internal const val FIELD_APPLICANT_EMAIL = "applicantEmail"
internal const val FIELD_STORE_NAME = "storeName"
internal const val FIELD_STORE_DESCRIPTION = "storeDescription"
internal const val FIELD_STORE_LOGO_URL = "storeLogoUrl"
internal const val FIELD_STATUS = "status"
internal const val FIELD_CREATED_AT = "createdAt"
internal const val FIELD_REVIEWED_AT = "reviewedAt"
internal const val FIELD_REVIEWED_BY_USER_ID = "reviewedByUserId"
internal const val FIELD_REJECTION_REASON = "rejectionReason"
