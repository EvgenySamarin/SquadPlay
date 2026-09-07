# Brief: Biometric Transaction Guard

* **Commit Title**: `feat(security): enforce biometric prompt on high-value transfers`
* **Domains**: `security`, `payments`

## Affected Capabilities & Side Effects

* **Behavior**: Intercepts transfer requests exceeding threshold amount and displays system
  biometric prompt before dispatching API call.
* **Contract Adjustments**: Extended `TransferRepository.submitOrder` interface with optional
  `BiometricProofToken` argument.
* **Hotspots**: Downstream payment dispatchers must now supply an authenticated token or catch
  `BiometricAuthRequiredException`.