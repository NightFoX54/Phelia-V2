package com.example.myapplication.data.repository

import com.example.myapplication.data.CardPanUtils
import com.example.myapplication.data.model.PaymentMethodDoc
import com.example.myapplication.data.model.ShippingAddressDoc
import com.example.myapplication.data.model.readMillis
import com.example.myapplication.data.remote.FirebaseRemoteDataSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

data class ShippingAddressInput(
    val label: String,
    val fullName: String,
    val phone: String,
    val line1: String,
    val line2: String,
    val district: String,
    val city: String,
    val postalCode: String,
    val country: String,
    val isDefault: Boolean,
)

/**
 * @param panDigits Tam kart numarası yalnızca rakamlar; depoda tutulmaz, masked + last4 üretilir.
 * Düzenlemede boş bırakılırsa mevcut kart bilgisi korunur.
 */
data class PaymentMethodInput(
    val label: String,
    val type: String,
    val brand: String,
    val holderName: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val isDefault: Boolean,
    val panDigits: String,
)

class UserAccountRepository(
    private val db: FirebaseFirestore = FirebaseRemoteDataSource.firestore,
) {

    fun listenShippingAddresses(
        userId: String,
        onUpdate: (List<ShippingAddressDoc>) -> Unit,
    ): ListenerRegistration =
        db.collection(USERS).document(userId).collection(SHIPPING_ADDRESS)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.toShippingAddressDoc() }
                    ?.sortedWith(compareBy({ !it.isDefault }, { -it.createdAtMs })) ?: emptyList()
                onUpdate(list)
            }

    fun listenPaymentMethods(
        userId: String,
        onUpdate: (List<PaymentMethodDoc>) -> Unit,
    ): ListenerRegistration =
        db.collection(USERS).document(userId).collection(PAYMENT_METHODS)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.toPaymentMethodDoc() }
                    ?.sortedWith(compareBy({ !it.isDefault }, { -it.createdAtMs })) ?: emptyList()
                onUpdate(list)
            }

    suspend fun saveShippingAddress(
        userId: String,
        existingAddressId: String?,
        input: ShippingAddressInput,
    ): Result<String> = runCatching {
        val col = db.collection(USERS).document(userId).collection(SHIPPING_ADDRESS)
        val ref = if (existingAddressId.isNullOrBlank()) col.document() else col.document(existingAddressId)
        val id = ref.id
        val data = mutableMapOf<String, Any>(
            "addressId" to id,
            "label" to input.label.trim(),
            "fullName" to input.fullName.trim(),
            "phone" to input.phone.trim(),
            "line1" to input.line1.trim(),
            "line2" to input.line2.trim(),
            "district" to input.district.trim(),
            "city" to input.city.trim(),
            "postalCode" to input.postalCode.trim(),
            "country" to input.country.trim().ifBlank { "TR" },
            "isDefault" to input.isDefault,
        )
        if (existingAddressId.isNullOrBlank()) {
            data["createdAt"] = FieldValue.serverTimestamp()
        }
        ref.set(data, SetOptions.merge()).await()
        if (input.isDefault) {
            clearOtherDefaultsShipping(userId, keepId = id)
        }
        id
    }

    suspend fun deleteShippingAddress(userId: String, addressId: String): Result<Unit> = runCatching {
        db.collection(USERS).document(userId).collection(SHIPPING_ADDRESS).document(addressId).delete().await()
    }

    suspend fun setDefaultShippingAddress(userId: String, addressId: String): Result<Unit> = runCatching {
        clearOtherDefaultsShipping(userId, keepId = addressId)
        db.collection(USERS).document(userId).collection(SHIPPING_ADDRESS).document(addressId)
            .update("isDefault", true).await()
    }

    private suspend fun clearOtherDefaultsShipping(userId: String, keepId: String) {
        val snap = db.collection(USERS).document(userId).collection(SHIPPING_ADDRESS).get().await()
        val batch = db.batch()
        snap.documents.forEach { doc ->
            if (doc.id != keepId && doc.getBoolean("isDefault") == true) {
                batch.update(doc.reference, "isDefault", false)
            }
        }
        batch.commit().await()
    }

    suspend fun savePaymentMethod(
        userId: String,
        existingPaymentMethodId: String?,
        input: PaymentMethodInput,
    ): Result<String> = runCatching {
        val col = db.collection(USERS).document(userId).collection(PAYMENT_METHODS)
        val ref = if (existingPaymentMethodId.isNullOrBlank()) col.document() else col.document(existingPaymentMethodId)
        val id = ref.id
        val panDigits = CardPanUtils.digitsOnly(input.panDigits)
        val existingSnap =
            if (!existingPaymentMethodId.isNullOrBlank()) ref.get().await() else null
        val (last4, maskedPan, brandOut) = when {
            panDigits.length >= CardPanUtils.MIN_PAN_LENGTH -> {
                if (!CardPanUtils.luhnCheck(panDigits)) {
                    throw IllegalArgumentException("Invalid card number")
                }
                Triple(
                    CardPanUtils.last4(panDigits),
                    CardPanUtils.maskedDisplay(panDigits),
                    input.brand.trim().ifBlank { CardPanUtils.inferBrand(panDigits) },
                )
            }
            existingSnap != null && existingSnap.exists() -> {
                Triple(
                    existingSnap.getString("last4").orEmpty(),
                    existingSnap.getString("maskedPan").orEmpty(),
                    input.brand.trim().ifBlank { existingSnap.getString("brand").orEmpty() },
                )
            }
            else -> throw IllegalArgumentException("Enter the full card number")
        }
        val data = mutableMapOf<String, Any>(
            "paymentMethodId" to id,
            "label" to input.label.trim(),
            "type" to input.type.trim().ifBlank { "card" },
            "brand" to brandOut,
            "maskedPan" to maskedPan,
            "last4" to last4,
            "holderName" to input.holderName.trim(),
            "expiryMonth" to input.expiryMonth.coerceIn(1, 12),
            "expiryYear" to maxOf(input.expiryYear, 2024),
            "isDefault" to input.isDefault,
        )
        if (existingPaymentMethodId.isNullOrBlank()) {
            data["createdAt"] = FieldValue.serverTimestamp()
        }
        ref.set(data, SetOptions.merge()).await()
        if (input.isDefault) {
            clearOtherDefaultsPayment(userId, keepId = id)
        }
        id
    }

    suspend fun deletePaymentMethod(userId: String, paymentMethodId: String): Result<Unit> = runCatching {
        db.collection(USERS).document(userId).collection(PAYMENT_METHODS).document(paymentMethodId).delete().await()
    }

    suspend fun setDefaultPaymentMethod(userId: String, paymentMethodId: String): Result<Unit> = runCatching {
        clearOtherDefaultsPayment(userId, keepId = paymentMethodId)
        db.collection(USERS).document(userId).collection(PAYMENT_METHODS).document(paymentMethodId)
            .update("isDefault", true).await()
    }

    private suspend fun clearOtherDefaultsPayment(userId: String, keepId: String) {
        val snap = db.collection(USERS).document(userId).collection(PAYMENT_METHODS).get().await()
        val batch = db.batch()
        snap.documents.forEach { doc ->
            if (doc.id != keepId && doc.getBoolean("isDefault") == true) {
                batch.update(doc.reference, "isDefault", false)
            }
        }
        batch.commit().await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toShippingAddressDoc(): ShippingAddressDoc? {
        val pid = getString("addressId")?.takeIf { it.isNotBlank() } ?: id
        return ShippingAddressDoc(
            addressId = pid,
            label = getString("label").orEmpty(),
            fullName = getString("fullName").orEmpty(),
            phone = getString("phone").orEmpty(),
            line1 = getString("line1").orEmpty(),
            line2 = getString("line2").orEmpty(),
            district = getString("district").orEmpty(),
            city = getString("city").orEmpty(),
            postalCode = getString("postalCode").orEmpty(),
            country = getString("country").orEmpty(),
            isDefault = getBoolean("isDefault") == true,
            createdAtMs = this.readMillis("createdAt"),
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toPaymentMethodDoc(): PaymentMethodDoc? {
        val pid = getString("paymentMethodId")?.takeIf { it.isNotBlank() } ?: id
        return PaymentMethodDoc(
            paymentMethodId = pid,
            label = getString("label").orEmpty(),
            type = getString("type").orEmpty(),
            brand = getString("brand").orEmpty(),
            maskedPan = getString("maskedPan").orEmpty(),
            last4 = getString("last4").orEmpty(),
            holderName = getString("holderName").orEmpty(),
            expiryMonth = (getLong("expiryMonth") ?: 1L).toInt().coerceIn(1, 12),
            expiryYear = (getLong("expiryYear") ?: 2030L).toInt(),
            isDefault = getBoolean("isDefault") == true,
            createdAtMs = this.readMillis("createdAt"),
        )
    }

    companion object {
        private const val USERS = "users"
        private const val SHIPPING_ADDRESS = "shippingAddress"
        private const val PAYMENT_METHODS = "paymentMethods"
    }
}
