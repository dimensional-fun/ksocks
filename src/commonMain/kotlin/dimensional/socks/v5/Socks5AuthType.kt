package dimensional.socks.v5

public sealed class Socks5AuthType(public val value: UByte) {
    public companion object {
        public val ALL: List<Socks5AuthType> get() = listOf(NoAuth, GSSAPI, UserPass, NoMethods)

        public fun valueOf(value: Byte): Socks5AuthType {
            return valueOf(value.toUByte())
        }

        public fun valueOf(value: UByte): Socks5AuthType {
            return ALL.find { it.value == value } ?: Custom(value)
        }
    }

    /** No authentication */
    public object NoAuth : Socks5AuthType(0x00u) {
        override fun toString(): String = "Socks5AuthType::NoAuth"
    }

    /** [GSSAPI](https://en.wikipedia.org/wiki/GSSAPI) */
    public object GSSAPI : Socks5AuthType(0x01u) {
        override fun toString(): String = "Socks5AuthType::GSSAPI"
    }

    /** Authenticate with a username/password */
    public object UserPass : Socks5AuthType(0x02u) {
        /** The username/password auth version */
        public val version: UByte = 0x01u

        override fun toString(): String = "Socks5AuthType::UserPass"
    }

    /** Couldn't authenticate */
    public object NoMethods : Socks5AuthType(0xFFu) {
        override fun toString(): String = "Socks5AuthType::NoMethods"
    }

    /** A custom authentication type. */
    public class Custom(value: UByte) : Socks5AuthType(value) {
        override fun toString(): String = "Socks5AuthType::Custom(value=$value)"
    }
}
