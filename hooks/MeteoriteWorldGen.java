package appeng.hooks;

import java.util.Random;
import java.util.concurrent.Future;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import appeng.api.features.IWorldGen.WorldGenType;
import appeng.api.util.DimensionalCoord;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.WorldSettings;
import appeng.core.features.registries.WorldGenRegistry;
import appeng.helpers.MeteoritePlacer;
import appeng.services.helpers.ICompassCallback;
import cpw.mods.fml.common.IWorldGenerator;

final public class MeteoriteWorldGen implements IWorldGenerator
{

	class myGen implements ICompassCallback
	{

		double distance = 0;

		@Override
		public void calculatedDirection(boolean hasResult, boolean spin, double radians, double dist)
		{
			if ( hasResult )
				distance = dist;
			else
				distance = Double.MAX_VALUE;
		}

	};

	@Override
	public void generate(Random r, int chunkX, int chunkZ, World w, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
	{
		if ( WorldGenRegistry.instance.isWorldGenEnabled( WorldGenType.Metorites, w ) && r.nextFloat() > 0.9 )
		{
			int x = r.nextInt( 16 ) + (chunkX << 4);
			int z = r.nextInt( 16 ) + (chunkZ << 4);

			myGen obj = new myGen();
			Future<?> future = WorldSettings.getInstance().getCompass().getCompassDirection( new DimensionalCoord( w, x, 128, z ), 70, obj );

			try
			{
				future.get();

				if ( obj.distance > AEConfig.instance.minMeteoriteDistanceSq )
				{
					int depth = 180 + r.nextInt( 20 );
					for (int trys = 0; trys < 20; trys++)
					{
						MeteoritePlacer mp = new MeteoritePlacer();

						if ( mp.spawnMeteorite( w, x, depth, z ) )
							return;

						depth -= 15;
						if ( depth < 40 )
							return;

					}
				}

			}
			catch (Throwable e)
			{
				AELog.error( e );
			}

		}
	}
}
