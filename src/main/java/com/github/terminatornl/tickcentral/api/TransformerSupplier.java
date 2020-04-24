package com.github.terminatornl.tickcentral.api;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.IFMLCallHook;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Must have a constructor without arguments.
 */
public interface TransformerSupplier extends IFMLCallHook {

	default void onLoad(LaunchClassLoader loader, Side side){

	}

	/**
	 * @return The order in which to trigger the {@link IFMLCallHook} calls  in relation to other TickCentral projects.
	 */
	@SuppressWarnings("SameReturnValue")
	default int callOrder(){
		return 0;
	}

	/**
	 * @return all the transformers that will be added to forge. Call order is _not_ preserved.
	 * These transformers are called last in the entire transformation process (Even after mixin)
	 */
	@Nonnull
	default Collection<Class<? extends IClassTransformer>> getLastTransformers(){
		return Collections.emptyList();
	}

	/**
	 * @return all the transformers that will be added to forge.
	 * If the class is an instance of a class defined in {@link #getLastTransformers()} it will be applied last.
	 * Valid input is something like: TransformerSupplier.class.getName()
	 */
	@Nonnull
	default Collection<String> getTransformers(){
		return Collections.emptyList();
	}

	/**
	 * Injected with data from the FML environment:
	 * "classLoader" : The FML Class Loader
	 *
	 * @param data data obtained from forge
	 */
	@Override
	default void injectData(Map<String, Object> data){

	}

	/**
	 * Computes a result, or throws an exception if unable to do so.
	 *
	 * @return computed result
	 * @throws Exception if unable to compute a result
	 */
	@SuppressWarnings("RedundantThrows")
	@Override
	default Void call() throws Exception{
		return null;
	}
}
