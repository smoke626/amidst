package amidst.mojangapi.world;

import java.io.FileNotFoundException;
import java.io.IOException;

import amidst.documentation.Immutable;
import amidst.mojangapi.file.directory.SaveDirectory;
import amidst.mojangapi.file.nbt.LevelDat;
import amidst.mojangapi.minecraftinterface.MinecraftInterface;
import amidst.mojangapi.minecraftinterface.RecognisedVersion;
import amidst.mojangapi.world.icon.NetherFortressProducer;
import amidst.mojangapi.world.icon.OceanMonumentProducer;
import amidst.mojangapi.world.icon.PlayerProducer;
import amidst.mojangapi.world.icon.SpawnProducer;
import amidst.mojangapi.world.icon.StrongholdProducer;
import amidst.mojangapi.world.icon.TempleProducer;
import amidst.mojangapi.world.icon.VillageProducer;
import amidst.mojangapi.world.oracle.BiomeDataOracle;
import amidst.mojangapi.world.oracle.SlimeChunkOracle;
import amidst.mojangapi.world.player.MovablePlayerList;
import amidst.mojangapi.world.player.WorldPlayerType;
import amidst.utilities.GoogleTracker;

@Immutable
public class WorldBuilder {
	private final GoogleTracker googleTracker;

	public WorldBuilder(GoogleTracker googleTracker) {
		this.googleTracker = googleTracker;
	}

	public World fromSeed(MinecraftInterface minecraftInterface,
			WorldSeed seed, WorldType worldType) {
		return create(minecraftInterface, seed, worldType, "",
				MovablePlayerList.dummy());
	}

	public World fromFile(MinecraftInterface minecraftInterface,
			SaveDirectory saveDirectory) throws FileNotFoundException,
			IOException {
		LevelDat levelDat = saveDirectory.createLevelDat();
		MovablePlayerList movablePlayerList = new MovablePlayerList(
				saveDirectory, isSaveEnabled(minecraftInterface),
				WorldPlayerType.from(saveDirectory, levelDat));
		return create(minecraftInterface,
				WorldSeed.fromFile(levelDat.getSeed()),
				levelDat.getWorldType(), levelDat.getGeneratorOptions(),
				movablePlayerList);
	}

	// TODO: why does it depend on the loaded minecraft version whether we can
	// save player locations or not?
	@Deprecated
	private boolean isSaveEnabled(MinecraftInterface minecraftInterface) {
		return minecraftInterface.getRecognisedVersion().isSaveEnabled();
	}

	private World create(MinecraftInterface minecraftInterface, WorldSeed seed,
			WorldType worldType, String generatorOptions,
			MovablePlayerList movablePlayerList) {
		googleTracker.trackSeed(seed);
		// @formatter:off
		minecraftInterface.createWorld(seed.getLong(), worldType, generatorOptions);
		RecognisedVersion recognisedVersion = minecraftInterface.getRecognisedVersion();
		BiomeDataOracle biomeDataOracle = new BiomeDataOracle(minecraftInterface);
		return new World(
				seed,
				worldType,
				generatorOptions,
				movablePlayerList,
				biomeDataOracle,
				new SlimeChunkOracle(      seed.getLong()),
				new SpawnProducer(         recognisedVersion, seed.getLong(), biomeDataOracle),
				new StrongholdProducer(    recognisedVersion, seed.getLong(), biomeDataOracle),
				new PlayerProducer(        recognisedVersion, movablePlayerList),
				new TempleProducer(        recognisedVersion, seed.getLong(), biomeDataOracle),
				new VillageProducer(       recognisedVersion, seed.getLong(), biomeDataOracle),
				new OceanMonumentProducer( recognisedVersion, seed.getLong(), biomeDataOracle),
				new NetherFortressProducer(recognisedVersion, seed.getLong(), biomeDataOracle));
		// @formatter:on
	}
}
