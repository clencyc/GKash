# OTP Service Integration Guide

## Overview

This document describes the integration of a real OTP (One-Time Password) service into the G-Kash Android application. The implementation replaces the previous mock OTP verification with actual SMS-based OTP functionality using the Tiara Connect OTP service.

## Architecture

The OTP service follows a clean architecture pattern with clear separation of concerns:

```
├── otp/
│   ├── data/
│   │   ├── OtpModels.kt           # Data models for API requests/responses
│   │   └── OtpApiServiceImpl.kt   # Implementation of OTP API service
│   └── domain/
│       ├── OtpApiService.kt       # Interface for OTP operations
│       └── OtpUseCases.kt         # Business logic for OTP operations
```

## API Endpoints

The service integrates with Tiara Connect OTP endpoints:

**Base URL:** `https://tiara-connect-otp.onrender.com`

### Send OTP
- **Endpoint:** `POST /api/auth/send-otp`
- **Request Body:**
```json
{
  "phone": "+254xxxxxxxxx",
  "name": "User Name"
}
```
- **Response:**
```json
{
  "success": true,
  "message": "OTP sent successfully"
}
```

### Verify OTP
- **Endpoint:** `POST /api/auth/verify-otp`
- **Request Body:**
```json
{
  "phone": "+254xxxxxxxxx", 
  "otp": "123456"
}
```
- **Response:**
```json
{
  "success": true,
  "message": "OTP verified successfully"
}
```

## Implementation Details

### 1. Data Models (`OtpModels.kt`)

Defines serializable data classes for API communication:
- `SendOtpRequest` & `SendOtpResponse`
- `VerifyOtpRequest` & `VerifyOtpResponse`

### 2. API Service Interface (`OtpApiService.kt`)

```kotlin
interface OtpApiService {
    suspend fun sendOtp(phone: String, name: String): Result<SendOtpResponse>
    suspend fun verifyOtp(phone: String, otp: String): Result<VerifyOtpResponse>
}
```

### 3. API Service Implementation (`OtpApiServiceImpl.kt`)

Key features:
- **Phone Number Formatting**: Automatically formats phone numbers to include +254 country code
- **Error Handling**: Proper exception handling with logging
- **Ktor Integration**: Uses existing Ktor HTTP client
- **Comprehensive Logging**: Debug logs for troubleshooting

### 4. Use Cases (`OtpUseCases.kt`)

Business logic layer that includes:
- **Input Validation**: Validates phone numbers and OTP format
- **Phone Number Cleaning**: Removes spaces, hyphens, etc.
- **Kenyan Phone Number Validation**: Supports multiple formats:
  - `+254xxxxxxxxx` (13 digits)
  - `254xxxxxxxxx` (12 digits)
  - `0xxxxxxxxx` (10 digits starting with 0)
  - `xxxxxxxxx` (9 digits)

## KYC Integration

The OTP service is integrated into the KYC flow in `KycViewModel`:

### Updated Flow
1. **Add Phone Number**: User enters phone number → Backend saves it
2. **Send OTP**: Real OTP is sent via Tiara Connect service
3. **Verify OTP**: User enters received OTP → Real verification occurs

### Key Changes

#### `addPhoneNumber()` Method
```kotlin
// First add phone number to backend
addPhoneUseCase(phoneNumber).fold(
    onSuccess = { 
        // Then send real OTP
        sendOtpUseCase(phoneNumber, userName).fold(...)
    }
)
```

#### `verifyOtp()` Method
```kotlin
// Use real OTP verification
verifyOtpUseCase(phoneNumber, otp).fold(
    onSuccess = { verifyResponse ->
        if (verifyResponse.success) {
            // Proceed to next step
        }
    }
)
```

## Dependency Injection

Updated `AppModule.kt` to include:

```kotlin
// OTP API Service
single<OtpApiService> { OtpApiServiceImpl(get()) }

// OTP Use Cases
factory { SendOtpUseCase(get()) }
factory { VerifyOtpUseCase(get()) }

// Updated KycViewModel with OTP dependencies
viewModel { KycViewModel(get(), get(), get(), get(), get(), get()) }
```

## Error Handling

The service includes comprehensive error handling:

### Network Errors
- Connection timeouts
- Network unavailability
- API server errors

### Validation Errors
- Invalid phone number formats
- Invalid OTP format (must be 6 digits)
- Missing required parameters

### API Response Errors
- OTP send failures
- OTP verification failures
- Rate limiting

## Phone Number Formatting

The service automatically handles various phone number formats:

```kotlin
// Input formats supported:
"0712345678"     → "+254712345678"
"712345678"      → "+254712345678"  
"254712345678"   → "+254712345678"
"+254712345678"  → "+254712345678" (unchanged)
```

## Testing

### Manual Testing
1. **Send OTP Test:**
```bash
curl -X POST https://tiara-connect-otp.onrender.com/api/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{"phone": "+254700000000", "name": "Test User"}'
```

2. **Verify OTP Test:**
```bash
curl -X POST https://tiara-connect-otp.onrender.com/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"phone": "+254700000000", "otp": "123456"}'
```

### App Testing
1. Complete KYC flow up to phone number entry
2. Enter a valid Kenyan phone number
3. Check for OTP SMS receipt
4. Enter received OTP code
5. Verify successful progression to PIN creation

## Logging

Comprehensive logging for debugging:

```
D/OtpApiService: Sending OTP to phone: 0712345678, name: John Doe
D/OtpApiService: Formatted phone number: +254712345678
D/OtpApiService: OTP sent successfully to +254712345678

D/OtpApiService: Verifying OTP for phone: 0712345678, otp: 123456
D/OtpApiService: Formatted phone number: +254712345678
D/OtpApiService: OTP verified successfully for +254712345678
```

## Security Considerations

1. **No OTP Storage**: OTP codes are never stored in the app
2. **Secure Transmission**: All communication over HTTPS
3. **Input Validation**: Strict validation of all inputs
4. **Rate Limiting**: Server-side rate limiting prevents abuse

## Future Enhancements

Consider implementing:
1. **Retry Logic**: Automatic retry for failed OTP operations
2. **Resend OTP**: Allow users to request new OTP codes
3. **Multiple Providers**: Support for multiple OTP service providers
4. **Localization**: Multi-language support for OTP messages
5. **Analytics**: Track OTP success/failure rates

## Troubleshooting

### Common Issues

**OTP Not Received:**
- Check phone number format
- Verify network connectivity
- Check device SMS settings

**Verification Fails:**
- Ensure OTP is 6 digits
- Check for typos
- Verify OTP hasn't expired

**Network Errors:**
- Check internet connectivity
- Verify Tiara Connect service availability
- Check firewall/proxy settings

### Debug Logs
Monitor these log tags for troubleshooting:
- `OtpApiService`: API communication logs
- `KYC`: KYC flow integration logs
- `SendOtpUseCase` / `VerifyOtpUseCase`: Business logic logs

## Deployment Checklist

- [ ] Test OTP sending with real phone numbers
- [ ] Test OTP verification with various phone formats
- [ ] Verify error handling scenarios
- [ ] Test network failure scenarios  
- [ ] Monitor logs for any issues
- [ ] Test complete KYC flow end-to-end

This integration provides a robust, production-ready OTP solution for the G-Kash application while maintaining clean architecture principles and comprehensive error handling.