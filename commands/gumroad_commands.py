from discord import Forbidden
from discord_utils import sanitize_role
from main import dynamo
import gumroad


async def link_id_to_role(args, ctx):
    role_id = sanitize_role(args[1])
    if not role_id or not ctx.guild.get_role(role_id):
        await ctx.channel.send("You must provide a valid role mention or role ID")
        return

    if gumroad.verify_product_exists(args[0]):
        gumroad_id = args[0]
    else:
        await ctx.channel.send("The provided Gumroad ID doesn't exist.")
        return

    dynamo.set_gumroad_to_role(ctx.guild.id, gumroad_id, role_id)
    await ctx.channel.send("Successfully linked Gumroad ID!")


async def unlink_id(args, ctx):
    gumroad_id = args[0]

    if dynamo.get_gumroad_to_role(ctx.guild.id, gumroad_id) is None:
        await ctx.channel.send("The specified Gumroad ID hasn't been linked in this server and cannot be unlinked.")
        return

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
        await ctx.channel.send("This Gumroad ID/Alias is missing a role!")
        return

    role = ctx.guild.get_role(role_id_to_assign)    # Make sure role still exists
    if not role:
        await ctx.channel.send("Linked role no longer exists!")
        return

    # Make sure the key provided is actually valid
    if not gumroad.verify_license(gumroad_id, license_id):
        await ctx.channel.send("This license key is invalid.")
        return

    # All checks succeeded, add the role!
    try:
        await ctx.author.add_roles(role)
        await ctx.channel.send("Successfully verified license!")
    except Forbidden:
        await ctx.channel.send("Error! Successfully verified but failed to add role. Make sure GumCord has the 'Manage "
                               "Roles' permission and is higher on the role list than the target role.")
