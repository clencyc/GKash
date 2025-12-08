# Phone Number Entry Flow - Debugging & Fixes

## Problem
The UI was skipping the phone number entry screen even though logs indicated the navigation state was set to `ADD_PHONE`.

## Root Cause Analysis
The phone entry flow failure was likely due to:
1. **API serialization mismatch**: The `AddPhoneRequest` was using camelCase (`phoneNumber`) instead of snake_case (`phone_number`) expected by the backend
2. **Missing error handling**: The API call didn't have proper error response handling like other endpoints
3. **Lack of visibility**: Insufficient logging to track the flow of data and identify where it fails

## Changes Made

### 1. Fixed AddPhoneRequest Serialization (usermodels.kt)
**Issue**: API request field name mismatch
**Fix**: Added `@SerialName("phone_number")` annotation to ensure correct JSON serialization

```kotlin
// BEFORE
@Serializable
data class AddPhoneRequest(
    val phoneNumber: String
)

// AFTER
@Serializable
data class AddPhoneRequest(
    @SerialName("phone_number")
    val phoneNumber: String
)
```

**Import Added**: `import kotlinx.serialization.SerialName`

### 2. Enhanced API Error Handling (ApiService.kt)
**Issue**: No error response handling or logging for phone addition API calls
**Fix**: Added try-catch blocks and logging similar to registration endpoint

```kotlin
override suspend fun addPhone(request: AddPhoneRequest, tempToken: String): AddPhoneResponse {
    return try {
        val response = client.post("$baseUrl/auth/add-phone") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $tempToken")
            setBody(request)
        }.body<AddPhoneResponse>()
        Log.d("API_SERVICE", "Add phone response: $response")
        response
    } catch (e: ClientRequestException) {
        Log.e("API_SERVICE", "Add phone failed: ${e.response.status} ${e.message}")
        try {
            e.response.body<AddPhoneResponse>()
        } catch (parseError: Exception) {
            AddPhoneResponse(success = false, message = e.response.status.description)
        }
    }
}
```

### 3. Added Comprehensive Logging (KycViewModel.kt)
**Issue**: Unable to trace where phone addition was failing
**Fix**: Added detailed debug logging at critical points

```kotlin
fun addPhoneNumber(phoneNumber: String) {
    Log.d("KYC_PHONE", "Starting phone number addition: $phoneNumber, tempToken exists: ${_uiState.value.tempToken.isNotEmpty()}")
    // ... rest of function with additional logging:
    // - Log when API is called
    // - Log response status
    // - Log errors with full stack traces
}
```

### 4. Added UI Visibility Logging (KycScreens.kt)
**Issue**: Couldn't confirm if phone screen was being displayed
**Fix**: Added logging when the composable is rendered

```kotlin
fun KycAddPhoneScreen(...) {
    // ... 
    LaunchedEffect(Unit) {
        Log.d("KYC_UI", "KycAddPhoneScreen is now displayed")
    }
    // ...
}
```

**Import Added**: `import android.util.Log`

### 5. Enhanced Navigation Tracking (KycFlowScreen.kt)
**Issue**: No visibility into step transitions
**Fix**: Added comprehensive logging for navigation state changes

```kotlin
// Log step transitions in KycFlowScreen
LaunchedEffect(Unit) {
    kycViewModel.uiState.collect { state ->
        Log.d("KYC_NAVIGATION", "Step changed: ${state.currentStep}, isLoading: ${state.isLoading}")
    }
}
```

## Testing the Fix

### Expected Behavior After Fix:
1. User completes account creation → sees success toast
2. Phone number entry screen displays → `KycAddPhoneScreen is now displayed` log appears
3. User enters valid phone number (10+ digits)
4. Clicks "Send Verification Code" button
5. API sends phone number with snake_case field name
6. Backend validates and returns success
7. UI navigates to OTP verification screen
8. User enters 6-digit OTP
9. OTP is verified and flow continues to PIN confirmation

### Debugging Output to Watch For:

**Successful Flow Logs:**
```
D/KYC_PHONE: Starting phone number addition: 9876543210, tempToken exists: true
D/KYC_PHONE: Calling addPhoneUseCase with phone: 9876543210, token: eyJhbGciOiJIUzI1NiI...
D/API_SERVICE: Add phone response: AddPhoneResponse(success=true, message=Phone added successfully)
D/KYC_PHONE: AddPhone response: success=true, message=Phone added successfully
D/KYC: Phone number added successfully: 9876543210
D/KYC_UI: Current step in UI: VERIFY_PHONE
D/KYC_NAVIGATION: Step changed: VERIFY_PHONE, isLoading: false
```

**Error Flow Logs:**
```
D/KYC_PHONE: Starting phone number addition: 123, tempToken exists: false
// or
D/API_SERVICE: Add phone failed: 400 Bad Request
D/KYC_PHONE: AddPhone response: success=false, message=Invalid phone number
D/KYC_PHONE: AddPhone failed with error: ...
```

## Files Modified
1. `/app/src/main/java/com/example/g_kash/authentication/data/usermodels.kt`
   - Added `@SerialName` to `AddPhoneRequest`
   - Added `SerialName` import

2. `/app/src/main/java/com/example/g_kash/authentication/data/ApiService.kt`
   - Enhanced `addPhone()` with error handling and logging

3. `/app/src/main/java/com/example/g_kash/authentication/presentation/KycViewModel.kt`
   - Added detailed logging to `addPhoneNumber()` function

4. `/app/src/main/java/com/example/g_kash/authentication/presentation/KycScreens.kt`
   - Added logging to `KycAddPhoneScreen` composable
   - Added Log import

5. `/app/src/main/java/com/example/g_kash/authentication/presentation/KycFlowScreen.kt`
   - Added navigation step transition logging

## Next Steps if Issues Persist

If the phone entry screen is still not appearing:

1. **Check Logcat for:**
   - `KYC_PHONE` logs - are they showing?
   - `KYC_NAVIGATION` logs - is currentStep actually changing?
   - `API_SERVICE` logs - what's the API response?

2. **If API returns error:**
   - Check exact error message in logs
   - Verify phone number format (try with country code)
   - Check backend authentication requirements

3. **If step doesn't change:**
   - Verify `tempToken` is not empty
   - Check if `AddPhoneResponse` is being deserialized correctly
   - Look for any exceptions in the use case

4. **If UI screen doesn't appear:**
   - Verify `when (uiState.currentStep)` in `KycFlowScreen` matches the state
   - Check if another navigation event is overriding the step
   - Look for any LaunchedEffect blocks that might advance the step prematurely
