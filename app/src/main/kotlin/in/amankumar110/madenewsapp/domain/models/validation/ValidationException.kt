package `in`.amankumar110.madenewsapp.domain.models.validation

sealed class ValidationException(message: String) : Exception(message) {
    class InvalidEmail(message: String) : ValidationException(message)
    class InvalidAge(message: String) : ValidationException(message)
    class InvalidPassword(message: String) : ValidationException(message)
    class InvalidUsername(message: String) : ValidationException(message)
}

