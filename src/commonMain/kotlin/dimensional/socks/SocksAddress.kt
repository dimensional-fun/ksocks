package dimensional.socks

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*

@OptIn(ExperimentalUnsignedTypes::class)
public data class SocksAddress(
    val type: SocksAddressType,
    val host: String,
    val port: Short
) {
    public companion object {
        private val IPV4_REGEX: Regex = """^(\d{1,3}\.){3}\d{1,3}$""".toRegex(RegexOption.IGNORE_CASE)
        private val IPV6_REGEX: Regex = """^(::)?(((\d{1,3}\.){3}(\d{1,3}))?([0-9a-f]){0,4}:{0,2}){1,8}(::)?$""".toRegex(RegexOption.IGNORE_CASE)

        public operator fun invoke(host: String, port: Short): SocksAddress {
            val type = when {
                IPV4_REGEX.matches(host) -> SocksAddressType.IPV4
                IPV6_REGEX.matches(host) -> SocksAddressType.IPV6
                else -> SocksAddressType.DomainName // TODO: verify valid hostname
            }

            return SocksAddress(type, host, port)
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

                        is SocksAddressType.IPV6 -> TODO()

                        is SocksAddressType.DomainName -> {
                            val len = channel.readByte().toUByte().toInt()
                            channel.readUTF8Line(len) ?: error("Unable to read domain name of length: $len")
                        }
                    }

                    /* read address port */
                    val port = channel.readShort().toUShort().toShort()
                    SocksAddress(type, addr, port)
                }

                SocksVersion.SOCKS4 -> {
                    val port = channel.readShort().toUShort().toShort()
                    val addr = channel.readIPV4()
                    SocksAddress(SocksAddressType.IPV4, addr, port)
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
                    is SocksAddressType.IPV6 -> TODO()
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

    private fun BytePacketBuilder.writeIPV4() {
        val parts = host.split('.', limit = 4)
        for (part in parts) writeUByte(part.toUByte())
    }
}
