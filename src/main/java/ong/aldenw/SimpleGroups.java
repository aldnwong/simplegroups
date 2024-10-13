package ong.aldenw;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleGroups implements ModInitializer {
	public static final String MOD_ID = "simple-groups";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		CommandRegistrationCallback.EVENT.register(CommandHandler::initialize);
		LOGGER.info("Simple Groups plugin initialized. :3");
	}
}