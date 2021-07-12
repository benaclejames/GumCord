def sanitize_role(role_str):
    return_role = role_str
    if not return_role.isnumeric():
        return_role = ""
        for i, c in enumerate(role_str):
            if c.isdigit():
                return_role += c

    return int(return_role) if return_role and return_role.isnumeric() else None
