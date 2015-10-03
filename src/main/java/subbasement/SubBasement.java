package subbasement;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import subbasement.reference.Metadata;
import subbasement.reference.Reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod(name = Reference.NAME, modid = Reference.ID, version = Reference.VERSION_FULL)
public class SubBasement
{
    private List<BlockedDimension> blockedDimensions = new ArrayList<BlockedDimension>();

    @Mod.Metadata(Reference.ID)
    public static ModMetadata metadata;

    @Mod.Instance(value = Reference.ID)
    public static SubBasement INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        metadata = Metadata.init(metadata);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @NetworkCheckHandler
    public final boolean networkCheck(Map<String, String> remoteVersions, Side side)
    {
        return true;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void placeBlock(BlockEvent.PlaceEvent event)
    {
        if (event.world == null || checkPlacementBlocked(event.world.provider.dimensionId, event.y))
        {
            event.setCanceled(true);
        }
    }

    public boolean checkPlacementBlocked(int dim, int y)
    {
        for (BlockedDimension dimension : blockedDimensions)
        {
            if (dimension.isBlocked(dim, y))
            {
                return true;
            }
        }
        return false;
    }

    private static class BlockedDimension
    {
        int maxDim, minDim, maxY, minY;

        public boolean isBlocked(int dim, int y)
        {
            return dim <= maxDim && dim >= minDim && y <= maxY && y >= minY;
        }
    }
}
