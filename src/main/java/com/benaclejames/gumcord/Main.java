package com.benaclejames.gumcord;

import com.benaclejames.gumcord.interactions.ButtonHandler;
import com.benaclejames.gumcord.interactions.InteractionHandler;
import com.benaclejames.gumcord.interactions.SelectMenu.PaginatedSelectMenu;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;

public class Main {
    private static JDA jda;

    public static JDA getJda() {
        return jda;
    }

    public static void main(String[] args) throws LoginException {
        jda = JDABuilder.createDefault(args[0])
                .addEventListeners(
                        new SetupHandler(),
                        new InteractionHandler(),
                        new ButtonHandler(),
                        new PaginatedSelectMenu.PaginatedSelectMenuButtonHandler())
                .setActivity(Activity.of(Activity.ActivityType.WATCHING, "improved dropdowns dialogs!"))
                .build();
    }
}

