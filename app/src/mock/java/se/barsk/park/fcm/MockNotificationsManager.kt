package se.barsk.park.fcm

class MockNotificationsManager : NotificationsManager() {
    override val pushToken: String? = "token"
}
