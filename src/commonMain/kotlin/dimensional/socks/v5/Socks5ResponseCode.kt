package dimensional.socks.v5

public enum class Socks5ResponseCode(
    public val value: UByte,
    public val message: String,
    public val failure: Boolean = true,
) {
    Success(
        0x00u,
        "",
        false
    ),

    Failure(
        0x01u,
        "SOCKS5 server failure"
    ),

    RuleFailure(
        0x02u,
        "SOCKS5 rule failure"
    ),

    NetworkUnreachable(
        0x03u,
        "Network Unreachable"
    ),

    HostUnreachable(
        0x04u,
        "Host Unreachable"
    ),

    ConnectionRefused(
        0x05u,
        "Connection Refused"
    ),

    TtlExpired(
        0x06u,
        "TTL Expired"
    ),

    CommandNotSupported(
        0x07u,
        "Command Not Supported"
    ),

    AddrTypeNotSupported(
        0x08u,
        "Address Type Not Supported"
    ),
    ;

    override fun toString(): String = "Socks5ResponseCode::$name"
}
