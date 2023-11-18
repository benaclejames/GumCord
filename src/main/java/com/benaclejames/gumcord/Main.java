package com.benaclejames.gumcord;

import com.benaclejames.gumcord.Interactions.ButtonHandler;
import com.benaclejames.gumcord.Interactions.InteractionHandler;
import com.benaclejames.gumcord.Interactions.SelectMenu.PaginatedSelectMenu;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class Main {
    public static JDA jda;

    public static void main(String[] args) throws LoginException {
        jda = JDABuilder.createDefault(args[0])
                .addEventListeners(
                        new SetupHandler(),
                        new InteractionHandler(),
                        new ButtonHandler(),
                        new PaginatedSelectMenu.PaginatedSelectMenuButtonHandler())
                .build();
    }
}

