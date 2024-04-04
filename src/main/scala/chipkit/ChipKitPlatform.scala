package chipkit

import chipsalliance.rocketchip.config.{Config, Parameters}
import composer.Platforms.ASIC.{MemoryCompiler, TechLib}
import composer.Platforms.PlatformType.PlatformType
import composer.Platforms._
import composer.Protocol.FrontBus.FrontBusProtocol
import os.Path

class ChipKitPlatform(m0generator: Parameters => M0Abstract,
                      val technologyLibrary: TechLib,
                      override val clockRateMHz: Int) extends Platform with HasPostProccessorScript with HasMemoryCompiler {
  override val platformType: PlatformType = PlatformType.ASIC
  override val hasDiscreteMemory: Boolean = true
  override val frontBusBaseAddress: Long = 0x2000FC00L
  override val frontBusAddressNBits: Int = 32
  override val frontBusAddressMask: Long = 0x3FFL
  override val frontBusBeatBytes: Int = 4
  override val frontBusCanDriveMemory: Boolean = true
  override val frontBusProtocol: FrontBusProtocol = new ChipkitFrontBusProtocol(m0generator)
  override val physicalMemoryBytes: Long = 1L << 22
  override val memorySpaceAddressBase: Long = 0x0
  override val memorySpaceSizeBytes: Long = physicalMemoryBytes
  override val memoryNChannels: Int = 1
  override val memoryControllerIDBits: Int = 4
  override val memoryControllerBeatBytes: Int = 4

  override val memoryCompiler: MemoryCompiler = technologyLibrary.memoryCompiler

  override def postProcessorMacro(c: Config, paths: Seq[Path]): Unit = technologyLibrary.postProcessorMacro(c, paths)
}
