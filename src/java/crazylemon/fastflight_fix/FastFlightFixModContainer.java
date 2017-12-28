package crazylemon.fastflight_fix;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.StringFormatterMessageFactory;

import com.google.common.eventbus.EventBus;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

public class FastFlightFixModContainer extends DummyModContainer {

    private static final Logger logger = LogManager.getLogger("FastFlightFix", StringFormatterMessageFactory.INSTANCE);

	public FastFlightFixModContainer() {
		super(new ModMetadata());
		ModMetadata meta = getMetadata();
		meta.modId = "fastflight_fix";
		meta.name = "Fast Flight Fix";
		meta.description = "Removes the fast movement restriction on non-server-owners.";
		meta.version = "1.12.2-1.0";
		meta.authorList = Arrays.asList("Crazylemon");
	}

    public static void logInfo(String formattableString, Object... objects) {
        logger.info(formattableString, objects);
    }

    /**
     * Default 'crash Minecraft' method.
     *
     * @param string Reason why we are forcing Minecraft to crash
     */
    public static void die(String reason) throws PatchFailedException {
        throw new PatchFailedException("[FastFlightFix] " + reason);
    	
    }

	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		bus.register(this);
		return true;
	}
}
