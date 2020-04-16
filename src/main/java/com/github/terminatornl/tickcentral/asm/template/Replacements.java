package com.github.terminatornl.tickcentral.asm.template;

import com.github.terminatornl.tickcentral.api.TickHub;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

/**
 * This class is not actually parsed. This merely represents the final product after ASM transformation.
 */
public class Replacements {

	public static abstract class Block extends net.minecraft.block.Block {

		public Block(Material materialIn) {
			super(materialIn);
			throw new RuntimeException();
		}

		@Override
		public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random random){
			TickHub.EXECUTOR.redirectUpdateTick(this, worldIn, pos, state, random);
		}

		@Override
		@ParametersAreNonnullByDefault
		public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random){
			TickHub.EXECUTOR.redirectRandomTick(this, worldIn, pos, state, random);
		}
	}

	public static abstract class ITickable implements net.minecraft.util.ITickable {
		@Override
		public void update(){
			TickHub.EXECUTOR.redirectUpdate(this);
		}
	}
}
