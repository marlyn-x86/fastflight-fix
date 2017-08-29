package crazylemon.fastflight_fix;

import java.util.Arrays;

import com.google.common.eventbus.EventBus;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

public class FastFlightFixModContainer extends DummyModContainer {

	public FastFlightFixModContainer() {
		super(new ModMetadata());
		ModMetadata meta = getMetadata();
		meta.modId = "fastflight_fix";
		meta.name = "Fast Flight Fix";
		meta.description = "Removes the fast movement restriction on non-server-owners.";
		meta.version = "1.10.2-1.0";
		meta.authorList = Arrays.asList("Crazylemon");
	}

	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		bus.register(this);
		return true;
	}
}
