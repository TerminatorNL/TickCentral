package com.github.terminatornl.tickcentral.core;

public interface TrueITickable{
	default void update(){
		this.update();
	}
}
