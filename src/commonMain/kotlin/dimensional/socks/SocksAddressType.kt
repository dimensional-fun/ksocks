package dimensional.socks

public sealed class SocksAddressType(public val value: UByte) {
    public companion object {
        public val ALL: List<SocksAddressType> get() = listOf(IPV4, IPV6, DomainName)

        public fun valueOf(value: String): SocksAddressType? = when (value.lowercase()) {
            "ipv4" -> IPV4
            "ipv6" -> IPV6
            "domainname" -> DomainName
            else -> null
        }

        public fun valueOf(value: Byte): SocksAddressType? {
            return valueOf(value.toUByte())
        }

        public fun valueOf(value: UByte): SocksAddressType? {
            return ALL.find { it.value == value }
        }
    }

    /** ipv4 address */
    public object IPV4 : SocksAddressType(0x01u) {
        override fun toString(): String = "SocksAddressType::IPV4"
    }

    /** ipv6 address */
    public object IPV6 : SocksAddressType(0x04u) {
        override fun toString(): String = "SocksAddressType::IPV6"
    }

    /** domain name */
    public object DomainName : SocksAddressType(0x03u) {
        override fun toString(): String = "SocksAddressType::DomainName"
    }
}
