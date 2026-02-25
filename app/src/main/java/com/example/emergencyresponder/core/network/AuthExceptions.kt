package com.example.emergencyresponder.core.network

sealed class AuthException(message: String) : Exception(message)  {
    class InvalidCredentialsException : AuthException("Email or password is incorrect.")
    class EmailNotVerifiedException : AuthException("Email not verified. Please check your inbox.")
    class NetworkException : AuthException("Network error. Please check your internet connection.")
    class UserAlreadyExistsException : AuthException("User already exists.")
    class GoogleLoginException : AuthException("Google login failed.")
    class UserNotFoundException : AuthException("User not found.")
    class UserNotLoggedInException : AuthException("User not logged in.")

    class EmailAlreadyInUseException : AuthException("Email already in use.")
    class PasswordTooShortException : AuthException("Password must be at least 6 characters long.")
    class PasswordMismatchException : AuthException("Passwords do not match.")
    class InvalidPasswordException : AuthException("Invalid password format.")
    class InValidEmailException : AuthException("Please enter a valid email address.")
    class EmailProviderException : AuthException("Failed to send verification email.")
    class UserCreationFailedException : AuthException("User creation failed.")
}





