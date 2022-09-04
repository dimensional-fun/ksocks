package dimensional.socks

/** The supported [SOCKS](https://en.wikipedia.org/wiki/SOCKS) versions */
public enum class SocksVersion(public val value: UByte) {
    /** [SOCKS4](https://en.wikipedia.org/wiki/SOCKS#SOCKS4) */
    SOCKS4(0x04u),

    /** [SOCKS5](https://en.wikipedia.org/wiki/SOCKS#SOCKS5) */
    SOCKS5(0x05u)
}
