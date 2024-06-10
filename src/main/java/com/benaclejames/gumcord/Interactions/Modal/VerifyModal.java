package com.benaclejames.gumcord.Interactions.Modal;

import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.internal.interactions.modal.ModalImpl;

import java.util.Collections;
import java.util.List;

public class VerifyModal extends ModalImpl {

    public VerifyModal(String id, String title) {
        super("verifymodal_" + id, ComputeTitle(title), ConstructModalComponents());
    }

    private static String ComputeTitle(String productName) {
        String computedName = productName;

        // If the productname is longer than 45 characters, add an ellipsis
        if (productName.length() > 45)
            computedName = productName.substring(0, 42) + "...";

        // If prefixing the productname with "Verify License Key for " makes it longer than 45 characters, don't prefix it
        if (productName.length() + 24 <= 45)
            computedName =  "Verify License Key for " + productName;

        return computedName;
    }

    private static List<LayoutComponent> ConstructModalComponents() {
        TextInput subject = TextInput.create("key", "License Key", TextInputStyle.SHORT)
                .setPlaceholder("12345678-12345678-12345678-12345678")
                .setRequiredRange(35, 35)
                .setRequired(true)
                .build();

        return Collections.singletonList(ActionRow.of(subject));
    }
}
