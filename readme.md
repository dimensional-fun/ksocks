# Ksocks

> Experimental SOCKS v4 & v5 client for Kotlin Multiplatform

- 🚀 uses ktor

[**Discord Server**](https://discord.gg/8R4d8RydT4)

## Installation

Current Version: *coming soon*

```kotlin
repositories {
    maven("https://maven.dimensional.fun/public")
}

dependencies {
    implementation("fun.dimensional:ksocks-core:{VERSION}")
}
```

## Usage

```kotlin
fun main() {
    val selector = ActorSelectorManager(Dispatchers.Default + Job())
    val socksServer = InetSocketAddress("127.0.0.1", 1080)

    // SOCKS5
    val connection = socks(selector, socksServer)
        .v5()
        .userPass("test", "test") // this can be omitted.
        .connect(SocksAddress("ip", "port"))
    
    // SOCKS4
    val connection = socks(selector, socksServer)
        .v4()
        .connect(SocksAddress("ip", "port"))
}
```

## Acknowledgements

- [SOCKS](https://en.wikipedia.org/wiki/SOCKS) on wikipedia
- https://github.com/JoshGlazebook/socks 

---

[Dimensional Fun](https://www.dimensional.fun) &copy; 2020 - 2022
