package dimensional.socks

public enum class SocksCommand(public val value: UByte) {
    /** Establish a TCP/IP stream connection */
    Connect(0x01u),

    /** Establish a TCP/IP port binding */
    Bind(0x02u),

    /** Associate a UDP port. Only supported by SOCKS5 */
    Associate(0x03u)
    ;

    override fun toString(): String = "SocksCommand::$name"
}
