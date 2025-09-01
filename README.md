<div align="center">

# ![multi^2_base](multibase.png)

[![A-SIT Plus Official](https://raw.githubusercontent.com/a-sit-plus/a-sit-plus.github.io/709e802b3e00cb57916cbb254ca5e1a5756ad2a8/A-SIT%20Plus_%20official_opt.svg)](https://plus.a-sit.at/open-source.html)
![GitHub license](https://img.shields.io/badge/license-MIT-blue)
[![Maven Central](https://img.shields.io/maven-central/v/at.asitplus/multibase)](https://mvnrepository.com/artifact/at.asitplus/multibase/)

[![Kotlin](https://img.shields.io/badge/kotlin-multiplatform-orange.svg?logo=kotlin)](http://kotlinlang.org)
[![Kotlin](https://img.shields.io/badge/kotlin-2.0.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
![Java](https://img.shields.io/badge/java-17-blue.svg?logo=OPENJDK)

## multi<sup>2</sup>–<sub>base</sub> = 100% pure Kotlin *Multi*platform *Multibase* Encoder/Decoder

</div>

This is combined a port of

* [Protocol Labs'](https://protocol.ai/) Kotlin/JVM [multibase implementation](https://github.com/changjiashuai/kotlin-multibase) to Kotlin
multiplatform with significant help from Matthew Nelson's awesome [encoding](https://github.com/05nelsonm/encoding) library and
Uglješa Jovanović's [KMP bignum library](https://github.com/ionspin/kotlin-multiplatform-bignum)
* [Erwin Kok's](https://erwinkok.org/) Kotlin [unsigned varint implementation](https://github.com/erwin-kok/multiformat/blob/main/src/main/kotlin/org/erwinkok/multiformat/util/UVarInt.kt) with some streamlining

This project includes the original Protocol Labs repository as a git submodule for its test vectors, but does not
incorporate any code from it in releases.

multi<sup>2</sup>–<sub>base</sub> runs on all KMP targets except `watchosDeviceArm64` and supports the following multibase encodings:
* BASE10
* BASE16
* BASE16_UPPER
* BASE32
* BASE32_UPPER
* BASE32_PAD
* BASE32_PAD_UPPER
* BASE32_HEX
* BASE32_HEX_UPPER
* BASE32_HEX_PAD
* BASE32_HEX_PAD_UPPER
* BASE58_FLICKR
* BASE58_BTC
* BASE64
* BASE64_URL
* BASE64_PAD
* BASE64_URL_PAD

→ [Full documentation](https://a-sit-plus.github.io/multibase/).

## Using in your Projects

This library is available at maven central.

### Gradle

```kotlin
dependencies {
    implementation("at.asitplus:multibase:$version")
}
```
Note: This library exposes Matthew Nelson's Base64, Base32, and Base16 encoders as API dependency!

### API

Simply `MultiBase.decode(from_multibase_string)` or `MultiBase.encode(Base.<desired>, any_byte_array)` to a multibase string
or use the extension functions:
 * `multibaseString.multibaseDecode()`
 * `byteArray.multibaseEncode(Base.<desired>)`

Note: Base10 and Base58 don't perform well. Only use those on small (<4KiB) data.

`UVarInt` works similarly straight-forward:
* Create: `UVarInt(1337u)`
* Decode: `someVarIntByteArray.varIntDecode()` or `UVarInt.fromByteArray(someVarIntByteArray)`
* Encode: `aUVarInt.encodeToByteArray()`

'Nuff said!

## Contributing
External contributions are greatly appreciated!
Just be sure to observe the contribution guidelines (see [CONTRIBUTING.md](CONTRIBUTING.md)).

<br>


<br>

---
<p align="center">
The MIT license does not apply to the project logo and the A-SIT logo, as these are the sole property of
A-SIT/A-SIT Plus GmbH and may not be used without explicit permission!
</p>
