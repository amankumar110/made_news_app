package `in`.amankumar110.madenewsapp.utils

import `in`.amankumar110.madenewsapp.domain.models.validation.ValidationException
import java.util.regex.Pattern

object ValidationManager {

    fun validateEmail(email: String): Boolean {
        if (!email.contains("@")) {
            throw ValidationException.InvalidEmail("Email must contain '@' symbol.")
        }
        val emailPattern = "^[A-Za-z0-9\$#@._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        if (!Pattern.matches(emailPattern, email)) {
            throw ValidationException.InvalidEmail("Invalid email format.")
        }
        return true
    }

    fun validateAge(age: String): Boolean {
        val ageInt = age.toIntOrNull()
        if (ageInt == null) {
            throw ValidationException.InvalidAge("Age must be a valid number.")
        }
        if (ageInt < 18) {
            throw ValidationException.InvalidAge("Age must be over 18 years old.")
        }
        if (ageInt > 99) {
            throw ValidationException.InvalidAge("Age must be between 18 and 99.")
        }
        return true
    }

    fun validatePassword(password: String): Boolean {
        if (password.length < 8) {
            throw ValidationException.InvalidPassword("Password must be at least 8 characters long.")}
        if (password.length > 14) {
            throw ValidationException.InvalidPassword("Password must not exceed 14 characters.")
        }
        if (!password.any { it.isLetter() }) {
            throw ValidationException.InvalidPassword("Password must contain at least one letter.")
        }
        if (!password.any { it.isDigit() }) {
            throw ValidationException.InvalidPassword("Password must contain at least one number.")
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            throw ValidationException.InvalidPassword("Password must contain at least one symbol.")
        }
        // This regex can be simplified given the individual checks above,
        // but it's kept for stricter format enforcement if needed.
        val passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#\$%^&*()_+=\\-{}\\[\\]:;\\\"'<>,.?/]).{8,14}\$"
        if (!Pattern.matches(passwordPattern, password)) {
            // This message might be redundant if individual checks are specific enough.
            throw ValidationException.InvalidPassword("Password format incorrect (8-14 chars, letters, numbers, symbols).")
        }
        return true
    }

    fun validateUsername(username: String): Boolean {
        if (username.length < 3) {
            throw ValidationException.InvalidUsername("Username must be at least 3 characters long.")
        }
        if (username.length > 20) {
            throw ValidationException.InvalidUsername("Username must not exceed 20 characters.")
        }
        if (username.startsWith("_") || username.startsWith(".")) {
            throw ValidationException.InvalidUsername("Username cannot start with '_' or '.'.")
        }
        if (username.endsWith("_") || username.endsWith(".")) {
            throw ValidationException.InvalidUsername("Username cannot end with '_' or '.'.")
        }
        if (username.contains("__") || username.contains("..") || username.contains("_.") || username.contains("._")) {
            throw ValidationException.InvalidUsername("Username cannot have consecutive underscores or periods, or combinations like '_.'.")
        }
        // Allows alphanumeric characters, underscores, and periods.
        // It enforces that it doesn't start/end with _ or . and no consecutive _ or .
        // This regex essentially re-checks some of the above but is good for overall structure.
        // ^[a-zA-Z0-9] starts with alphanumeric
        // (?:[a-zA-Z0-9._-]*[a-zA-Z0-9])? allows alphanumeric, dot, underscore, hyphen in middle, ends alphanumeric
        // The hyphen was removed from requirement, adjusting regex.
        // Regex: ^[a-zA-Z0-9](?:[a-zA-Z0-9._]*[a-zA-Z0-9])?$  -- this also enforces no start/end with . or _
        // A simpler regex if individual checks are relied upon: ^[a-zA-Z0-9._]+$
        // The one below is more comprehensive for the stated rules:
        val usernamePattern = "^[a-zA-Z0-9](?!.*[_.]{2})[a-zA-Z0-9._]{1,18}[a-zA-Z0-9]\$"
        // Explanation of the pattern:
        // ^[a-zA-Z0-9]            - Start with an alphanumeric character
        // (?!.*[_.]{2})         - Negative lookahead: not followed by two consecutive underscores or periods anywhere
        // [a-zA-Z0-9._]{1,18}   - Middle part: 1 to 18 characters (alphanumeric, underscore, period)
        //                         (total length 3 to 20, minus start and end char)
        // [a-zA-Z0-9]$            - End with an alphanumeric character

        // A slightly more readable approach without complex regex might be preferred
        // given the individual checks above.
        // Let's refine the regex to be more direct for allowed characters
        // and rely on the manual checks for start/end/consecutive.
        val allowedCharsPattern = "^[a-zA-Z0-9._]+\$"
        if (!Pattern.matches(allowedCharsPattern, username)) {
            throw ValidationException.InvalidUsername("Username can only contain letters, numbers, '.', and '_'.")
        }
        // The individual checks for start/end and consecutive symbols are more explicit
        // than trying to pack everything into one complex regex.
        return true
    }
}
