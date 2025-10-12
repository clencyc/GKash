# KYC 401 Error Fix Guide

## Problem Description
After completing the KYC registration flow and PIN creation, users encountered a 401 Unauthorized error when the app tried to make subsequent API calls. This occurred because there was a race condition between:

1. The PIN creation completing and saving the final authentication token
2. The navigation to the main app happening before the token was fully persisted
3. Subsequent API calls being made with incomplete or missing tokens

## Root Cause Analysis

The original flow was:
1. `KycViewModel.confirmPin()` calls `createPinKycUseCase()`
2. `AuthRepositoryImpl.createPinKyc()` saves the final token via `sessionStorage.saveSession()`
3. `confirmPin()` immediately emits `KycEvent.RegistrationComplete`
4. `KycFlowScreen` adds 1-second delay then calls `onKycComplete()`
5. Navigation to main app occurs potentially before token persistence is complete

## Solution Implemented

### 1. Enhanced Token Persistence Timing (KycViewModel.kt)

**Changes made:**
- Increased wait time from 1 second to 2 seconds in `confirmPin()`
- Added token verification step before emitting `RegistrationComplete` event
- Added proper error handling if token verification fails

```kotlin
// Wait a bit to ensure token is properly persisted before navigation
kotlinx.coroutines.delay(2000) // Increased from implicit 1s to 2s

// Verify token persistence by checking with auth repository
authRepository.getAuthTokenStream().first()?.let { token ->
    Log.d("KYC", "Token verification successful before navigation")
    _events.emit(KycEvent.RegistrationComplete)
    _events.emit(KycEvent.ShowSuccess("Registration completed successfully!"))
} ?: run {
    Log.e("KYC", "Token not found after PIN creation - retrying")
    _events.emit(KycEvent.ShowError("Session setup incomplete. Please try again."))
    _uiState.value = _uiState.value.copy(isLoading = false)
}
```

### 2. Improved KycFlowScreen Event Handling (KycFlowScreen.kt)

**Changes made:**
- Removed the additional 1-second delay since timing is now properly handled in the ViewModel
- Streamlined the `RegistrationComplete` event handler

```kotlin
is KycEvent.RegistrationComplete -> {
    // ViewModel already handles proper timing and token validation
    onKycComplete()
}
```

### 3. Enhanced Token Persistence Validation (AuthRepositoryImpl.kt)

**Changes made:**
- Added token persistence validation in `createPinKyc()`
- Added 500ms delay to ensure DataStore persistence
- Added validation by reading the token back and comparing it with the expected value
- Enhanced logging for debugging token persistence issues

```kotlin
// Validate token persistence by reading it back
kotlinx.coroutines.delay(500) // Small delay to ensure DataStore persistence
val savedToken = sessionStorage.authTokenStream.first()
if (savedToken == response.token) {
    Log.d("KYC", "Token persistence validated successfully")
} else {
    Log.w("KYC", "Token persistence validation failed...")
}
```

### 4. Enhanced SessionStorage Debugging (SessionStorage.kt)

**Changes made:**
- Added debug logging to `saveSession()` and `saveAuthToken()` methods
- Added timing logs to track when token operations occur
- Enhanced session state logging

### 5. Improved Ktor Auth Plugin Error Handling (AppModule.kt)

**Changes made:**
- Enhanced 401 error logging and debugging
- Added session state logging when 401 errors occur
- Improved error handling in the refresh tokens block
- Added better documentation for future refresh token implementation

## Testing the Fix

### 1. Build and Install
```bash
./gradlew assembleDebug
# Install the APK and test the KYC flow
```

### 2. Monitor Logs
Look for these log entries to verify the fix is working:

```
D/KYC: Registration completed successfully
D/SessionStorage: Saving session - Token: [token_preview]..., UserId: [user_id]
D/SessionStorage: Session saved successfully
D/KYC: Token persistence validated successfully
D/KYC: Token verification successful before navigation
```

### 3. Verify No 401 Errors
After KYC completion, subsequent API calls should work without 401 errors.

## Key Improvements

1. **Race Condition Fix**: Ensures token persistence is complete before navigation
2. **Token Validation**: Verifies saved token matches expected token before proceeding
3. **Better Error Handling**: Provides user feedback if token persistence fails
4. **Enhanced Debugging**: Comprehensive logging for troubleshooting token issues
5. **Robust Timing**: Proper delays to account for DataStore persistence timing

## Future Enhancements

Consider implementing these improvements for production:

1. **Refresh Token Mechanism**: Implement proper token refresh logic in the Ktor Auth plugin
2. **Retry Logic**: Add automatic retry for failed token operations
3. **Token Expiration Handling**: Implement proper token expiration detection and handling
4. **Background Persistence**: Consider using WorkManager for critical token operations

## Monitoring

Continue monitoring these metrics post-deployment:
- 401 error rates after KYC completion
- Token persistence failure rates
- User drop-off rates during KYC completion
- Session authentication failure rates

This fix should significantly reduce or eliminate 401 Unauthorized errors after KYC completion while providing better debugging capabilities for any remaining authentication issues.