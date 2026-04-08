package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.EligibleReviewSlot
import com.example.myapplication.data.model.ProductQuestionDoc
import com.example.myapplication.data.model.ProductReviewDoc
import com.example.myapplication.data.repository.OrderRepository
import com.example.myapplication.data.repository.ProductEngagementRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductEngagementViewModel(
    private val productId: String,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val orderRepository: OrderRepository = OrderRepository(),
    private val engagementRepository: ProductEngagementRepository = ProductEngagementRepository(),
) : ViewModel() {

    private val _reviews = MutableStateFlow<List<ProductReviewDoc>>(emptyList())
    val reviews: StateFlow<List<ProductReviewDoc>> = _reviews.asStateFlow()

    private val _questions = MutableStateFlow<List<ProductQuestionDoc>>(emptyList())
    val questions: StateFlow<List<ProductQuestionDoc>> = _questions.asStateFlow()

    private val _eligibleReviewSlot = MutableStateFlow<EligibleReviewSlot?>(null)
    val eligibleReviewSlot: StateFlow<EligibleReviewSlot?> = _eligibleReviewSlot.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private var reviewsListener: ListenerRegistration? = null
    private var questionsListener: ListenerRegistration? = null

    init {
        reviewsListener = engagementRepository.listenProductReviews(productId) { _reviews.value = it }
        questionsListener = engagementRepository.listenProductQuestions(productId) { _questions.value = it }
        refreshEligibility()
    }

    fun refreshEligibility() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid
            if (uid.isNullOrBlank()) {
                _eligibleReviewSlot.value = null
                return@launch
            }
            _eligibleReviewSlot.value = orderRepository.findEligibleReviewSlot(uid, productId)
        }
    }

    fun submitReview(rating: Double, comment: String, onResult: (Result<Unit>) -> Unit) {
        val slot = _eligibleReviewSlot.value
        val uid = auth.currentUser?.uid
        if (slot == null || uid.isNullOrBlank()) {
            onResult(Result.failure(IllegalStateException("Cannot submit review")))
            return
        }
        viewModelScope.launch {
            _busy.value = true
            _lastError.value = null
            val r = engagementRepository.submitReview(
                userId = uid,
                orderId = slot.orderId,
                suborderId = slot.suborderId,
                itemId = slot.itemId,
                productId = slot.productId,
                rating = rating,
                comment = comment,
            )
            r.onFailure { _lastError.value = it.message }
            r.onSuccess {
                refreshEligibility()
            }
            _busy.value = false
            onResult(r)
        }
    }

    fun answerQuestionAsStore(questionId: String, answer: String, onResult: (Result<Unit>) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            onResult(Result.failure(IllegalStateException("Not signed in")))
            return
        }
        viewModelScope.launch {
            _busy.value = true
            _lastError.value = null
            val r = engagementRepository.answerQuestionAsStore(uid, productId, questionId, answer)
            r.onFailure { _lastError.value = it.message }
            _busy.value = false
            onResult(r)
        }
    }

    fun respondToReviewAsStore(reviewId: String, response: String, onResult: (Result<Unit>) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            onResult(Result.failure(IllegalStateException("Not signed in")))
            return
        }
        viewModelScope.launch {
            _busy.value = true
            _lastError.value = null
            val r = engagementRepository.respondToReviewAsStore(uid, productId, reviewId, response)
            r.onFailure { _lastError.value = it.message }
            _busy.value = false
            onResult(r)
        }
    }

    fun submitQuestion(question: String, onResult: (Result<Unit>) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            onResult(Result.failure(IllegalStateException("Not signed in")))
            return
        }
        viewModelScope.launch {
            _busy.value = true
            _lastError.value = null
            val r = engagementRepository.submitQuestion(uid, productId, question).map { }
            r.onFailure { _lastError.value = it.message }
            _busy.value = false
            onResult(r)
        }
    }

    override fun onCleared() {
        reviewsListener?.remove()
        questionsListener?.remove()
    }
}

class ProductEngagementViewModelFactory(
    private val productId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ProductEngagementViewModel(productId) as T
}
