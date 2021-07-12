import json


class Config:
    def __init__(self):
        file_string = open('config.json', 'r')
        json_parsed = json.load(file_string)

        self.token = json_parsed['discordToken']
        self.gumroadToken = json_parsed['gumroadToken']
        self.table_name = json_parsed['dynamoTableName']
