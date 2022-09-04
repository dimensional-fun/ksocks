package dimensional.socks.v4

public enum class Socks4ReplyCode(
    public val value: UByte,
    public val message: String,
    public val failure: Boolean = true
) {
    RequestGranted(
        0x5Au,
        "Request Granted",
        false
    ),

    RequestRejected(
        0x5Bu,
        "Request Rejected or Failed",
    ),

    WeirdFailure1(
        0x5Cu,
        "Request Failed"
    ),

    WeirdFailure2(
        0x5Du,
        "Request Failed"
    ),

    ;

}
