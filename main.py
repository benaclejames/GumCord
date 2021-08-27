import config_reader
import dynamo_helper
import discord
import command_handler

# Initialize modules
reader = config_reader.Config()
dynamo = dynamo_helper.DynamoHelper(reader.table_name)
client = discord.Client()


@client.event
async def on_ready():
    print("Connected to Discord!")


@client.event
async def on_message(message):
    await command_handler.handle(message)


@client.event
async def on_guild_join(guild):
    dynamo.create_server(guild.id)


if __name__ == '__main__':
    client.run(reader.token)
