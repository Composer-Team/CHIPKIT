package chipkit

import beethoven.Platforms.PlatformType.PlatformType
import beethoven.Platforms.{DeviceConfig, PhysicalInterface, PhysicalMemoryInterface, Platform, PlatformType}
import beethoven.Protocol.FrontBus.{AXIFrontBusProtocol, FrontBusProtocol}

class SimPlatform(override val clockRateMHz: Int, memoryBusWidthBytes: Int = 4) extends Platform {
  override val platformType: PlatformType = PlatformType.FPGA
  override val hasDiscreteMemory: Boolean = true
  /**
   * The front bus is the MMIO bus that the host uses to access accelerator cores. Most parameters are
   * self-explanatory except for some platforms (usually heavily resource constrained ones), the host
   * may access memory directly through beethoven. This adds some latency but is not the primary concern
   * on such systems.
   */
  override val frontBusBaseAddress: Long = 0x0
  override val frontBusAddressNBits: Int = 32
  override val frontBusAddressMask: Long = 0xFFFF
  override val frontBusBeatBytes: Int = 4
  override val frontBusCanDriveMemory: Boolean = false
  override val frontBusProtocol: FrontBusProtocol = new AXIFrontBusProtocol(false)
  /**
   * These parameters describe the main memory access channel. Of note, the physical address space can
   * be smaller than the addressable space. This is the case, for instance, when certain parts of the
   * address space correspond to peripherals and don't actually correspond to main memory. The
   * `physicalMemoryBytes` parameter corresponds to the size of the physical memory space and
   * `memorySpaceSizeBytes` corresponds to the size of the whole addressable space.
   */
  override val physicalMemoryBytes: Long = 1L << 32
  override val memorySpaceAddressBase: Long = 0x0
  override val memorySpaceSizeBytes: Long = 1L << 32
  override val memoryNChannels: Int = 1
  override val memoryControllerIDBits: Int = 4
  override val memoryControllerBeatBytes: Int = memoryBusWidthBytes
  override val physicalDevices: List[DeviceConfig] = List(DeviceConfig(0, "root"))
  override val physicalInterfaces: List[PhysicalInterface] = List(PhysicalMemoryInterface(0, 0))
  override val physicalConnectivity: List[(Int, Int)] = List.empty
}
