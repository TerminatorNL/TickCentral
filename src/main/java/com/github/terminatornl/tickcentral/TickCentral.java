package com.github.terminatornl.tickcentral;

import com.github.terminatornl.tickcentral.asm.APITransformer;
import com.github.terminatornl.tickcentral.asm.BlockTransformer;
import com.github.terminatornl.tickcentral.asm.ITickableTransformer;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.IFMLCallHook;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.TransformerExclusions({
		"com.github.terminatornl.tickcentral.asm",
		"com.github.terminatornl.tickcentral.TickCentral",
		"com.github.terminatornl.tickcentral.Config",
		"com.github.terminatornl.tickcentral.ModContainer",
		"tickcentral.api." /* Intended for dependant mods, not here. */
})
@IFMLLoadingPlugin.Name(TickCentral.NAME)
@IFMLLoadingPlugin.SortingIndex(0)
public class TickCentral extends DummyModContainer implements IFMLLoadingPlugin, IFMLCallHook {
	public static final Config CONFIG = new Config();
	public static final String NAME = "TickCentral";
	public static final String MODID = "tickcentral";
	public static final Logger LOGGER = LogManager.getLogger(NAME);
	public static final String VERSION = "1.0";
	public static TickCentral INSTANCE;
	public static Map<String, Object> FML_DATA;

	public TickCentral() {
		INSTANCE = this;
	}

	/**
	 * Return a list of classes that implements the IClassTransformer interface
	 *
	 * @return a list of classes that implements the IClassTransformer interface
	 */
	@Override
	public String[] getASMTransformerClass() {
		return new String[]{
				ITickableTransformer.class.getName(),
				BlockTransformer.class.getName(),
				APITransformer.class.getName()
		};
	}

	@Override
	public String getModContainerClass() {
		return ModContainer.class.getName();
	}

	@Nullable
	@Override
	public String getSetupClass() {
		return getClass().getName();
	}

	/**
	 * Inject coremod data into this coremod
	 * This data includes:
	 * "mcLocation" : the location of the minecraft directory,
	 * "coremodList" : the list of coremods
	 * "coremodLocation" : the file this coremod loaded from,
	 *
	 * @param data
	 */
	@Override
	public void injectData(Map<String, Object> data) {
		FML_DATA = data;
	}

	/**
	 * Return an optional access transformer class for this coremod. It will be injected post-deobf
	 * so ensure your ATs conform to the new srgnames scheme.
	 *
	 * @return the name of an access transformer class or null if none is provided
	 */
	@Override
	public String getAccessTransformerClass() {
		return null;
	}

	/**
	 * Computes a result, or throws an exception if unable to do so.
	 *
	 * @return computed result
	 * @throws Exception if unable to compute a result
	 */
	@Override
	public Void call() throws Exception {
		return null;
	}

	@Override
	public String getModId() {
		return MODID;
	}

}
