from discord_utils import sanitize_role
from main import dynamo
import gumroad


async def link_id_to_role(args, ctx):
    if gumroad.verify_product_exists(args[0]):
        gumroad_id = args[0]
    else:
        await ctx.channel.send("The provided Gumroad ID doesn't exist.")
        return

    role_tag = sanitize_role(args[1])

    dynamo.set_gumroad_to_role(ctx.guild.id, gumroad_id, role_tag)
    await ctx.channel.send("Successfully linked Gumroad ID!")


async def unlink_id(args, ctx):
    gumroad_id = args[0]

    dynamo.del_gumroad_to_role(ctx.guild.id, gumroad_id)
    dynamo.delete_server_commands_for_gumroad_id(ctx.guild.id, gumroad_id)
    await ctx.channel.send("Successfully unlinked Gumroad ID and associated Aliases!")


async def create_gumroad_alias(args, ctx):
    alias = args[0].lower()
    gumroad_id = args[1]
    if dynamo.get_gumroad_to_role(ctx.guild.id, gumroad_id) is None:
        await ctx.channel.send("You must assign this Gumroad ID to a role before making any aliases linking to it.")
        return

    dynamo.store_server_command(ctx.guild.id, alias, gumroad_id)
    await ctx.channel.send("Successfully created Gumroad alias!")

async def delete_gumroad_alias(args, ctx):
    alias = args[0].lower()

    dynamo.delete_server_command(ctx.guild.id, alias)
    await ctx.channel.send("Successfully removed Gumroad alias!")


async def verify_license(args, ctx):
    gumroad_id = dynamo.get_command_to_gumroad(ctx.guild.id, args[0])  # Check if this is an alias
    if gumroad_id is None:
        gumroad_id = args[0]

    license_id = args[1]
    role_id_to_assign = dynamo.get_gumroad_to_role(ctx.guild.id, gumroad_id)  # Make sure we have a role to assign
    if role_id_to_assign is None:
        await ctx.channel.send("This Gumroad ID/Alias hasn't been linked to a role yet!")
        return

    gumroad.verify_license(gumroad_id, license_id)
