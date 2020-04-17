package com.github.terminatornl.tickcentral.asm.template;

import com.github.terminatornl.tickcentral.api.TickHub;
import net.minecraft.block.Block;
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
public class Redirections {

	public static void trueUpdateTick(Block block, World worldIn, BlockPos pos, IBlockState state, Random random){
		block.updateTick(worldIn, pos, state, random);
	}

	/**
	 * Will be populated with the true update method using ASM
	 * @param block the block
	 * @param worldIn the world
	 * @param pos the block pos
	 * @param state the state of the block
	 * @param random random
	 */
	public native static void trueRandomTick(Block block, World worldIn, BlockPos pos, IBlockState state, Random random);

	/**
	 * Will be populated with the true update method using ASM
	 * @param tickable the tickable
	 */
	public native static void trueUpdate(ITickable tickable);
}
