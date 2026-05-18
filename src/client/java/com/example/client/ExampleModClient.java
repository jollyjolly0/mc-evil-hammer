package com.example.client;

import com.example.client.necromancer.NecromancerKeybindings;
import net.fabricmc.api.ClientModInitializer;

public class ExampleModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		NecromancerKeybindings.initialize();
	}
}
