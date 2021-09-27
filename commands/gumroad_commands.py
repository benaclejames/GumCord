import discord
from discord import Embed, Forbidden, DMChannel
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


# Actually finds the requred info about the licence we're trying to verify.
async def verify_license(guild, member, command, token):
    gumroad_id = dynamo.get_command_to_gumroad(guild.id, command)  # Check if this is an alias
    if gumroad_id is None:
        gumroad_id = command

    role_id_to_assign = dynamo.get_gumroad_to_role(guild.id, gumroad_id)  # Make sure we have a role to assign
    if role_id_to_assign is None:
        return False, "This Gumroad ID/Alias is missing a role!"

    role = guild.get_role(role_id_to_assign)  # Make sure role still exists
    if not role:
        return False, "Linked role no longer exists!"

    # Make sure the user doesn't already have the role
    if role in member.roles:
        return False, "You already have this role!"

    if dynamo.already_contains_token(guild.id, token):
        return False, "This key has already been used by someone else."

    # Make sure the key provided is actually valid
    if not gumroad.verify_license(gumroad_id, token):
        return False, "This license key is invalid."

    dynamo.invalidate_license(guild.id, token)
    return True, role


# Handles the INVOCATION of the command
async def verify_license_handler(args, ctx):
    if isinstance(ctx.channel, discord.DMChannel):
        # TODO: Check all known servers for the user. For each server the user is in, run the verification as if they
        #  requested it from that server.
        await print_error(ctx.channel, "License Verification isn't supported in DMs... **Yet**")
        return
        applicable_roles = []
        for guild in client.guilds:
            found_member = await guild.get_member(ctx.author.id)
            if found_member:
                gumroad_result = await verify_license(guild, ctx.author, args[0], args[1])
                if gumroad_result[0]:
                    applicable_roles.append(gumroad_result[1])
                    await ctx.author.add_roles(gumroad_result[1])

        if len(applicable_roles):
            guilds = set([])
            for role in applicable_roles:
                guilds.add(role.guild)
            embed = Embed(color=0x2fdf0c)
            embed.add_field(name="Verification Success", value=str(len(applicable_roles))+" roles successfully added in "+str(len(guilds))+" different guilds!", inline=True)
            embed.set_footer(text="GumCord")
            await ctx.channel.send(embed=embed)
        else:
            embed = Embed(color=0x4E5D94)
            embed.add_field(name="Verification Warning",
                            value="No roles were added, either because the provided license key is invalid, "
                                  "or because you're not part of any guilds that have a linked role.", inline=True)
            embed.set_footer(text="GumCord")
            await ctx.channel.send(embed=embed)
        return

    gumroad_result = await verify_license(ctx.guild, ctx.author, args[0], args[1])

    if gumroad_result[0]:
        # All checks succeeded, add the role!
        try:
            await ctx.author.add_roles(gumroad_result[1])
            embed = Embed(color=0x2fdf0c)
            embed.add_field(name="Verification Success", value="Role Added!", inline=True)
            embed.set_footer(text="GumCord")
            await ctx.channel.send(embed=embed)
        except Forbidden:
            await print_error(ctx.channel, "Failed to add role. Make sure GumCord has the 'Manage "
                                           "Roles' permission and is higher on the role list than the target role.")
    else:
        await print_error(ctx.channel, gumroad_result[1])
