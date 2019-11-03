package se.barsk.park.error

import java.lang.Exception

class FailedToGetFcmTokenException(message: String): Exception(message)
class FailedToSignInException(message: String): Exception(message)