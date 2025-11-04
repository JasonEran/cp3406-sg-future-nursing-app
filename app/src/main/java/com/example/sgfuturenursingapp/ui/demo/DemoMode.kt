package com.example.sgfuturenursingapp.ui.demo

import com.example.sgfuturenursingapp.network.model.NewsArticle
import com.example.sgfuturenursingapp.network.model.NewsSource
import com.example.sgfuturenursingapp.ui.data.ResourceArticle
import com.example.sgfuturenursingapp.ui.data.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Simple in-memory flag that marks whether the app should surface demo fixtures instead of live
 * Firebase/News API data. Only enabled through the debug-only "Skip login" action.
 */
object DemoModeController {
    private val _isDemoModeEnabled = MutableStateFlow(false)
    val isDemoModeEnabled: StateFlow<Boolean> = _isDemoModeEnabled.asStateFlow()

    fun enableDemoMode() {
        _isDemoModeEnabled.value = true
    }

    fun disableDemoMode() {
        _isDemoModeEnabled.value = false
    }
}

/**
 * Centralised collection of sample data that mirrors real workloads so screenshots never show empty
 * states when demo mode is active.
 */
object DemoContentProvider {
    val tasks =
        listOf(
            Task(
                id = 2001,
                title = "Morning medication review",
                time = "07:30",
                category = "Medication",
                iconName = "medical_services",
                isCompleted = false,
                priority = 3,
                userId = "helper-alex",
            ),
            Task(
                id = 2002,
                title = "Blood pressure & pulse check",
                time = "09:00",
                category = "Vitals",
                iconName = "monitor_heart",
                isCompleted = false,
                priority = 3,
                userId = "helper-suri",
            ),
            Task(
                id = 2003,
                title = "Video call with physiotherapist",
                time = "11:15",
                category = "Therapy",
                iconName = "devices",
                isCompleted = true,
                priority = 2,
                userId = "helper-alex",
            ),
            Task(
                id = 2004,
                title = "Hydration reminder",
                time = "13:00",
                category = "Daily Care",
                iconName = "water_drop",
                isCompleted = false,
                priority = 1,
                userId = "family-amanda",
            ),
            Task(
                id = 2005,
                title = "Cognitive stimulation games",
                time = "15:30",
                category = "Activities",
                iconName = "extension",
                isCompleted = false,
                priority = 2,
                userId = "helper-suri",
            ),
        )

    val newsArticles =
        listOf(
            NewsArticle(
                title = "Digital care boards cut overdue tasks by 32% in pilot homes",
                description = "Combining real-time assignments with analytics helped caregivers respond faster to changes in condition.",
                url = "https://news.example.com/care-boards-impact",
                imageUrl = null,
                publishedAt = "2025-10-25T08:00:00Z",
                source = NewsSource(name = "Caregiver News"),
            ),
            NewsArticle(
                title = "Community nurses embrace shared task lists for dementia care",
                description = "Teams in Singapore report better shift handovers and fewer duplicate visits.",
                url = "https://news.example.com/community-nurse-handovers",
                imageUrl = null,
                publishedAt = "2025-10-22T10:30:00Z",
                source = NewsSource(name = "Health Daily"),
            ),
            NewsArticle(
                title = "5 hydration cues to watch during hot weather alerts",
                description = "Simple reminders and wearable prompts reduce hospital visits for dehydration.",
                url = "https://news.example.com/hydration-cues",
                imageUrl = null,
                publishedAt = "2025-10-18T06:45:00Z",
                source = NewsSource(name = "Nurse Weekly"),
            ),
        )

    val resourceArticles =
        listOf(
            ResourceArticle(
                id = "demo-daily-1",
                title = "Morning safety sweep checklist",
                summary = "Spend 3 minutes scanning flooring, bathroom rails, and appliance switches before the day begins.",
                content = "Walk through the bedroom, bathroom, and kitchen with a flashlight.\n- Confirm night lights are powered.\n- Check that grab bars are tight.\n- Ensure medication trays match the schedule cards.\nThis routine reduces fall risk and builds confidence for helpers starting a shift.",
                category = "daily_care",
            ),
            ResourceArticle(
                id = "demo-daily-2",
                title = "Stretch and hydrate routine",
                summary = "Pair light range-of-motion stretches with timed hydration breaks every two hours.",
                content = "Use a phone timer or the built-in reminder in the dashboard. Encourage seated shoulder rolls, ankle circles, and finger flexes before sipping 150 ml of water. Record compliance inside the task notes to surface in analytics.",
                category = "daily_care",
            ),
            ResourceArticle(
                id = "demo-med-1",
                title = "Setting up medication organisers",
                summary = "How to pre-fill a weekly pill planner and log dose confirmations.",
                content = "Label each compartment with both a time and icon (sun, daybreak, moon). After each dose, helpers scan the QR task to acknowledge completion. Pharmacists recommend reviewing the tray every Sunday evening with a caregiver.",
                category = "medication",
            ),
            ResourceArticle(
                id = "demo-firstaid-1",
                title = "Responding to sudden dizziness",
                summary = "A quick triage guide for caregivers before escalating to telemedicine.",
                content = "Seat the elder, check blood pressure if a cuff is available, and document onset time. If readings stay above 180/120 or symptoms persist beyond 5 minutes, activate the escalation protocol listed in the Profile screen.",
                category = "first_aid",
            ),
        )

    data class DemoProfile(
        val email: String,
        val role: String,
        val careGroupName: String,
        val members: List<DemoCareMember>,
    )

    data class DemoCareMember(
        val email: String,
        val role: String,
    )

    val profile =
        DemoProfile(
            email = "amanda.ong@sgfuture.health",
            role = "Primary Caregiver",
            careGroupName = "Ong Family Care Group",
            members =
                listOf(
                    DemoCareMember(email = "amanda.ong@sgfuture.health", role = "Primary Caregiver"),
                    DemoCareMember(email = "alex.tan@sgfuture.health", role = "Helper"),
                    DemoCareMember(email = "suri.lim@sgfuture.health", role = "Helper"),
                    DemoCareMember(email = "dr.lee@sgfuture.health", role = "Clinician"),
                ),
        )

    data class DemoAnalytics(
        val completed: Int,
        val overdue: Int,
        val pending: Int,
        val helperTotals: List<Pair<String, Int>>,
    )

    val analytics =
        DemoAnalytics(
            completed = 28,
            overdue = 3,
            pending = 7,
            helperTotals =
                listOf(
                    "Alex Tan" to 14,
                    "Suri Lim" to 9,
                    "Visiting Nurse" to 5,
                ),
        )
}
