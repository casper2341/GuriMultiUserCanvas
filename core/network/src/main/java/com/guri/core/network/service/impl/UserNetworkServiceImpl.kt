package com.guri.core.network.service.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.guri.core.model.Result
import com.guri.core.model.User
import com.guri.core.network.service.UserNetworkService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserNetworkServiceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserNetworkService {
    
    private val usersCollection = firestore.collection("users")
    
    override suspend fun getCurrentUser(): Result<User> {
        return try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                val userDoc = usersCollection.document(firebaseUser.uid).get().await()
                if (userDoc.exists()) {
                    Result.success(userDoc.toObject(User::class.java) ?: createUserFromFirebaseUser(firebaseUser))
                } else {
                    val newUser = createUserFromFirebaseUser(firebaseUser)
                    usersCollection.document(newUser.id).set(newUser).await()
                    Result.success(newUser)
                }
            } else {
                Result.error(Exception("No user is signed in"))
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }
    
    override suspend fun signInAnonymously(): Result<User> {
        return try {
            val authResult = firebaseAuth.signInAnonymously().await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val newUser = createUserFromFirebaseUser(firebaseUser)
                usersCollection.document(newUser.id).set(newUser).await()
                Result.success(newUser)
            } else {
                Result.error(Exception("Failed to sign in anonymously"))
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }
    
    override suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }
    
    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }
    
    override suspend fun getUserById(userId: String): Result<User> {
        return try {
            val userDoc = usersCollection.document(userId).get().await()
            if (userDoc.exists()) {
                Result.success(userDoc.toObject(User::class.java) ?: 
                    User(id = userId, name = "Unknown User"))
            } else {
                Result.error(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }
    
    override fun observeCurrentUser(): Flow<Result<User>> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                usersCollection.document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(Result.error(error))
                            return@addSnapshotListener
                        }
                        
                        if (snapshot != null && snapshot.exists()) {
                            val user = snapshot.toObject(User::class.java)
                            if (user != null) {
                                trySend(Result.success(user))
                            } else {
                                val newUser = createUserFromFirebaseUser(firebaseUser)
                                trySend(Result.success(newUser))
                            }
                        } else {
                            val newUser = createUserFromFirebaseUser(firebaseUser)
                            trySend(Result.success(newUser))
                        }
                    }
            } else {
                trySend(Result.error(Exception("No user is signed in")))
            }
        }
        
        firebaseAuth.addAuthStateListener(authStateListener)
        
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }
    
    private fun createUserFromFirebaseUser(firebaseUser: com.google.firebase.auth.FirebaseUser): User {
        return User(
            id = firebaseUser.uid,
            name = firebaseUser.displayName ?: "User-${firebaseUser.uid.take(5)}",
            email = firebaseUser.email ?: "",
            photoUrl = firebaseUser.photoUrl?.toString(),
            isAnonymous = firebaseUser.isAnonymous
        )
    }
} 