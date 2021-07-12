import commands.gumroad_commands as gr

commands = {
    "link": {"admin": True, "invoke": gr.link_id_to_role}
}


async def handle(message):
    # Make sure sender is not a bot and that the message is a command
    if message.author.bot or not check_if_command(message.content):
        return

    # Get command and args
    command_and_args = extract_command_and_args(message.content)
    command_data = commands[command_and_args[0]]

    # Check to make sure we have appropriate perms to execute this command
    if command_data["admin"] and not message.author.guild_permissions.administrator:
        return

    # All checks passed. Invoke the command
    await command_data["invoke"](command_and_args[1], message)


def extract_command_and_args(message_content):
    split_message = message_content[1:].split(' ')
    return split_message[0], split_message[1:]


def check_if_command(message_content):
    return message_content.startswith("?")
