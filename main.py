import config_reader
import dynamo_helper
import discord
import command_handler

# Initialize modules
reader = config_reader.Config()
dynamo = dynamo_helper.DynamoHelper(reader.table_name)
client = discord.Client()


def initialize_bot(token):
    client.run(token)


@client.event
async def on_message(message):
    await command_handler.handle(message)


if __name__ == '__main__':
    initialize_bot(reader.token)
