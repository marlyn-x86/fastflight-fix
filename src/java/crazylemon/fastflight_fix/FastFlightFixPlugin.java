package crazylemon.fastflight_fix;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.TransformerExclusions({"crazylemon.fastflight_fix"})
public class FastFlightFixPlugin implements IFMLLoadingPlugin {

	@Override
	public String[] getASMTransformerClass() {
		return new String[] {"crazylemon.fastflight_fix.FastFlightFixClassTransformer"};
	}

	@Override
	public String getModContainerClass() {
		return "crazylemon.fastflight_fix.FastFlightFixModContainer";
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
