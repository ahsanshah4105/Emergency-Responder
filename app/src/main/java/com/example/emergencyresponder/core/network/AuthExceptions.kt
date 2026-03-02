package com.example.emergencyresponder.core.network

sealed class AuthException(message: String) : Exception(message)  {
    class InvalidCredentialsException : AuthException("Email or password is incorrect.")
    class EmailNotVerifiedException : AuthException("Email not verified. Please check your inbox.")
    class NetworkException : AuthException("Network error. Please check your internet connection.")
    class UserAlreadyExistsException : AuthException("User already exists.")
    class GoogleLoginException : AuthException("Google login failed.")
    class UserNotFoundException : AuthException("User not found.")
    class UserNotLoggedInException : AuthException("User not logged in.")
    class UserSessionIsExpiredException : AuthException("User session is expired. Please login again. ")
    class UserNotAuthenticatedException : AuthException("User not authenticated.")

    class EmailAlreadyInUseException : AuthException("Email already in use.")
    class PasswordTooShortException : AuthException("Password must be at least 6 characters long.")
    class PasswordMismatchException : AuthException("Passwords do not match.")
    class InValidEmailException : AuthException("Please enter a valid email address.")
    class NewEmailRequiredException : AuthException("New email is required.")
    class CurrentPasswordRequiredException : AuthException("Current password is required.")
    class EmailProviderException : AuthException("Failed to send verification email.")
    class UserCreationFailedException : AuthException("User creation failed.")
    class NameCannotBeEmpty : AuthException("Name cannot be empty")
    class NameIsTooShort : AuthException("Name must be at least 3 characters long")
    class InvalidPhoneFormat : AuthException("Invalid phone format")
    class InvalidPhoneNumber : AuthException("Invalid phone number")
    class DatabaseException(override val message: String) : AuthException("Database error")
    class UnknownException(override val message: String) : AuthException("Unknown error")
    class FailedToDeleteEmergencyContact : AuthException("Failed to delete emergency contact")



}





