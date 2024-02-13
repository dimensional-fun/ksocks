package dimensional.socks

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*

@OptIn(ExperimentalUnsignedTypes::class)
public data class SocksAddress(
    val type: SocksAddressType,
    val host: String,
    val port: String
) {
    public companion object {
        private val IPV4_REGEX: Regex = """^(\d{1,3}\.){3}\d{1,3}$""".toRegex(RegexOption.IGNORE_CASE)
        private val IPV6_REGEX: Regex = """^(::)?(((\d{1,3}\.){3}(\d{1,3}))?([0-9a-f]){0,4}:{0,2}){1,8}(::)?$""".toRegex(RegexOption.IGNORE_CASE)

        public operator fun invoke(host: String, port: String): SocksAddress {
            val type = when {
                IPV4_REGEX.matches(host) -> SocksAddressType.IPV4
                IPV6_REGEX.matches(host) -> SocksAddressType.IPV6
                isValidHostname(host) -> SocksAddressType.DomainName
                else -> throw IllegalArgumentException("Invalid hostname: $host")
            }

            return SocksAddress(type, host, port)
        }

        private fun isValidHostname(hostname: String): Boolean {
            // RFC 1035 allows letters, digits, and hyphens in domain names
            // It also specifies that the name cannot start or end with a hyphen
            // Total length must be between 1 and 255 characters
            val hostnameRegex = """^[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"""
            return hostname.matches(hostnameRegex.toRegex())
        }

        public suspend fun read(version: SocksVersion, channel: ByteReadChannel): SocksAddress {
            return when (version) {
                SocksVersion.SOCKS5 -> {
                    val typeByte = channel.readByte()

                    /* read address type */
                    val type = SocksAddressType.valueOf(typeByte)
                        ?: error("Unknown address type: $typeByte")

                    /* read address string */
                    val addr = when (type) {
                        is SocksAddressType.IPV4 -> channel.readIPV4()

                        is SocksAddressType.IPV6 -> channel.readIPV6()

                        is SocksAddressType.DomainName -> {
                            val len = channel.readByte().toUByte().toInt()
                            channel.readUTF8Line(len) ?: error("Unable to read domain name of length: $len")
                        }
                    }

                    /* read address port */
                    val port = channel.readShort().toUShort().toShort()
                    SocksAddress(type, addr, port.toString())
                }

                SocksVersion.SOCKS4 -> {
                    val port = channel.readShort().toUShort().toShort()
                    val addr = channel.readIPV4()
                    SocksAddress(SocksAddressType.IPV4, addr, port.toString())
                }
            }
        }

        private suspend fun ByteReadChannel.readIPV4(): String = buildString {
            repeat(4) {
                val part = readByte().toUByte()
                append("$part")
                if (it != 3) append('.')
            }
        }
        private suspend fun ByteReadChannel.readIPV6(): String {
            val address = ByteArray(16)
            readFully(address)

            return address.joinToString(":") { byte -> (byte.toInt() and 0xFF).toString(16).padStart(2, '0') }
        }
    }

    /**
     * This address as a byte read packet.
     *
     * @return The [ByteReadPacket]
     */
    public fun asPacket(version: SocksVersion): ByteReadPacket {
        val packet = BytePacketBuilder()
        when (version) {
            SocksVersion.SOCKS5 -> {
                packet.writeUByte(type.value)

                when (type) {
                    is SocksAddressType.IPV4 -> packet.writeIPV4()
                    is SocksAddressType.IPV6 -> packet.writeIPV6()
                    is SocksAddressType.DomainName -> {
                        packet.writeUByte(host.length.toUByte())
                        packet.writeText(host)
                    }
                }

                packet.writeUShort(port.toUShort())
            }

            SocksVersion.SOCKS4 -> {
                require(type is SocksAddressType.IPV4) {
                    "Address type $type is not supported by SOCKS4"
                }

                packet.writeUShort(port.toUShort())
                packet.writeIPV4()
            }
        }

        return packet.build()
    }

    /**
     * Writes this address to the specified [channel]
     *
     * @param channel The byte write channel to use.
     */
    public suspend fun write(version: SocksVersion, channel: ByteWriteChannel) {
        val packet = asPacket(version)
        channel.writePacket(packet)
    }

    /**
     * Writes IPv4 address to the specified [channel].
     *
     * @param channel The byte write channel to use.
     */
    private fun BytePacketBuilder.writeIPV4() {
        val parts = host.split('.', limit = 4)
        for (part in parts) writeUByte(part.toUByte())
    }

    /**
     * Writes IPv6 address to the specified [channel].
     *
     * @param channel The byte write channel to use.
     */
    private fun BytePacketBuilder.writeIPV6() {
        val parts = host.split(':')
        for (part in parts) {
            val value = part.ifEmpty { "0000" }
            val intValue = value.toInt(16)
            writeShort(intValue.toUShort().toShort())
        }
    }
}
