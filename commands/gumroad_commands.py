import discord
from discord import Embed, Forbidden
from discord_utils import sanitize_role, print_error
from main import dynamo, client
import gumroad


async def link_id_to_role(args, ctx):
    role_id = sanitize_role(args[1])
    if not role_id or not ctx.guild.get_role(role_id):
        await print_error(ctx.channel, "You must provide a valid role mention or role ID")
        return

    if gumroad.verify_product_exists(args[0]):
        gumroad_id = args[0]
    else:
        await print_error(ctx.channel, "The provided Gumroad ID doesn't exist.")
        return

    dynamo.set_gumroad_to_role(ctx.guild.id, gumroad_id, role_id)
    embed = Embed(color=0x4E5D94)
    embed.add_field(name="Link Modification", value="Successfully linked Gumroad ID!",
                    inline=True)
    embed.set_footer(text="GumCord")
    await ctx.channel.send(embed=embed)


async def unlink_id(args, ctx):
    gumroad_id = args[0]

    if dynamo.get_gumroad_to_role(ctx.guild.id, gumroad_id) is None:
        await print_error(ctx.channel, "The specified Gumroad ID isn't linked in this server.")
        return

    dynamo.del_gumroad_to_role(ctx.guild.id, gumroad_id)
    dynamo.delete_server_commands_for_gumroad_id(ctx.guild.id, gumroad_id)
    embed = Embed(color=0x4E5D94)
    embed.add_field(name="Link Modification", value="Successfully unlinked Gumroad ID and it's associated Aliases!",
                    inline=True)
    embed.set_footer(text="GumCord")
    await ctx.channel.send(embed=embed)


async def create_gumroad_alias(args, ctx):
    alias = args[0].lower()
    gumroad_id = args[1]

    if dynamo.get_gumroad_to_role(ctx.guild.id, gumroad_id) is None:
        await print_error(ctx.channel,
                          "You must assign this Gumroad ID to a role before making any aliases linking to it.")
        return

    dynamo.store_server_command(ctx.guild.id, alias, gumroad_id)
    embed = Embed(color=0x4E5D94)
    embed.add_field(name="Alias Modified", value="Successfully created Gumroad alias!", inline=True)
    embed.set_footer(text="GumCord")
    await ctx.channel.send(embed=embed)


async def delete_gumroad_alias(args, ctx):
    alias = args[0].lower()

    dynamo.delete_server_command(ctx.guild.id, alias)
    embed = Embed(color=0x4E5D94)
    embed.add_field(name="Alias Modified", value="Successfully removed Gumroad alias!", inline=True)
    embed.set_footer(text="GumCord")
    await ctx.channel.send(embed=embed)


async def verify_license(args, ctx):
    if ctx.message.channel is discord.DMChannel:
        # TODO: Check all known servers for the user. For each server the user is in, run the verification as if they
        #  requested it from that server.
        await print_error(ctx.message.channel, "This command is not available in DMs... ***Yet***")
        return

    gumroad_id = dynamo.get_command_to_gumroad(ctx.guild.id, args[0])  # Check if this is an alias
    if gumroad_id is None:
        gumroad_id = args[0]

    license_id = args[1]
    role_id_to_assign = dynamo.get_gumroad_to_role(ctx.guild.id, gumroad_id)  # Make sure we have a role to assign
    if role_id_to_assign is None:
        await print_error(ctx.channel, "This Gumroad ID/Alias is missing a role!")
        return

    role = ctx.guild.get_role(role_id_to_assign)  # Make sure role still exists
    if not role:
        await print_error(ctx.channel, "Linked role no longer exists!")
        return

    # Make sure the user doesn't already have the role
    if role in ctx.author.roles:
        await print_error(ctx.channel, "You already have this role!")
        return

    # Make sure the key provided is actually valid
    if not gumroad.verify_license(gumroad_id, license_id):
        await print_error(ctx.channel, "This license key is invalid.")
        return

    # All checks succeeded, add the role!
    try:
        await ctx.author.add_roles(role)
        embed = Embed(color=0x2fdf0c)
        embed.add_field(name="Verification Success", value="Role Added!", inline=True)
        embed.set_footer(text="GumCord")
        await ctx.channel.send(embed=embed)
    except Forbidden:
        await print_error(ctx.channel, "Failed to add role. Make sure GumCord has the 'Manage "
                                       "Roles' permission and is higher on the role list than the target role.")
