package com.benaclejames.gumcord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;

public class Main {
    public static JDA jda;

    //public static final Table table = dynamo.getTable("GumCord");
    public static void main(String[] args) throws LoginException {
        jda = JDABuilder.createDefault(args[0]).enableIntents(GatewayIntent.MESSAGE_CONTENT).addEventListeners(new CommandHandler()).build();
    }
}
