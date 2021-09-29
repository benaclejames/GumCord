from discord import Embed


def sanitize_role(role_str):
    return_role = role_str
    if not return_role.isnumeric():
        return_role = ""
        for i, c in enumerate(role_str):
            if c.isdigit():
                return_role += c

    return int(return_role) if return_role and return_role.isnumeric() else None


async def print_error(ctx, message):
    embed = Embed(color=0xdf2b0c)
    embed.add_field(name="Error", value=message, inline=False)
    embed.set_footer(text="GumCord")
    try:
        await ctx.send(embed=embed)
    except:
        await ctx.send("Error! "+message)
