package com.github.terminatornl.tickcentral;

import com.github.terminatornl.tickcentral.asm.Transformer;
import net.minecraft.block.BlockBush;
import net.minecraftforge.fml.relauncher.IFMLCallHook;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.TransformerExclusions({
        "com.github.terminatornl.tickcentral.asm",
        "tickcentral.api." /* Intended for dependant mods, not here. */
})
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name(TickCentral.NAME)
@IFMLLoadingPlugin.SortingIndex(0)
public class TickCentral implements IFMLLoadingPlugin, IFMLCallHook {
    public static final String NAME = "TickCentral";
    public static final Logger LOGGER = LogManager.getLogger(NAME);
    public static Map<String, Object> FML_DATA;

    /**
     * Return a list of classes that implements the IClassTransformer interface
     *
     * @return a list of classes that implements the IClassTransformer interface
     */
    @Override
    public String[] getASMTransformerClass() {
        return new String[]{Transformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
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
        //TODO: LOAD CONFIG FROM OTHER MODS
        return null;
    }
}
