# Detail: Biometric Transaction Guard

* **Target Commit Title**: `feat(security): enforce biometric prompt on high-value transfers`

## 1. Intent & Architectural Constraints

Enforce strong customer authentication (SCA) for single transfers exceeding limit. Must avoid
blocking the main UI thread during biometric prompt invocation and fallback gracefully to PIN when
biometrics are unavailable.

## 2. Executed Plan

- [x] Create `BiometricPromptHandler` abstraction.
- [x] Inject biometric gate check into `ConfirmTransferUseCase`.
- [x] Update `TransferRepository` signature with required security payload.
- [x] Implement error handling for hardware lockout and canceled prompts.

## 3. Touched Files

* `core/security/BiometricPromptHandler.kt`
* `domain/transfers/ConfirmTransferUseCase.kt`
* `data/transfers/TransferRepository.kt`
* `test/security/BiometricPromptHandlerTest.kt`

## 4. Architectural Divergences & Discoveries

Initially planned to store token in memory cache, but shifted to returning a single-use
crypto-signed nonce to prevent replay attacks during network retry.

## 5. Resulting Commits

* `a1c4e92` - feat(security): enforce biometric prompt on high-value transfers
* `8f2b311` - test(security): add unit tests for BiometricPromptHandler