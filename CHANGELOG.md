# 1.x

## 1.3.0
* Kotlin 2.4.0 and Gradle 9.2
* Fix Base10 and Base58 resource hoggery
* Fix Base10 and Base58 encoding/decoding of empty and all-zero payloads
* Rewrite arbitrary Base-N encoding/decoding using a BigInteger-free Base-X algorithm
    * Add `ByteArray.encodeBaseN` and `String.decodeBaseN` as the new API
    * Deprecate the entire `BaseN` object and its BigInteger compatibility APIs for removal in 1.4.0
    * Keep BigInteger out of all non-deprecated encoding and decoding paths
    * Reject invalid bases and alphabets instead of risking non-termination
* Migrate to TestBalloon matrix tests across all supported encodings and platforms
* Migrate the build to conventions plugin
* Publish lightweight Javadoc redirect JARs while retaining full hosted Dokka documentation

## 1.2.2
* Kotlin 2.1.20
* Encoding 2.4.0
- More targets:
    * watchosSimulatorArm64
    * watchosX64
    * watchosArm32
    * watchosArm64
    * androidNativeX64
    * androidNativeX86
    * androidNativeArm32
    * androidNativeArm64

## 1.2.1
* Kotlin 2.0.20
* WasmJS support

## 1.2.0
* Include `UVarInt`

## 1.1.1
* Fix Typo in POM

## 1.1.0

* Delegate Base16 encoding/decoding to Matthew Nelson's encoder/decoder to improve performance
* Also expose Matthew Nelson's Base16 encoder/decoder as API dependency
* Expose `BaseN` object, for manually implementing arbitrary BaseN encodings

## 1.0.0

* Initial public release
