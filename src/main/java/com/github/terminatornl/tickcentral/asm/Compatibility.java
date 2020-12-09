package com.github.terminatornl.tickcentral.asm;

import com.github.terminatornl.tickcentral.TickCentral;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.asm.ASMTransformerWrapper;
import net.minecraftforge.fml.common.asm.transformers.*;

import java.lang.reflect.Field;
import java.util.*;

public class Compatibility {

	private static boolean applied = false;

	public static void FixTransformerOrdering() {
		if (applied) {
			return;
		} else {
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

	public static class OrderedArrayList extends ArrayList<IClassTransformer> {

		public static OrderedArrayList INSTANCE;
		private final Collection<Map.Entry<Class<? extends IClassTransformer>, Integer>> bias;
		private final Field transformerField;
		private boolean needsSorting = true;

		public OrderedArrayList(List<IClassTransformer> list, Collection<Map.Entry<Class<? extends IClassTransformer>, Integer>> bias) throws NoSuchFieldException {
			super(list);
			if (INSTANCE != null) {
				throw new IllegalStateException("Only one instance can exists!");
			}
			this.bias = bias;
			transformerField = ASMTransformerWrapper.TransformerWrapper.class.getDeclaredField("parent");
			transformerField.setAccessible(true);
			sortSelf();
			INSTANCE = this;
		}


		/**
		 * Runs all transformers on a class as if it was completely transformed by forge.
		 */
		public final byte[] transformAsPureFML(String name, String transformedName, byte[] basicClass) {
			Iterator<IClassTransformer> it = super.iterator();
			LOOP:
			while (it.hasNext()) {
				IClassTransformer transformer = it.next();
				boolean allowed
						 = transformer instanceof PatchingTransformer
						|| transformer instanceof SideTransformer
						|| transformer instanceof SoundEngineFixTransformer
						|| transformer instanceof DeobfuscationTransformer
						|| transformer instanceof AccessTransformer
						|| transformer instanceof FieldRedirectTransformer
						|| transformer instanceof TerminalTransformer
						|| transformer instanceof ModAPITransformer;
				if (allowed == false) {
					continue;
				}
				basicClass = transformer.transform(name, transformedName, basicClass);
			}
			return basicClass;
		}

		@Override
		public final boolean add(IClassTransformer transformer) {
			boolean val = super.add(transformer);
			needsSorting = true;
			return val;
		}

		@Override
		public final void add(int index, IClassTransformer transformer) {
			super.add(index, transformer);
			needsSorting = true;
		}

		// Called from LaunchClassLoader::runTransformers
		@Override
		public Iterator<IClassTransformer> iterator() {
			if (needsSorting)
				sortSelf();
			return super.iterator();
		}

		private void sortSelf() {
			this.sort((one, two) -> {
				if (one instanceof ASMTransformerWrapper.TransformerWrapper) {
					try {
						one = (IClassTransformer) transformerField.get(one);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
				if (two instanceof ASMTransformerWrapper.TransformerWrapper) {
					try {
						two = (IClassTransformer) transformerField.get(two);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
				int r = 0;

				for (Map.Entry<Class<? extends IClassTransformer>, Integer> b : bias) {
					if (b.getKey().isAssignableFrom(one.getClass())) {
						r = r + b.getValue();
					}
					if (b.getKey().isAssignableFrom(two.getClass())) {
						r = r - b.getValue();
					}
				}
				return r;
			});
			needsSorting = false;
		}
	}
}
