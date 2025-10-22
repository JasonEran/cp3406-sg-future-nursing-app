package com.example.sgfuturenursingapp.ui.screens.admin

import com.example.sgfuturenursingapp.testing.MainDispatcherRule
import com.example.sgfuturenursingapp.ui.data.User
import com.example.sgfuturenursingapp.ui.data.auth.AuthRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HelperManagementViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var firestore: com.google.firebase.firestore.FirebaseFirestore
    private lateinit var authRepository: AuthRepository
    private lateinit var adminUser: FirebaseUser
    private lateinit var usersCollection: CollectionReference
    private lateinit var careGroupsCollection: CollectionReference
    private lateinit var adminUserDocument: DocumentReference
    private lateinit var adminUserSnapshot: DocumentSnapshot

    @Before
    fun setUp() {
        firestore = mockk()
        authRepository = mockk()
        adminUser = mockk(relaxed = true)
        usersCollection = mockk()
        careGroupsCollection = mockk()
        adminUserDocument = mockk()
        adminUserSnapshot = mockk()

        every { firestore.collection("users") } returns usersCollection
        every { firestore.collection("care_groups") } returns careGroupsCollection
        every { usersCollection.document(ADMIN_UID) } returns adminUserDocument
        every { adminUserDocument.get() } returns Tasks.forResult(adminUserSnapshot)
        every { adminUserSnapshot.toObject(User::class.java) } returns
            User(
                uid = ADMIN_UID,
                email = ADMIN_EMAIL,
                role = "Admin",
                careGroupId = ADMIN_GROUP_ID,
            )

        every { adminUser.uid } returns ADMIN_UID
        coEvery { authRepository.getCurrentUser() } returns adminUser
    }

    @Test
    fun `searchHelperByEmail finds helper not yet in group`() = runTest {
        val helperSnapshot = mockk<DocumentSnapshot>()
        val helperQuerySnapshot = mockk<QuerySnapshot>()
        val helperQuery = mockk<Query>()
        val helperUser =
            User(
                uid = HELPER_UID,
                email = HELPER_EMAIL,
                role = "Helper",
                careGroupId = null,
            )

        every { helperSnapshot.toObject(User::class.java) } returns helperUser
        every { helperQuerySnapshot.documents } returns listOf(helperSnapshot)
        every { usersCollection.whereEqualTo("email", HELPER_EMAIL) } returns helperQuery
        every { helperQuery.limit(1) } returns helperQuery
        every { helperQuery.get() } returns Tasks.forResult(helperQuerySnapshot)

        val viewModel = HelperManagementViewModel(firestore, authRepository)
        advanceUntilIdle()

        viewModel.onSearchQueryChange(HELPER_EMAIL)
        viewModel.searchHelperByEmail()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(helperUser, state.foundUser)
        assertNull(state.successMessage)
        assertNull(state.errorMessage)
        assertFalse(state.isMemberOfGroup)
    }

    @Test
    fun `searchHelperByEmail shows already in care group message`() = runTest {
        val helperSnapshot = mockk<DocumentSnapshot>()
        val helperQuerySnapshot = mockk<QuerySnapshot>()
        val helperQuery = mockk<Query>()
        val helperUser =
            User(
                uid = HELPER_UID,
                email = HELPER_EMAIL,
                role = "Helper",
                careGroupId = ADMIN_GROUP_ID,
            )

        every { helperSnapshot.toObject(User::class.java) } returns helperUser
        every { helperQuerySnapshot.documents } returns listOf(helperSnapshot)
        every { usersCollection.whereEqualTo("email", HELPER_EMAIL) } returns helperQuery
        every { helperQuery.limit(1) } returns helperQuery
        every { helperQuery.get() } returns Tasks.forResult(helperQuerySnapshot)

        val viewModel = HelperManagementViewModel(firestore, authRepository)
        advanceUntilIdle()

        viewModel.onSearchQueryChange(HELPER_EMAIL)
        viewModel.searchHelperByEmail()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(helperUser, state.foundUser)
        assertTrue(state.isMemberOfGroup)
        assertEquals("${helperUser.email} is already in this care group.", state.successMessage)
        assertNull(state.errorMessage)
    }

    @Test
    fun `addHelperToCareGroup updates firestore and state`() = runTest {
        val helperSnapshot = mockk<DocumentSnapshot>()
        val helperQuerySnapshot = mockk<QuerySnapshot>()
        val helperQuery = mockk<Query>()
        val helperUser =
            User(
                uid = HELPER_UID,
                email = HELPER_EMAIL,
                role = "Helper",
                careGroupId = null,
            )
        val helperDocument = mockk<DocumentReference>()
        val careGroupDocument = mockk<DocumentReference>()

        every { helperSnapshot.toObject(User::class.java) } returns helperUser
        every { helperQuerySnapshot.documents } returns listOf(helperSnapshot)
        every { usersCollection.whereEqualTo("email", HELPER_EMAIL) } returns helperQuery
        every { helperQuery.limit(1) } returns helperQuery
        every { helperQuery.get() } returns Tasks.forResult(helperQuerySnapshot)
        every { usersCollection.document(HELPER_UID) } returns helperDocument
        every { careGroupsCollection.document(ADMIN_GROUP_ID) } returns careGroupDocument

        every { firestore.runBatch(any()) } returns Tasks.forResult(null)
        val viewModel = HelperManagementViewModel(firestore, authRepository)
        advanceUntilIdle()

        viewModel.onSearchQueryChange(HELPER_EMAIL)
        viewModel.searchHelperByEmail()
        advanceUntilIdle()

        viewModel.addHelperToCareGroup()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.foundUser)
        assertNull(state.errorMessage)
        assertEquals("$HELPER_EMAIL added to care group.", state.successMessage)
        assertTrue(state.isMemberOfGroup)
        assertEquals(ADMIN_GROUP_ID, state.foundUser?.careGroupId)
    }

    private companion object {
        const val ADMIN_UID = "adminUid"
        const val ADMIN_EMAIL = "admin@test.com"
        const val ADMIN_GROUP_ID = "${ADMIN_UID}_group"
        const val HELPER_UID = "helperUid"
        const val HELPER_EMAIL = "helper@email.com"
    }
}
