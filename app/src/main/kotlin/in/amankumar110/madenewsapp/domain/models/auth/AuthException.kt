package `in`.amankumar110.madenewsapp.domain.models.auth

sealed class AuthException(message: String) : RuntimeException(message) {

    class InternetConnection(message: String) : AuthException(message)
    class InvalidEmail(message: String) : AuthException(message)
    class InvalidPassword(message: String) : AuthException(message)
    class UserAlreadyExists(message: String) : AuthException(message)
    class UserNotFound(message: String) : AuthException(message)
    class WrongPassword(message: String) : AuthException(message)
    class UnknownError(message: String) : AuthException(message)
    class EmailNotVerified(message: String) : AuthException(message)
    class UserNotLoggedIn(message: String) : AuthException(message)
    class InvalidCredentials(message: String) : AuthException(message)
    class UsernameAlreadyExists(message: String) : AuthException(message)
}