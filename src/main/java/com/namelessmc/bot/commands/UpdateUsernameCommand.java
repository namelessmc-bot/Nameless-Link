package com.namelessmc.bot.commands;

import java.util.Collections;
import java.util.Optional;

import com.namelessmc.bot.Language;
import com.namelessmc.bot.Main;
import com.namelessmc.bot.connections.BackendStorageException;
import com.namelessmc.java_api.ApiError;
import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class UpdateUsernameCommand extends Command {

	public UpdateUsernameCommand() {
		super("!updateusername", Collections.singletonList("!usernameupdate"), CommandContext.GUILD_MESSAGE);
	}

	@Override
	public void execute(final User user, final String[] args, final Message message) {
		final Guild guild = ((TextChannel) message.getChannel()).getGuild();
		
		Optional<NamelessAPI> optApi;
		try {
			optApi = Main.getConnectionManager().getApi(guild.getIdLong());
		} catch (final BackendStorageException e) {
			message.reply(Language.DEFAULT.get("error_generic")).queue();
			return;
		}
		
		if (optApi.isEmpty()) {
			message.reply(Language.DEFAULT.get("error_not_set_up")).queue();
			return;
		}
		
		final NamelessAPI api = optApi.get();
		
		final Language language = Language.getDiscordUserLanguage(api, user);
		
		Optional<NamelessUser> optNameless;
		try {
			optNameless = api.getUserByDiscordId(user.getIdLong());
		} catch (final NamelessException e) {
			message.reply(language.get("error_website_connection")).queue();;
			return;
		}
		
		if (optNameless.isEmpty()) {
			message.reply(language.get("error_not_linked")).queue();
			return;
		}

		try {
			api.updateDiscordUsernames(new long[] {user.getIdLong()}, new String[] {user.getName()});
		} catch (final ApiError e) {
			if (e.getError() == ApiError.UNABLE_TO_FIND_USER) {
				message.reply(language.get("error_not_linked")).queue();
				return;
			} else {
				System.err.println("Error code " + e.getError() + " while updating username");
				message.reply(language.get("error_generic")).queue();
				return;
			}
		} catch (final NamelessException e) {
			message.reply(language.get("error_website_connection")).queue();
			return;
		}
		
		message.addReaction("U+2705").queue(); // ✅
	}

}
