import discord
import commands.gumroad_commands as gr
from discord_utils import print_error

commands = {
    "link": {"admin": True, "invoke": gr.link_id_to_role},
    "unlink": {"admin": True, "invoke": gr.unlink_id},
    "alias": {"admin": True, "invoke": gr.create_gumroad_alias},
    "unalias": {"admin": True, "invoke": gr.delete_gumroad_alias},
    "verify": {"admin": False, "invoke": gr.verify_license},
}


async def handle(message):
    # Make sure sender is not a bot and that the message is a command and that the message was not sent in a DM
    if not message.content.startswith("?") or message.author.bot or message.channel is discord.DMChannel:
        return

    # Get command and args
    command_and_args = extract_command_and_args(message.content)
    command_data = commands[command_and_args[0]]

    # Check to make sure we have appropriate perms to execute this command
    if command_data["admin"] and not message.author.guild_permissions.administrator:
        await print_error(message.channel, "This command is available to Administrators only.")
        return

    # All checks passed. Invoke the command
    await command_data["invoke"](command_and_args[1], message)


def extract_command_and_args(message_content):
    split_message = message_content[1:].split(' ')
    return split_message[0], split_message[1:]
