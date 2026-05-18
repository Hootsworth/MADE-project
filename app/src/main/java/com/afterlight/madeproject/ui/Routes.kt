package com.afterlight.madeproject.ui

object Routes {
    const val Splash = "splash"
    const val Onboarding = "onboarding"
    const val Auth = "auth"
    const val ProfileSetup = "profile_setup"
    const val RoleSelect = "role_select"
    const val Main = "main"
    const val Home = "home"
    const val Discover = "discover"
    const val MyEvents = "my_events"
    const val Notifications = "notifications"
    const val Settings = "settings"
    const val Accounts = "accounts"
    const val EventDetail = "event_detail/{eventId}"
    const val HostEvent = "host_event"
    const val HostControls = "host_controls/{eventId}"
    const val RecapWall = "recap_wall/{eventId}"

    fun eventDetail(eventId: String) = "event_detail/$eventId"
    fun hostControls(eventId: String) = "host_controls/$eventId"
    fun recapWall(eventId: String) = "recap_wall/$eventId"
}
