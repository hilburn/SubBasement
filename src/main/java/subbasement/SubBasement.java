package subbasement;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod(name = Reference.NAME, modid = Reference.ID, version = Reference.VERSION_FULL)
public class SubBasement
{
    public static final TypeAdapter<BlockedDimension> TYPE_ADAPTER = new TypeAdapter<BlockedDimension>()
    {
        @Override
        public void write(JsonWriter out, BlockedDimension value) throws IOException
        {
            out.beginObject();
            if (value.minDim == value.maxDim)
            {
                out.name("dim").value(value.minDim);
            } else
            {
                if (value.minDim != Integer.MIN_VALUE)
                {
                    out.name("minDim").value(value.minDim);
                }
                if (value.maxDim != Integer.MAX_VALUE)
                {
                    out.name("maxDim").value(value.maxDim);
                }
            }
            if (value.minY == value.maxY)
            {
                out.name("y").value(value.minY);
            } else
            {
                if (value.minY != Integer.MIN_VALUE)
                {
                    out.name("minY").value(value.minY);
                }
                if (value.maxY != Integer.MAX_VALUE)
                {
                    out.name("maxY").value(value.maxY);
                }
            }
            out.endObject();
        }

        @Override
        public BlockedDimension read(JsonReader in) throws IOException
        {
            BlockedDimension dimension = new BlockedDimension();
            in.beginObject();
            while (in.peek() != JsonToken.END_OBJECT)
            {
                String name = in.nextName();
                if (name.equalsIgnoreCase("minDim"))
                {
                    dimension.minDim = in.nextInt();
                } else if (name.equalsIgnoreCase("maxDim"))
                {
                    dimension.maxDim = in.nextInt();
                } else if (name.equalsIgnoreCase("dim"))
                {
                    dimension.minDim = in.nextInt();
                    dimension.maxDim = dimension.minDim;
                } else if (name.equalsIgnoreCase("minY"))
                {
                    dimension.minY = in.nextInt();
                } else if (name.equalsIgnoreCase("maxY"))
                {
                    dimension.maxY = in.nextInt();
                } else if (name.equalsIgnoreCase("y"))
                {
                    dimension.minY = in.nextInt();
                    dimension.maxY = dimension.minY;
                }
            }
            in.endObject();
            int minDim = Math.min(dimension.minDim, dimension.maxDim);
            dimension.maxDim = Math.max(dimension.minDim, dimension.maxDim);
            dimension.minDim = minDim;
            int minY = Math.min(dimension.minY, dimension.maxY);
            dimension.maxY = Math.max(dimension.minY, dimension.maxY);
            dimension.minY = minY;
            return dimension;
        }
    };
    private static Gson GSON = new GsonBuilder().registerTypeAdapter(BlockedDimension.class, TYPE_ADAPTER).setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES).create();
    private static List<BlockedDimension> blockedDimensions = new ArrayList<BlockedDimension>();

    @Mod.Metadata(Reference.ID)
    public static ModMetadata metadata;

    @Mod.Instance(value = Reference.ID)
    public static SubBasement INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        metadata = Metadata.init(metadata);
        MinecraftForge.EVENT_BUS.register(this);
        load(event.getModConfigurationDirectory());
    }

    public static void load(File file)
    {
        try
        {
            File save = new File(file, Reference.ID + ".json");
            if (!save.exists()) save.createNewFile();
            JsonReader reader = new JsonReader(new FileReader(save));
            Type type = new TypeToken<List<BlockedDimension>>(){}.getType();
            blockedDimensions = GSON.fromJson(reader, type);
            reader.close();
            if (blockedDimensions == null)
            {
                blockedDimensions = new ArrayList<BlockedDimension>();
            }
            FileWriter fileWriter = new FileWriter(save);
            GSON.toJson(blockedDimensions, fileWriter);
            fileWriter.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
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

    public static boolean checkPlacementBlocked(int dim, int y)
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
        
        int maxDim = Integer.MAX_VALUE, minDim = Integer.MIN_VALUE, maxY = Integer.MAX_VALUE, minY = Integer.MIN_VALUE;

        public boolean isBlocked(int dim, int y)
        {
            return dim <= maxDim && dim >= minDim && y <= maxY && y >= minY;
        }
    }
}
