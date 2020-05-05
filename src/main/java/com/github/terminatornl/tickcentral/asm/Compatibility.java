package com.github.terminatornl.tickcentral.asm;

import com.github.terminatornl.tickcentral.TickCentral;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.asm.ASMTransformerWrapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Compatibility{

	private static boolean applied = false;

	public static void FixTransformerOrdering(){
		if(applied){
			return;
		}else{
			applied = true;
		}
		try {
			Field transformerField = LaunchClassLoader.class.getDeclaredField("transformers");
			transformerField.setAccessible(true);

			@SuppressWarnings("unchecked")
			List<IClassTransformer> transformers = (List<IClassTransformer>) transformerField.get(Compatibility.class.getClassLoader());
			transformerField.set(Compatibility.class.getClassLoader(), new OrderedArrayList(transformers, TickCentral.INSTANCE.getPrioritizedASMTransformers()));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static class OrderedArrayList extends ArrayList<IClassTransformer>{

		private final Collection<Map.Entry<Class<? extends IClassTransformer>,Integer>> bias;
		private final Field transformerField;

		public OrderedArrayList(List<IClassTransformer> list, Collection<Map.Entry<Class<? extends IClassTransformer>,Integer>> bias) throws NoSuchFieldException {
			super(list);
			this.bias = bias;
			transformerField = ASMTransformerWrapper.TransformerWrapper.class.getDeclaredField("parent");
			transformerField.setAccessible(true);
			sortSelf();
		}

		@Override
		public final boolean add(IClassTransformer transformer) {
			boolean val = super.add(transformer);
			sortSelf();
			return val;
		}

		@Override
		public final void add(int index, IClassTransformer transformer) {
			super.add(index, transformer);
			sortSelf();
		}

		private void sortSelf(){
			this.sort((one, two) -> {
				if(one instanceof ASMTransformerWrapper.TransformerWrapper){
					try {
						one = (IClassTransformer) transformerField.get(one);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
				if(two instanceof ASMTransformerWrapper.TransformerWrapper){
					try {
						two = (IClassTransformer) transformerField.get(two);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
				int r = 0;

				for (Map.Entry<Class<? extends IClassTransformer>, Integer> b : bias) {
					if(b.getKey().isAssignableFrom(one.getClass())){
						r = r + b.getValue();
					}
					if(b.getKey().isAssignableFrom(two.getClass())){
						r = r - b.getValue();
					}
				}
				return r;
			});
		}
	}
}
