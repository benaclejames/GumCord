import boto3


class DynamoHelper:
    def __init__(self, table_name):
        self.table = boto3.resource('dynamodb').Table(table_name)

    def create_server(self, server):
        self.table.put_item(Item={"DiscordId": server, "Aliases": {}, "Roles": {}})

    def store_server_command(self, server_id, command, gumroad_id):
        self.table.update_item(
            Key={'DiscordId': server_id},
            UpdateExpression="set Aliases.#a=:g",
            ExpressionAttributeNames={'#a': command.lower()},
            ExpressionAttributeValues={':g': gumroad_id}
        )

    def delete_server_command(self, server_id, command):
        self.table.update_item(
            Key={'DiscordId': server_id},
            UpdateExpression="remove Aliases.#a",
            ExpressionAttributeNames={'#a': command.lower()},
        )

    def delete_server_commands_for_gumroad_id(self, server_id, gumroad_id):
        resp = self.table.get_item(
            Key={'DiscordId': server_id},
            ProjectionExpression="DiscordId, Aliases"
        )

        keys_to_delete = ''
        for key in resp['Item']['Aliases'].keys():
            if resp['Item']['Aliases'][key] == gumroad_id:
                keys_to_delete += "Aliases." + key + ", "

        if not keys_to_delete:
            return

        self.table.update_item(
            Key={'DiscordId': server_id},
            UpdateExpression="remove " + keys_to_delete[:-2],
        )

    def get_command_to_gumroad(self, server_id, command):
        response = self.table.get_item(
            Key={'DiscordId': server_id},
            ProjectionExpression="DiscordId, Aliases.#a",
            ExpressionAttributeNames={'#a': command.lower()}
        )
        if not response['Item']['Aliases'] or command.lower() not in response['Item']['Aliases']:
            return None
        return response['Item']['Aliases'][command.lower()]

    def set_gumroad_to_role(self, server_id, gumroad_id, role_id):
        self.table.update_item(
            Key={'DiscordId': server_id},
            UpdateExpression="set #r.#g=:r",
            ExpressionAttributeNames={'#g': gumroad_id, '#r': "Roles"},
            ExpressionAttributeValues={':r': role_id}
        )

    def del_gumroad_to_role(self, server_id, gumroad_id):
        self.table.update_item(
            Key={'DiscordId': server_id},
            UpdateExpression="remove #r.#g",
            ExpressionAttributeNames={'#g': gumroad_id, '#r': "Roles"}
        )

    def get_gumroad_to_role(self, server_id, gumroad_id):
        response = self.table.get_item(
            Key={'DiscordId': server_id},
            ProjectionExpression="DiscordId, #r.#g",
            ExpressionAttributeNames={'#g': gumroad_id, '#r': "Roles"}
        )
        if 'Roles' not in response['Item']:
            return None
        return response['Item']['Roles'][gumroad_id]
