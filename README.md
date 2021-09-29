# 🔌 GumCord
WIP Discord bot to verify GumRoad license keys and assign roles.

## Commands
All commands are prefixed by `?`

| Command | Parameters | Description|
| ------- | ---------- | -----------|
| verify  | `Alias/GumRoad ID`, `License Key` | Verifies the authenticity of a GumRoad license key given an alias or gumroad product permalink and a product key |
| link    | `Gumroad ID`, `Role ID or Mention` | Links a GumRoad product ID to a role given it's ID or mention |
| unlink  | `Gumroad ID` | Unlinks a GumRoad ID and any associated aliases from all roles |
| alias   | `Gumroad ID`, `Proposed Alias` | Links a GumRoad ID to an alias of your choosing to use in the verify command instead of the raw ID |
| unalias | `Existing Alias` | Unlinks an existing alias from any IDs it's associated with |