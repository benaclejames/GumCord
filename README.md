# 🔌 GumCord
Open-Source Discord Bot to verify GumRoad license keys and assign roles.

## 💌 [Invite](https://discord.com/oauth2/authorize?client_id=864170397632299040&permissions=137707777216&scope=bot+applications.commands)
The bot needs to exist in your guild to read and modify roles for users. This might not be required in the future but for now it's necessary.

## 📜 Slash Commands

| Command       | Parameters                                                         | Permission        | Description                                            |
|---------------|--------------------------------------------------------------------|-------------------|--------------------------------------------------------|
| `spawnverify` |                                                                    | `MANAGE_CHANNEL` `MODERATE_MEMBERS` | Spawns a verification button where the command is run. |
| `linkrole`    | `Gumroad ID`, `Role to Apply`, `Alias to be used in verify dialogs` | `MANAGE_ROLES`    | Links a GumRoad product ID and alias to a role.        |
| `unlinkrole`  | `Gumroad ID`                                                       | `MANAGE_ROLES`    | Unlinks a GumRoad ID from a role.                      |
