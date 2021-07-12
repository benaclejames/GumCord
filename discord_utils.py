def sanitize_role(role_str):
    if role_str.startswith("<@&") and role_str.endswith(">"):
        return role_str[3:-1]
    else:
        return role_str
